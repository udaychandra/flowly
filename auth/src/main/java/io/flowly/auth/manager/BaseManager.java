/*
 * Copyright (c) 2015 The original author or authors.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Apache License v2.0 
 *  which accompanies this distribution.
 *
 *  The Apache License v2.0 is available at
 *  http://opensource.org/licenses/Apache-2.0
 *
 *  You may elect to redistribute this code under this license.
 */

package io.flowly.auth.manager;

import io.flowly.auth.graph.Schema;
import io.flowly.core.ObjectKeys;
import io.flowly.core.data.manager.GraphManager;
import io.flowly.core.security.Group;
import io.flowly.core.security.Permission;
import io.flowly.core.security.User;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class BaseManager extends GraphManager {
    public BaseManager(Graph graph) {
        super(graph);
    }

    /**
     * Create a vertex and add specified relationships in the auth graph.
     *
     * @param jsonObject holds the parameters used to create the vertex.
     * @return an empty list or a list of validation errors.
     */
    public abstract JsonArray create(JsonObject jsonObject);

    /**
     * Update a vertex and specified relationships in the auth graph.
     *
     * @param jsonObject holds the parameters used to update the vertex.
     * @return an empty list or a list of validation errors.
     */
    public abstract JsonArray update(JsonObject jsonObject);

    /**
     * Get the vertex based on the unique id assigned by the auth graph.
     *
     * @param id the vertex id in auth graph.
     * @return JSON object representing the vertex and its properties and relationships.
     */
    public abstract JsonObject get(Long id);

    /**
     * Get the vertex based on the user generated unique id.
     *
     * @param uniqueId string that uniquely identifies a vertex in the auth graph.
     * @return JSON object representing the vertex.
     */
    public abstract JsonObject get(String uniqueId);

    /**
     * Search for vertices based on provided criteria.
     *
     * @param pageNumber the page number used to retrieve vertices.
     * @param pageSize the number of vertices that fill a page.
     * @return a list of vertices.
     */
    public abstract JsonArray search(int pageNumber, int pageSize);

    public Handler<Message<JsonObject>> createHandler() {
        return message -> message.reply(create(message.body()));
    }

    public Handler<Message<JsonObject>> updateHandler() {
        return message -> message.reply(update(message.body()));
    }

    public Handler<Message<JsonObject>> searchHandler() {
        return message -> {
            JsonObject args = message.body();
            message.reply(search(args.getInteger(ObjectKeys.PAGE_NUMBER), args.getInteger(ObjectKeys.PAGE_SIZE)));
        };
    }

    public abstract Handler<Message<JsonObject>> getHandler();

    /**
     * Iterates the list of permission objects and grants them to the user
     * or group, if they are not previously granted.
     *
     * @param vertex the node that represents a user or group vertex.
     * @param permissions array of JSON objects each representing permissions on a given resource.
     * @param existingIds existing permissions granted to the user or group.
     */
    protected void grantPermissions(Vertex vertex, JsonArray permissions, Set<Long> existingIds) {
        if (permissions == null) {
            return;
        }

        for (Object prm : permissions) {
            Permission permission = new Permission((JsonObject) prm);

            if (existingIds == null || !existingIds.contains(permission.getResourceVertexId())) {
                Vertex resourceVertex = getVertex(permission.getResourceVertexId());
                vertex.addEdge(Schema.E_HAS_PERMISSION, resourceVertex, Schema.E_P_RWX, permission.getRWX());
            }
        }
    }

    /**
     * Revoke and grant permissions based on the specification.
     * Sequence - remove specified permissions, update specified permissions and then add specified permissions.
     *
     * @param vertex the node that represents a user or group vertex.
     * @param jsonObject JSON object representing the user or group permissions.
     */
    protected void redoPermissions(Vertex vertex, JsonObject jsonObject) {
        // Remove permissions.
        JsonArray permissionsToRemove = jsonObject.getJsonArray(Permission.PERMISSIONS_TO_REMOVE);
        if (permissionsToRemove != null) {
            graph.traversal().V(vertex).outE(Schema.E_HAS_PERMISSION).as("e").
                    inV().has(T.id, P.within(permissionsToRemove.getList().toArray())).
                    <Edge>select("e").drop().toList();
        }

        // Update permissions.
        JsonArray permissionsToUpdate = jsonObject.getJsonArray(Permission.PERMISSIONS_TO_UPDATE);
        if (permissionsToUpdate != null) {
            for (Object prm : permissionsToUpdate) {
                Permission permission = new Permission((JsonObject) prm);
                Long resourceVertexId = permission.getResourceVertexId();

                graph.traversal().V(vertex).outE(Schema.E_HAS_PERMISSION).as("e").
                        inV().has(T.id, resourceVertexId).
                        <Edge>select("e").property(Schema.E_P_RWX, permission.getRWX()).toList();
            }
        }

        // Add permissions.
        JsonArray permissionsToAdd = jsonObject.getJsonArray(Permission.PERMISSIONS_TO_ADD);
        if (permissionsToAdd != null) {
            Set<Long> existingIds = new HashSet<>();
            Long[] idsToAdd = new Long[permissionsToAdd.size()];

            for (int i=0; i<permissionsToAdd.size(); i++) {
                idsToAdd[i] = permissionsToAdd.getJsonObject(i).getLong(Permission.RESOURCE_VERTEX_ID);
            }

            graph.traversal().V(vertex).outE(Schema.E_HAS_PERMISSION).
                    inV().has(T.id, P.within(idsToAdd)).sideEffect(s -> {
                existingIds.add((Long) s.get().id());
            }).toList();

            grantPermissions(vertex, permissionsToAdd, existingIds);
        }

    }

    /**
     * Iterates through the list of ids representing group or user vertices and
     * adds "member" and "memberOf" edges or vice-versa between the given vertex
     * and vertices based on whether the given vertex is the owner or not.
     *
     * @param vertex the node that represents a group or user vertex.
     * @param isOwner indicates if the given vertex is a owner or member.
     * @param ids array of ids representing group or user vertices.
     * @param existingIds set of vertices that hold an edge between them and the provided vertex.
     */
    protected void grantMemberships(Vertex vertex, boolean isOwner, JsonArray ids, Set<Long> existingIds) {
        if (ids == null) {
            return;
        }

        for (Object id : ids) {
            if (existingIds == null || !existingIds.contains(id)) {
                Vertex vertexById = getVertex(id);
                vertex.addEdge(isOwner ? Schema.E_MEMBER : Schema.E_MEMBER_OF, vertexById);
                vertexById.addEdge(isOwner ? Schema.E_MEMBER_OF : Schema.E_MEMBER, vertex);
            }
        }
    }

    /**
     * Add and remove users based on the specifications.
     * Sequence: remove and then add.
     *
     * @param vertex the node that represents a group or user vertex.
     * @param isOwner indicates if the given vertex is an owner or member.
     * @param idsToAdd list of ids representing user or group vertices to be added.
     * @param idsToRemove list of ids representing user or group vertices to be removed.
     */
    protected void redoMemberships(Vertex vertex, boolean isOwner, JsonArray idsToAdd, JsonArray idsToRemove) {
        if (idsToRemove != null) {
            graph.traversal().V(vertex).bothE(Schema.E_MEMBER, Schema.E_MEMBER_OF).as("e").
                    otherV().has(T.id, P.within(idsToRemove.getList().toArray())).<Edge>select("e").drop().toList();
        }

        if (idsToAdd != null) {
            Set<Long> existingIds = new HashSet<>();

            graph.traversal().V(vertex).bothE(isOwner ? Schema.E_MEMBER : Schema.E_MEMBER_OF).
                    otherV().has(T.id, P.within(idsToAdd.getList().toArray())).sideEffect(s -> {
                existingIds.add((Long) s.get().id());
            }).toList();

            grantMemberships(vertex, isOwner, idsToAdd, existingIds);
        }
    }

    /**
     * Retrieves direct and indirect memberships that a user or group holds.
     *
     * @param vertex vertex in the auth graph representing a user or group.
     * @param jsonObject JSON object representing the user or group to which retrieved memberships are added.
     * @param includeEffectiveMemberships indicates if all the user or group memberships are to be retrieved.
     * @param includeDirectMemberships indicates if the user's direct memberships are to be retrieved.
     * @param includePermissions indicates if the permissions granted to each group are to be retrieved.
     */
    protected void getMemberships(Vertex vertex, JsonObject jsonObject, boolean includeEffectiveMemberships,
                                boolean includeDirectMemberships, boolean includePermissions) {
        boolean isUserVertex = jsonObject.containsKey(User.USER_ID);
        String uniqueId = isUserVertex ? jsonObject.getString(User.USER_ID) : jsonObject.getString(Group.GROUP_ID);

        if (includeEffectiveMemberships || includePermissions) {
            JsonArray effectiveMemberships = new JsonArray();
            jsonObject.put(User.EFFECTIVE_MEMBERSHIPS, effectiveMemberships);

            List<Vertex> groupVertices = graph.traversal().
                    V(vertex).repeat(__.outE(Schema.E_MEMBER_OF).inV()).emit().toList();
            getDistinctMemberships(groupVertices, effectiveMemberships, uniqueId, isUserVertex, includePermissions);
        }

        if (includeDirectMemberships) {
            JsonArray directMemberships = new JsonArray();
            jsonObject.put(User.DIRECT_MEMBERSHIPS, directMemberships);

            getDistinctMemberships(graph.traversal().V(vertex).outE(Schema.E_MEMBER_OF).inV().toList(),
                    directMemberships, null, false, false);
        }
    }

    /**
     * Add distinct groups to the memberships array.
     *
     * @param groupVertices vertices that represent groups in the auth graph.
     * @param memberships JSON array to which the group is to be added.
     * @param uniqueId unique id that identifies a user or group. Can be null.
     * @param idRepresentsUser indicates if the unique id argument represents a user or a group.
     * @param includePermissions indicates if the permissions granted to the group are to be retrieved.
     */
    private void getDistinctMemberships(List<Vertex> groupVertices, JsonArray memberships,
                                        String uniqueId, boolean idRepresentsUser, boolean includePermissions) {
        Set<Object> groupIds = new HashSet<>();

        for (Vertex groupVertex : groupVertices) {
            if (!groupIds.contains(groupVertex.id())) {
                JsonObject group = makeGroupObject(groupVertex, uniqueId, idRepresentsUser);
                memberships.add(group);

                if (includePermissions) {
                    getDirectPermissions(groupVertex, group);
                }

                groupIds.add(groupVertex.id());
            }
        }
    }

    /**
     * Get the permissions directly granted on the given user or group vertex.
     *
     * @param vertex represents a user or group vertex in the auth graph.
     * @param jsonObject JSON object representing the user or group.
     */
    protected void getDirectPermissions(Vertex vertex, JsonObject jsonObject) {
        jsonObject.put(Permission.DIRECT_PERMISSIONS, new JsonArray(graph.traversal().V(vertex).
                outE(Schema.E_HAS_PERMISSION).
                map(m -> makePermissionObject(m.get())).toList()));
    }

    /**
     * Generate a JSON representation of the user vertex.
     *
     * @param userVertex the node that represents a user vertex.
     * @return JSON object representing the user attributes.
     */
    protected User makeUserObject(Vertex userVertex) {
        User user = new User();
        user.setId(userVertex.id());
        user.setUserId(getPropertyValue(userVertex, Schema.V_P_USER_ID));
        user.setFirstName(getPropertyValue(userVertex, Schema.V_P_FIRST_NAME));
        user.setLastName(getPropertyValue(userVertex, Schema.V_P_LAST_NAME));
        user.setMiddleName(getPropertyValue(userVertex, Schema.V_P_MIDDLE_NAME));
        user.setFullName(getPropertyValue(userVertex, Schema.V_P_NAME));
        user.setInternal(getPropertyValue(userVertex, Schema.V_P_IS_INTERNAL));

        return user;
    }

    /**
     * Generate a JSON representation of the group vertex.
     *
     * @param groupVertex vertex in the auth graph that represents a group.
     * @param uniqueId optional string that identifies a user or group.
     *                 If provided, checks if the group and userId/groupId have a direct edge.
     * @param idRepresentsUser indicates if the unique id argument represents a user or a group.
     * @return JSON object representing a group.
     */
    protected Group makeGroupObject(Vertex groupVertex, String uniqueId, boolean idRepresentsUser) {
        Group group = new Group();
        group.setId(groupVertex.id());
        group.setGroupId(getPropertyValue(groupVertex, Schema.V_P_GROUP_ID));
        group.setDescription(getPropertyValue(groupVertex, Schema.V_P_DESCRIPTION));

        if (uniqueId != null) {
            // Figure out if the user or group is a direct or indirect member of this group.
            String label = idRepresentsUser ? Schema.V_USER : Schema.V_GROUP;
            String key = idRepresentsUser ? Schema.V_P_USER_ID : Schema.V_P_GROUP_ID;

            group.setInherited(!graph.traversal().V(groupVertex).
                    outE(Schema.E_MEMBER).inV().has(label, key, uniqueId).hasNext());
        }

        return group;
    }

    /**
     * Generate a JSON representation of a permission - read, write and execute privileges on a resource.
     *
     * @param permissionEdge edge in the auth graph that represents a permission.
     * @return JSON object representing access rights granted on a resource.
     */
    protected Permission makePermissionObject(Edge permissionEdge) {
        Vertex resource = permissionEdge.inVertex();

        Permission permission = new Permission();
        permission.setRWX(getPropertyValue(permissionEdge, Schema.E_P_RWX));
        permission.setResourceId(getPropertyValue(resource, Schema.V_P_RESOURCE_ID));
        permission.setResourceVertexId((Long) resource.id());

        return permission;
    }
}

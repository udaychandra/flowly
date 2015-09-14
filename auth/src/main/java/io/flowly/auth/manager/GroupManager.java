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
import io.flowly.core.security.Group;
import io.flowly.core.security.Permission;
import io.flowly.core.security.User;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Defines CRUD operations on groups in flowly.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class GroupManager extends BaseManager {
    private static final Logger logger = LoggerFactory.getLogger(GroupManager.class);

    public GroupManager(Graph graph) {
        super(graph);
    }

    /**
     * Create a group node in the auth graph.
     *
     * @param group JSON object representing a group's attributes, permissions, users and memberships.
     *              Ex: {
     *                  "groupId": "Group 1",
     *                  "usersToAdd": [
     *                      13,
     *                      14
     *                  ],
     *                  "groupsToAdd": [
     *                      12343,
     *                      34567
     *                  ],
     *                  "permissionsToAdd": [
     *                      {
     *                          "resourceVertexId": 555611
     *                          "rwx": 7
     *                      }
     *                  ]
     *              }
     * @return an empty list or a list of validation errors based on whether the group was created or not.
     */
    @Override
    public JsonArray create(JsonObject group) {
        Group newGroup = new Group(group);
        JsonArray errors = newGroup.validate();

        if (errors.size() == 0) {
            try {
                Vertex groupVertex = graph.addVertex(T.label, Schema.V_GROUP,
                        Schema.V_P_GROUP_ID, newGroup.getGroupId());
                setPropertyValue(groupVertex, Schema.V_P_DESCRIPTION, newGroup.getDescription());

                grantMemberships(groupVertex, true, group.getJsonArray(Group.USERS_TO_ADD), null);
                grantMemberships(groupVertex, false, group.getJsonArray(User.GROUPS_TO_ADD), null);
                grantPermissions(groupVertex, group.getJsonArray(Permission.PERMISSIONS_TO_ADD), null);

                commit();
            }
            catch (Exception ex) {
                rollback();
                String error = "Unable to create group: " + newGroup.getGroupId();
                logger.error(error, ex);
                errors.add(error);
            }
        }

        return errors;
    }

    /**
     * Update a group node in the auth graph.
     *
     * @param group JSON object representing a group's attributes, permissions, users and memberships.
     *              Ex: {
     *                  "id": 12345,
     *                  "description": "Represents flowly users.",
     *                  "usersToRemove": [
     *                      14
     *                  ],
     *                  "groupsToAdd": [
     *                      34567
     *                  ],
     *                  "groupsToRemove": [
     *                      12343
     *                  ],
     *                  "permissionsToRemove": [
     *                      555611
     *                  ]
     *              }
     * @return an empty list or a list of validation errors based on whether the group was updated or not.
     */
    @Override
    public JsonArray update(JsonObject group) {
        Group updatedGroup = new Group(group);
        JsonArray errors = updatedGroup.validate(true);

        if (errors.size() == 0) {
            try {
                Vertex groupVertex = getVertex(updatedGroup.getId());
                setGroupId(groupVertex, updatedGroup, true);
                setPropertyValue(groupVertex, Schema.V_P_DESCRIPTION, updatedGroup.getDescription());

                // Update group memberships.
                redoMemberships(groupVertex, false, updatedGroup.getJsonArray(User.GROUPS_TO_ADD),
                        updatedGroup.getJsonArray(User.GROUPS_TO_REMOVE));

                // Update group users.
                redoMemberships(groupVertex, true, updatedGroup.getJsonArray(Group.USERS_TO_ADD),
                        updatedGroup.getJsonArray(Group.USERS_TO_REMOVE));

                // Update permissions.
                redoPermissions(groupVertex, group);

                commit();
            }
            catch (IllegalArgumentException ex) {
                rollback();
                errors.add(ex.getMessage());
                logger.error(ex);
            }
            catch (Exception ex) {
                rollback();
                String error = "Unable to update group: " + updatedGroup.getGroupId();
                logger.error(error, ex);
                errors.add(error);
            }
        }

        return errors;
    }

    /**
     * Get the group based on the id.
     * Doesn't retrieve members, memberships and permissions.
     *
     * @param id unique id set by the auth graph.
     * @return JSON object representing a group.
     *         Ex: {
     *             "id": 12345,
     *             "groupId": "Group 1",
     *             "description": "Represents flowly users."
     *         }
     */
    @Override
    public JsonObject get(Long id) {
        try {
            Vertex groupVertex = getVertex(id);
            JsonObject group = makeGroupObject(groupVertex, null, false);
            commit();

            return group;
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to retrieve group: " + id, ex);
            return null;
        }
    }

    /**
     * Get the group based on group Id.
     * Doesn't retrieve members, memberships and permissions.
     *
     * @param groupId the group id the uniquely identifies a group in the auth graph.
     * @return JSON object representing a group.
     *         Ex: {
     *             "id": 12345,
     *             "groupId": "Group 1",
     *             "description": "Represents flowly users."
     *         }
     */
    @Override
    public JsonObject get(String groupId) {
        return get(groupId, false, false, false, false, false);
    }

    /**
     * Get the group based on group Id.
     *
     * @param groupId the group id that uniquely identifies a group in the auth graph.
     * @param includeDirectMemberships indicates if the group's direct memberships are to be retrieved.
     * @param includeEffectiveMemberships indicates if all the group's memberships are to be retrieved.
     * @param includeDirectMembers indicates if the group's direct members are to be retrieved.
     * @param includeEffectiveUsers indicates if all the group's users are to be retrieved.
     * @param includeDirectPermissions indicates if the permissions directly granted to the group are to be retrieved.
     * @return JSON object representing a group and optional memberships and permissions.
     *         Ex: {
     *             "id": 12345,
     *             "groupId": "Group A",
     *             "description": "Represents flowly users."
     *             "directMemberships": [
     *                 {
     *                     "id": 12345,
     *                     "groupId": "Group 1",
     *                 }
     *             ],
     *             "effectiveMemberships": [
     *                 {
     *                     "id": 12345,
     *                     "groupId": "Group 1",
     *                 },
     *                 {
     *                     "id": 12350,
     *                     "groupId": "Group 2",
     *                 }
     *             ],
     *             "directMembers: [
     *                 {
     *                     "id": 1,
     *                     "userId": "aragorn",
     *                     "fullName": "Strider Wingfoot"
     *                 }
     *             ]
     *             "directPermissions": [
     *                 {
     *                     "resourceId": 555611
     *                     "rwx": 7
     *                 }
     *             ]
     *         }
     */
    public JsonObject get(String groupId, boolean includeDirectMemberships, boolean includeEffectiveMemberships,
                          boolean includeDirectMembers, boolean includeEffectiveUsers,
                          boolean includeDirectPermissions) {
        try {
            JsonObject group = null;
            GraphTraversal<Vertex, Vertex> traversal = graph.traversal().
                    V().has(Schema.V_GROUP, Schema.V_P_GROUP_ID, groupId);

            if (traversal.hasNext()) {
                Vertex groupVertex = traversal.next();
                group = makeGroupObject(groupVertex, null, false);
                getMembers(groupVertex, group, includeDirectMembers, includeEffectiveUsers);
                getMemberships(groupVertex, group, includeEffectiveMemberships, includeDirectMemberships, false);

                if (includeDirectPermissions) {
                    getDirectPermissions(groupVertex, group);
                }
            }

            commit();
            return group;
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to retrieve group: " + groupId, ex);
            return null;
        }
    }

    @Override
    public Handler<Message<JsonObject>> getHandler() {
        return message -> {
            JsonObject args = message.body();
            message.reply(get(args.getString(Group.GROUP_ID),
                    args.getBoolean("includeDirectMemberships"),
                    args.getBoolean("includeEffectiveMemberships"),
                    args.getBoolean("includeDirectMembers"),
                    args.getBoolean("includeEffectiveUsers"),
                    args.getBoolean("includeDirectPermissions")));
        };
    }

    /**
     * Search for groups based on provided criteria.
     * By default, groups are sorted by GROUP_ID in ascending order.
     *
     * @param pageNumber the page number used to retrieve groups.
     * @param pageSize the number of users that fill a page.
     * @return a list of groups along with their respective members and memberships.
     */
    @Override
    public JsonArray search(int pageNumber, int pageSize) {
        int low = (pageNumber - 1) * pageSize;
        int high = low + pageSize;

        try {
            List<Group> groups = graph.traversal().V().hasLabel(Schema.V_GROUP).order().
                    by(Schema.V_P_GROUP_ID, Order.incr).range(low, high + ADDITIONAL_RECORDS).
                    map(m -> {
                        Vertex groupVertex = m.get();
                        Group group = makeGroupObject(groupVertex, null, false);
                        getMembers(groupVertex, group, true, false);
                        getMemberships(groupVertex, group, false, true, false);

                        return group;
                    }).toList();

            commit();
            return new JsonArray(groups);
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to search for groups.", ex);
            return null;
        }
    }

    @Override
    public JsonArray delete(Object id) {
        JsonArray errors;

        try {
            // Cannot delete administrators group.
            Vertex vertex = getVertex(id);
            if (!ObjectKeys.ADMIN_GROUP_ID.equalsIgnoreCase(getPropertyValue(vertex, Schema.V_P_GROUP_ID))) {
                errors = super.delete(id);
            }
            else {
                errors = new JsonArray().add("Cannot delete group: " + id);
            }

            commit();
        }
        catch (Exception ex) {
            rollback();
            errors = new JsonArray().add("Cannot delete group:" + id);
            logger.error(errors.getString(0), ex);
        }

        return errors;
    }

    private void getMembers(Vertex groupVertex, JsonObject group,
                            boolean includeDirectMembers, boolean includeEffectiveUsers) {
        if (includeDirectMembers) {
            JsonArray directMembers = new JsonArray();
            group.put(Group.DIRECT_MEMBERS, directMembers);

            graph.traversal().V(groupVertex).outE(Schema.E_MEMBER).inV().sideEffect(m -> {
                Vertex vertex = m.get();
                if (vertex.label().equals(Schema.V_USER)) {
                    directMembers.add(makeUserObject(vertex));
                }
                else {
                    directMembers.add(makeGroupObject(vertex, null, false));
                }

            }).toList();
        }

        // Recursively retrieve all unique users.
        if (includeEffectiveUsers) {
            JsonArray allUsers = new JsonArray();
            group.put(Group.ALL_USERS, allUsers);

            getUsers(groupVertex, allUsers, new HashSet<>());
        }
    }

    private void getUsers(Vertex groupVertex, JsonArray users, Set<Long> userIds) {
        graph.traversal().V(groupVertex).repeat(__.outE(Schema.E_MEMBER).inV()).emit().sideEffect(m -> {
            Vertex vertex = m.get();

            if (vertex.label().equals(Schema.V_USER) && !userIds.contains(vertex.id())) {
                users.add(makeUserObject(vertex));
                userIds.add((Long) vertex.id());
            }
            else {
                getUsers(vertex, users, userIds);
            }

        }).toList();
    }

    private void setGroupId(Vertex groupVertex, Group group, boolean isUpdate) {
        if (isUpdate) {
            String oldGroupId = getPropertyValue(groupVertex, Schema.V_P_GROUP_ID);
            String newGroupId = group.getGroupId();

            if (oldGroupId.equalsIgnoreCase(ObjectKeys.ADMIN_GROUP_ID)) {
                if (!oldGroupId.equalsIgnoreCase(newGroupId)) {
                    throw new IllegalArgumentException("Cannot change the administrator group id.");
                }
            }
            else {
                setPropertyValue(groupVertex, Schema.V_P_GROUP_ID, group.getGroupId());
            }
        }
        else {
            setPropertyValue(groupVertex, Schema.V_P_GROUP_ID, group.getGroupId());
        }
    }
}

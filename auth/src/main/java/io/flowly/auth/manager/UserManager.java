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
import io.flowly.core.data.Resource;
import io.flowly.core.security.PasswordHash;
import io.flowly.core.security.Permission;
import io.flowly.core.security.User;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines CRUD operations on users in flowly.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class UserManager extends BaseManager {
    private static final Logger logger = LoggerFactory.getLogger(UserManager.class);

    public UserManager(Graph graph) {
        super(graph);
    }

    /**
     * Create a user node in the auth graph.
     * If this is an internally managed user, the provided password will be hashed before storing in the graph.
     * If group memberships are specified, add user to the groups.
     * If specified, grant permissions on resources (direct edge between user and resource).
     *
     * @param user JSON object representing the user attributes, memberships and permissions.
     *             Ex: {
     *                 "userId": "aragorn",
     *                 "firstName": "First",
     *                 "lastName": "Last",
     *                 "isInternal": false,
     *                 "groupsToAdd": [
     *                     12343,
     *                     34567,
     *                     99901
     *                 ],
     *                 "permissionsToAdd": [
     *                     {
     *                         "resourceVertexId": 555611
     *                         "rwx": 7
     *                     }
     *                 ]
     *             }
     * @return an empty list or a list of validation errors based on whether the user was created or not.
     */
    @Override
    public JsonArray create(JsonObject user) {
        User newUser = new User(user);
        JsonArray errors = newUser.validate();

        if (errors.size() == 0) {
            try {
                Vertex userVertex = graph.addVertex(Schema.V_USER);
                setUserAttributes(userVertex, newUser, false);
                grantMemberships(userVertex, false, newUser.getGroupsToAdd(), null);
                grantPermissions(userVertex, newUser.getPermissionsToAdd(), null);

                commit();
            }
            catch (Exception ex) {
                rollback();
                String error = "Unable to create user: " + newUser.getUserId();
                logger.error(error, ex);
                errors.add(error);
            }
        }

        return errors;
    }

    /**
     * Update the specified user attributes in the auth graph.
     * If specified, update user memberships.
     * If specified, update user permissions on given resources.
     *
     * @param user JSON object representing the user attributes, memberships and permissions.
     *             Ex: {
     *                 "id": 999123,
     *                 "middleName": "C",
     *                 "password": "!234aCbbJk_#3"
     *                 "groupsToAdd": [
     *                     34567
     *                 ],
     *                 "groupsToRemove": [
     *                     12345
     *                 ],
     *                 "permissionsToUpdate": [
     *                     {
     *                         "resourceVertexId": 555611
     *                         "rwx": 6
     *                     }
     *                 ]
     *             }
     * @return an empty list or a list of validation errors based on whether the user was updated or not.
     */
    @Override
    public JsonArray update(JsonObject user) {
        User updatedUser = new User(user);
        JsonArray errors = updatedUser.validate(true);

        try {
            Vertex userVertex = getVertex(updatedUser.getId());

            if (userVertex != null) {
                setUserAttributes(userVertex, updatedUser, true);
                redoPermissions(userVertex, user);
                redoMemberships(userVertex, false, updatedUser.getGroupsToAdd(), updatedUser.getGroupsToRemove());
            }
            else {
                errors.add("User does not exist: " + updatedUser.getId());
            }

            commit();
        }
        catch (Exception ex) {
            rollback();
            String error = "Unable to update user: " + updatedUser.getId();
            logger.error(error, ex);
            errors.add(error);
        }

        return errors;
    }

    /**
     * Verify the authenticity of the provided user credentials.
     *
     * @param user JSON object representing the user credentials.
     *             Ex: {
     *                 "userId": "aragorn",
     *                 "password": "!234aCbbJk_#3"
     *             }
     * @return JSON object representing the authenticated user attributes and effective permissions.
     *         Ex: {
     *             "userId": "aragorn",
     *             "firstName": "First",
     *             "lastName": "Last",
     *             "middleName": "M",
     *             "fullName": "First M Last",
     *             "isInternal": true,
     *             "authenticated": true,
     *             "effectivePermissions": [
     *                 {
     *                     "resourceVertexId": 54113,
     *                     "resourceId": "Studio"
     *                     "rwx": 7
     *                 }
     *             ]
     *         }
     */
    public JsonObject authenticate(JsonObject user) {
        user.put(User.AUTHENTICATED, false);
        String password = (String) user.remove(User.PASSWORD);
        String userId = user.getString(User.USER_ID);

        try {
            if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(password)) {

                Traversal<Vertex, Vertex> traversal = graph.traversal().V().
                        has(Schema.V_USER, Schema.V_P_USER_ID, userId);

                if (traversal.hasNext()) {
                    Vertex userVertex = traversal.next();
                    String hash = getPropertyValue(userVertex, Schema.V_P_PASSWORD).toString();
                    boolean authenticated = PasswordHash.validatePassword(password, hash);

                    if (authenticated) {
                        user = get(userVertex, true, false, false, true);
                        user.put(User.AUTHENTICATED, true);
                    }
                }

                commit();
            }
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to authenticate user.", ex);
        }

        return user;
    }

    public Handler<Message<JsonObject>> authenticateHandler() {
        return message -> message.reply(authenticate(message.body()));
    }

    /**
     * Get the user based on the unique id assigned by the auth graph.
     * Doesn't retrieve user memberships or permissions.
     *
     * @param id the user vertex id in auth graph.
     * @return JSON object representing the user.
     *         Ex: {
     *             "userId": "aragorn",
     *             "firstName": "First",
     *             "lastName": "Last",
     *             "middleName": "M",
     *             "fullName": "First M Last",
     *             "isInternal": true
     *         }
     */
    @Override
    public JsonObject get(Long id) {
        try {
            Vertex userVertex = getVertex(id);
            JsonObject user = makeUserObject(userVertex);
            commit();

            return user;
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to retrieve user: " + id, ex);
            return null;
        }
    }

    /**
     * Get the user based on user Id.
     * Doesn't retrieve user memberships or permissions.
     *
     * @param userId the userId the uniquely identifies a user in the auth graph.
     * @return JSON object representing a user.
     *         Ex: {
     *             "userId": "aragorn",
     *             "firstName": "First",
     *             "lastName": "Last",
     *             "middleName": "M",
     *             "fullName": "First M Last",
     *             "isInternal": true
     *         }
     */
    @Override
    public JsonObject get(String userId) {
        return get(userId, false, false, false, false);
    }

    /**
     * Get the user based on user Id.
     *
     * @param userId the userId that uniquely identifies a user in the auth graph.
     * @param includeDirectMemberships indicates if the user's direct memberships are to be retrieved.
     * @param includeEffectiveMemberships indicates if all the user's memberships are to be retrieved.
     * @param includeDirectPermissions indicates if the permissions directly granted to the user are to be retrieved.
     * @param includeEffectivePermissions indicates if the effective permissions granted to the user are to be calculated.
     * @return JSON object representing a user and optional memberships and permissions.
     *         Ex: {
     *             "userId": "aragorn",
     *             "firstName": "First",
     *             "lastName": "Last",
     *             "middleName": "M",
     *             "fullName": "First M Last",
     *             "isInternal": true,
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
     *             "directPermissions": [
     *                 {
     *                     "resourceVertexId": 54112,
     *                     "resourceId": "API"
     *                     "rwx": 7
     *                 }
     *             ],
     *             "effectivePermissions": [
     *                 {
     *                     "resourceVertexId": 54113,
     *                     "resourceId": "Studio"
     *                     "rwx": 7
     *                 }
     *             ]
     *         }
     */
    public JsonObject get(String userId, boolean includeDirectMemberships, boolean includeEffectiveMemberships,
                          boolean includeDirectPermissions, boolean includeEffectivePermissions) {
        try {
            JsonObject user = null;
            GraphTraversal<Vertex, Vertex> traversal = graph.traversal().
                    V().has(Schema.V_USER, Schema.V_P_USER_ID, userId);

            if (traversal.hasNext()) {
                Vertex userVertex = traversal.next();
                user = get(userVertex, includeDirectMemberships, includeEffectiveMemberships,
                        includeDirectPermissions, includeEffectivePermissions);
            }

            commit();
            return user;
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to retrieve user: " + userId, ex);
            return null;
        }
    }

    @Override
    public Handler<Message<JsonObject>> getHandler() {
        return message -> {
            JsonObject args = message.body();
            message.reply(get(args.getString(User.USER_ID),
                    args.getBoolean("includeDirectMemberships"),
                    args.getBoolean("includeEffectiveMemberships"),
                    args.getBoolean("includeDirectPermissions"),
                    args.getBoolean("includeEffectivePermissions")));
        };
    }

    /**
     * Search for users based on provided criteria.
     * By default, users are sorted by USER_ID in ascending order.
     *
     * @param pageNumber the page number used to retrieve users.
     * @param pageSize the number of users that fill a page.
     * @return a list of users along with their respective memberships.
     */
    @Override
    public JsonArray search(int pageNumber, int pageSize) {
        int low = (pageNumber - 1) * pageSize;
        int high = low + pageSize;

        try {
            List<User> users = graph.traversal().V().hasLabel(Schema.V_USER).order().
                    by(Schema.V_P_USER_ID, Order.incr).range(low, high + ADDITIONAL_RECORDS).
                    map(m -> {
                        Vertex userVertex = m.get();
                        User user = makeUserObject(userVertex);
                        getMemberships(userVertex, user, true, false, false);

                        return user;
                    }).toList();

            commit();
            return new JsonArray(users);
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to search for users.", ex);
            return null;
        }
    }

    @Override
    public JsonArray delete(Object id) {
        JsonArray errors;

        try {
            // Cannot delete admin user.
            Vertex vertex = getVertex(id);
            if (!getPropertyValue(vertex, Schema.V_P_USER_ID).equals(ObjectKeys.ADMIN_USER_ID)) {
                errors = super.delete(id);
            }
            else {
                errors = new JsonArray().add("Cannot delete user: " + id);
            }

            commit();
        }
        catch (Exception ex) {
            rollback();
            errors = new JsonArray().add("Cannot delete user:" + id);
            logger.error(errors.getString(0), ex);
        }

        return errors;
    }

    /**
     * Populate the user attributes, memberships and permissions into a JSON object.
     *
     * @param includeDirectMemberships indicates if the user's direct memberships are to be retrieved.
     * @param includeEffectiveMemberships indicates if all the user's memberships are to be retrieved.
     * @param includeDirectPermissions indicates if the permissions directly granted to the user are to be retrieved.
     * @param includeEffectivePermissions indicates if the effective permissions granted to the user are to be calculated.
     * @return JSON object representing a user and optional memberships and permissions.
     */
    private JsonObject get(Vertex userVertex, boolean includeDirectMemberships, boolean includeEffectiveMemberships,
                           boolean includeDirectPermissions, boolean includeEffectivePermissions) {
        JsonObject user = makeUserObject(userVertex);

        getMemberships(userVertex, user, includeEffectiveMemberships,
                includeDirectMemberships, includeEffectivePermissions);

        if (includeDirectPermissions || includeEffectivePermissions) {
            getDirectPermissions(userVertex, user);
        }

        if (includeEffectivePermissions) {
            getEffectivePermissions(user);
        }

        // Once effective permissions are calculated, remove
        // other keys if they weren't requested.
        if (!includeDirectMemberships) {
            user.remove(User.DIRECT_MEMBERSHIPS);
        }

        if (!includeDirectPermissions) {
            user.remove(Permission.DIRECT_PERMISSIONS);
        }

        if (!includeEffectiveMemberships) {
            user.remove(User.EFFECTIVE_MEMBERSHIPS);
        }

        return user;
    }

    /**
     * Calculate the effective permissions granted to the given user.
     * The effective permission on each resource is the cumulative grants
     * given to the user either directly or indirectly.
     *
     * @param user JSON object representing the user in the auth graph.
     */
    private void getEffectivePermissions(JsonObject user) {
        JsonArray effectivePermissions = new JsonArray();

        // Holds the resource Ids and their respective permissions that have the cumulative
        // grant value (simple integer - rwx).
        Map<String, JsonObject> definedPermissions = new HashMap<>();

        // Start with direct permissions.
        getEffectivePermissions(user.getJsonArray(Permission.DIRECT_PERMISSIONS), definedPermissions);

        // Check permissions defined on groups.
        JsonArray effectiveMemberships = user.getJsonArray(User.EFFECTIVE_MEMBERSHIPS);

        if (effectiveMemberships != null) {
            for (Object mbr : effectiveMemberships) {
                JsonObject membership = (JsonObject) mbr;

                getEffectivePermissions(membership.getJsonArray(Permission.DIRECT_PERMISSIONS), definedPermissions);
            }
        }

        for (JsonObject permission : definedPermissions.values()) {
            effectivePermissions.add(permission);
        }

        user.put(Permission.EFFECTIVE_PERMISSIONS, effectivePermissions);
    }

    /**
     * Goes through the list of permissions that are to be considered and based on whether the given permission
     * is higher than the previously defined one, adds the new permission to the map.
     *
     * @param permissionsToConsider the list of new permissions to consider to add to the map.
     * @param definedPermissions the existing map of permissions added thus far.
     */
    private void getEffectivePermissions(JsonArray permissionsToConsider, Map<String, JsonObject> definedPermissions) {
        if (permissionsToConsider == null) {
            return;
        }

        for (Object prm : permissionsToConsider) {
            JsonObject permission = (JsonObject) prm;
            String resourceId = permission.getString(Resource.RESOURCE_ID);

            if (!definedPermissions.containsKey(resourceId)) {
                definedPermissions.put(resourceId, permission);
            }
            else {
                JsonObject definedPermission = definedPermissions.get(resourceId);
                definedPermissions.put(resourceId, Permission.merge(definedPermission, permission));
            }
        }
    }

    private void setUserAttributes(Vertex userVertex, User user, boolean isUpdate) throws Exception {
        setPropertyValue(userVertex, Schema.V_P_USER_ID, user.getUserId());

        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        String middleName = user.getMiddleName();

        setPropertyValue(userVertex, Schema.V_P_FIRST_NAME, firstName);
        setPropertyValue(userVertex, Schema.V_P_LAST_NAME, lastName);
        setPropertyValue(userVertex, Schema.V_P_MIDDLE_NAME, middleName);
        setFullName(userVertex, firstName, lastName, middleName, isUpdate);

        setPropertyValue(userVertex, Schema.V_P_IS_INTERNAL, user.isInternal());

        String password = user.getPassword();
        if (password != null) {
            setPropertyValue(userVertex, Schema.V_P_PASSWORD, PasswordHash.createHash(password));
        }
    }

    /**
     * Tries to set the full name of the user as "firstName [middleName] lastName".
     *
     * @param userVertex vertex in the auth graph that represent a user.
     * @param firstName user's first name.
     * @param lastName user's last name.
     * @param middleName user's middle name.
     */
    public void setFullName(Vertex userVertex, String firstName, String lastName, String middleName, boolean isUpdate) {
        if (firstName == null && lastName == null && middleName == null) {
            return;
        }

        if (isUpdate) {
            if (firstName == null) {
                firstName = getPropertyValue(userVertex, Schema.V_P_FIRST_NAME).toString();
            }

            if (lastName == null) {
                lastName = getPropertyValue(userVertex, Schema.V_P_LAST_NAME).toString();
            }

            if (middleName == null) {
                Object mname = getPropertyValue(userVertex, Schema.V_P_MIDDLE_NAME);
                middleName = mname != null ? mname.toString() : null;
            }
        }

        String fullName = middleName == null ?
                (firstName + " " + lastName) : (firstName + " " + middleName + " " + lastName);
        setPropertyValue(userVertex, Schema.V_P_NAME, fullName);
    }
}

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
import io.flowly.core.security.Group;
import io.flowly.core.security.Permission;
import io.flowly.core.security.User;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a>Uday Tatiraju</a>
 */
@RunWith(VertxUnitRunner.class)
public class UserManagerTest extends BaseManagerTest {
    private UserManager userManager;

    @Before
    public void setUp() {
        super.setUp();

        graph = TinkerGraph.open();
        userManager = new UserManager(graph);
        identity = 1L;

        graph.createIndex(User.USER_ID, Vertex.class);

        createGroups();
        createResources();
    }

    @Test
    public void testCreateSimpleUser(TestContext context) {
        context.assertTrue(userManager.create(generateUser(false, false)).size() == 0,
                "Assertion of simple user creation failed.");

        assertUserAttributes(context, userManager.get("aragorn"), true);
    }

    @Test
    public void testCreateUser(TestContext context) {
        context.assertTrue(userManager.create(generateUser(true, true)).size() == 0,
                "Assertion of user creation (with password and middle name) failed.");

        assertUserAttributes(context, userManager.get("aragorn", true, false, false, false), false);
    }

    @Test
    public void testCreateUserWithGroups(TestContext context) {
        JsonObject user = generateUser(true, false);
        addGroupsToUser(user);
        context.assertTrue(userManager.create(user).size() == 0, "Assertion of user creation (with groups) failed.");

        user = userManager.get("aragorn", false, true, false, false);
        assertUserAttributes(context, user, true);
        assertMemberships(context, user.getJsonArray(User.EFFECTIVE_MEMBERSHIPS), 3);
    }

    @Test
    public void testCreateUserWithPermissions(TestContext context) {
        JsonObject user = generateUser(true, false);
        addPermissionsToUser(user);
        context.assertTrue(userManager.create(user).size() == 0,
                "Assertion of user creation (with permissions) failed.");

        user = userManager.get("aragorn", false, false, true, false);
        assertUserAttributes(context, user, true);
        assertDirectPermissions(context, user.getJsonArray(Permission.DIRECT_PERMISSIONS));
    }

    @Test
    public void testCreateUserWithGroupsAndPermissions(TestContext context) {
        JsonObject user = generateUser(true, false);
        addGroupsToUser(user);
        addPermissionsToUser(user);

        context.assertTrue(userManager.create(user).size() == 0,
                "Assertion of user creation (with groups and permissions) failed.");

        user = userManager.get("aragorn", true, true, true, true);
        assertUserAttributes(context, user, true);
        assertMemberships(context, user.getJsonArray(User.EFFECTIVE_MEMBERSHIPS), 3);
        assertDirectPermissions(context, user.getJsonArray(Permission.DIRECT_PERMISSIONS));

        Map<String, Integer> expectedPermissions = new HashMap<>();
        expectedPermissions.put("Studio", 7);
        expectedPermissions.put("Console", 5);
        assertEffectivePermissions(context, user.getJsonArray(Permission.EFFECTIVE_PERMISSIONS), expectedPermissions);
    }

    @Test
    public void testCreateUserWithComplexGroupsAndPermissions(TestContext context) {
        JsonObject user = generateUser(true, false);
        addBunchOfGroups(user);
        addPermissionsToUser(user);
        context.assertTrue(userManager.create(user).size() == 0,
                "Assertion of user creation (with complex groups and permissions) failed.");

        user = userManager.get("aragorn", true, true, true, true);
        assertUserAttributes(context, user, true);
        assertMemberships(context, user.getJsonArray(User.EFFECTIVE_MEMBERSHIPS), 2);
        assertDirectPermissions(context, user.getJsonArray(Permission.DIRECT_PERMISSIONS));

        Map<String, Integer> expectedPermissions = new HashMap<>();
        expectedPermissions.put("Launchpad", 7);
        expectedPermissions.put("Studio", 7);
        expectedPermissions.put("Console", 7);
        expectedPermissions.put("Dashboard", 7);

        assertEffectivePermissions(context, user.getJsonArray(Permission.EFFECTIVE_PERMISSIONS), expectedPermissions);
    }

    @Test
    public void testUpdateUserAttributes(TestContext context) {
        JsonObject user = generateUser(true, false);
        context.assertTrue(userManager.create(user).size() == 0, "Errors thrown while creating new user.");

        User updatedUser = new User(userManager.get("aragorn"));
        updatedUser.setUserId("aragorn1");
        updatedUser.setMiddleName("Estel");

        updatedUser.remove(User.FIRST_NAME);
        updatedUser.remove(User.LAST_NAME);

        context.assertTrue(userManager.update(updatedUser).size() == 0, "Errors thrown while updating user.");
        context.assertEquals("Strider Estel Wingfoot", new User(userManager.get(updatedUser.getId())).getFullName(),
                "Update full name is not as expected.");
    }

    @Test
    public void testUpdateUserPermissions(TestContext context) {
        JsonObject user = generateUser(true, false);
        addPermissionsToUser(user);
        context.assertTrue(userManager.create(user).size() == 0, "Errors thrown while creating new user.");

        user = userManager.get("aragorn", false, false, false, true);

        Map<String, Integer> expectedPermissions = new HashMap<>();
        expectedPermissions.put("Studio", 7);
        expectedPermissions.put("Console", 5);
        assertEffectivePermissions(context, user.getJsonArray(Permission.EFFECTIVE_PERMISSIONS), expectedPermissions);

        User updatedUser = new User();
        updatedUser.setId(user.getLong(ObjectKeys.ID));
        updateUserPermissions(updatedUser);
        removeUserPermissions(updatedUser);

        context.assertTrue(userManager.update(updatedUser).size() == 0,
                "Errors thrown while updating user permissions.");
        user = userManager.get("aragorn", false, false, false, true);
        expectedPermissions.remove("Console");
        expectedPermissions.put("Studio", 1);
        assertEffectivePermissions(context, user.getJsonArray(Permission.EFFECTIVE_PERMISSIONS), expectedPermissions);
    }

    @Test
    public void testUpdateUserMemberships(TestContext context) {
        JsonObject user = generateUser(true, false);
        addGroupsToUser(user);
        context.assertTrue(userManager.create(user).size() == 0, "Errors thrown while creating new user.");

        user = userManager.get("aragorn", true, false, false, false);
        Set<String> expectedMemberships = new HashSet<>();
        expectedMemberships.add("Group 2");
        expectedMemberships.add("Group 3");
        assertMemberships(context, user.getJsonArray(User.DIRECT_MEMBERSHIPS), expectedMemberships);

        User updatedUser = new User();
        updatedUser.setId(user.getLong(ObjectKeys.ID));
        updatedUser.put(User.GROUPS_TO_ADD, new JsonArray().add(1L));
        updatedUser.put(User.GROUPS_TO_REMOVE, new JsonArray().add(2L).add(3L));

        context.assertTrue(userManager.update(updatedUser).size() == 0,
                "Errors thrown while updating user memberships");

        user = userManager.get("aragorn", true, false, false, false);
        expectedMemberships.clear();
        expectedMemberships.add("Group 1");
        assertMemberships(context, user.getJsonArray(User.DIRECT_MEMBERSHIPS), expectedMemberships);
    }

    @Test
    public void testDeleteUser(TestContext context) {
        User user = generateUser(true, false);
        addGroupsToUser(user);
        addPermissionsToUser(user);

        context.assertTrue(userManager.create(user).size() == 0,
                "Errors thrown while creating user (with groups and permissions).");

        user = new User(userManager.get("aragorn"));
        context.assertTrue(userManager.delete(user.getId()).size() == 0, "Errors thrown while deleting user.");
    }

    @Test
    public void testLogin(TestContext context) {
        context.assertTrue(userManager.create(generateUser(true, false)).size() == 0,
                "Errors thrown while creating user.");

        User user = new User();
        user.setUserId("aragorn");
        user.setPassword("password");
        context.assertTrue(userManager.authenticate(user).getBoolean(User.AUTHENTICATED), "User authentication failed.");
    }

    @Test
    public void testFailedLogin(TestContext context) {
        context.assertTrue(userManager.create(generateUser(true, false)).size() == 0,
                "Errors thrown while creating user.");

        User user = new User();
        user.setUserId("aragorn");
        user.setPassword("wrongpassword");
        context.assertFalse(userManager.authenticate(user).getBoolean(User.AUTHENTICATED),
                "User authentication should fail.");
    }

    @Test
    public void testSearchAll(TestContext context) {
        Map<String, Integer> expectedUsers = new HashMap<>();

        for (int i=0; i<13; i++) {
            User user = generateUser("uid" + i, "fn " + i, "ln " + i);

            if (i % 3 == 0) {
                addGroupsToUser(user);
                expectedUsers.put(user.getUserId(), 3);
            }
            else {
                expectedUsers.put(user.getUserId(), 0);
            }

            context.assertTrue(userManager.create(user).size() == 0);
        }

        JsonArray users = userManager.search(1, 10);
        context.assertTrue(users.size() == 11, "Result of search all users not as expected.");

        for (Object usr : users) {
            JsonObject user = (JsonObject) usr;
            String userId = user.getString(User.USER_ID);

            context.assertEquals(expectedUsers.get(userId),
                    user.getJsonArray(User.EFFECTIVE_MEMBERSHIPS).size(),
                    "Actual and expected memberships differ for user: " + userId);
        }
    }

    private User generateUser(boolean includePassword, boolean includeMiddleName) {
        User user = new User();
        user.setUserId("aragorn");
        user.setFirstName("Strider");
        user.setLastName("Wingfoot");
        user.setInternal(true);

        if (includePassword) {
            user.setPassword("password");
            user.put("confirmPassword", "password");
        }

        if (includeMiddleName) {
            user.setMiddleName("Estel");
        }

        return user;
    }

    private User generateUser(String userId, String firstName, String lastName) {
        User user = new User();
        user.setUserId(userId);
        user.setFirstName(firstName);
        user.setLastName(lastName);

        return user;
    }

    private void addGroupsToUser(JsonObject user) {
        JsonArray groupIds = new JsonArray();
        groupIds.add(2L);
        groupIds.add(3L);

        user.put(User.GROUPS_TO_ADD, groupIds);
    }

    private void addBunchOfGroups(JsonObject user) {
        // Create membership hierarchy.
        Vertex parent = addGroup("Parent", identity++);
        Vertex child1 = addGroup("Child 1", identity++);
        Vertex child2 = addGroup("Child 2", identity++);

        Vertex architects = addGroup("Architects", identity++);
        Vertex directors = addGroup("Directors", identity++);
        Vertex engineers = addGroup("Software Engineers", identity++);

        addMembership(child2, parent);
        addMembership(directors, child2);
        addMembership(architects, child1);
        addMembership(engineers, child1);

        for (Vertex resource : resources) {
            addPermission(child1, resource, 7);
            addPermission(engineers, resource, 3);
        }

        user.put(User.GROUPS_TO_ADD, new JsonArray().add(engineers.id()));
    }

    private void addPermissionsToUser(JsonObject user) {
        JsonArray permissionsToAdd = new JsonArray();
        permissionsToAdd.add(new Permission(resources.get(0).id(), "Studio", 7));
        permissionsToAdd.add(new Permission(resources.get(1).id(), "Console", 5));
        user.put(Permission.PERMISSIONS_TO_ADD, permissionsToAdd);
    }

    private void updateUserPermissions(JsonObject user) {
        JsonArray permissionsToUpdate = new JsonArray();
        permissionsToUpdate.add(new Permission(resources.get(0).id(), "Studio", 1));
        user.put(Permission.PERMISSIONS_TO_UPDATE, permissionsToUpdate);
    }

    private void removeUserPermissions(JsonObject user) {
        JsonArray permissionsToRemove = new JsonArray();
        permissionsToRemove.add(resources.get(1).id());
        user.put(Permission.PERMISSIONS_TO_REMOVE, permissionsToRemove);
    }

    private void assertUserAttributes(TestContext context, JsonObject usr, boolean testFullName) {
        User user = new User(usr);
        context.assertEquals("aragorn", user.getUserId(), "User Id not as expected.");
        context.assertEquals("Strider", user.getFirstName(), "First name not as expected.");
        context.assertEquals("Wingfoot", user.getLastName(), "Last name not as expected.");

        if (testFullName) {
            context.assertEquals("Strider Wingfoot", user.getFullName(), "Full name not as expected.");
        }
    }

    private void assertMemberships(TestContext context, JsonArray memberships, int expectedCount) {
        context.assertNotNull(memberships, "User memberships cannot be null.");
        context.assertEquals(expectedCount, memberships.size(), "User membership count is not as expected.");
    }

    private void assertMemberships(TestContext context, JsonArray memberships, Set<String> expectedMemberships) {
        context.assertNotNull(memberships, "User memberships cannot be null.");
        context.assertEquals(expectedMemberships.size(), memberships.size(),
                "User membership count is not as expected.");

        for (Object mbr : memberships) {
            Group group = new Group((JsonObject) mbr);
            context.assertTrue(expectedMemberships.contains(group.getGroupId()),
                    "Expected group not part of actual membership: " + group.getGroupId());
        }
    }

    private void assertDirectPermissions(TestContext context, JsonArray permissions) {
        context.assertNotNull(permissions, "User direct permissions cannot be null.");
        context.assertEquals(2, permissions.size(), "User should be granted two direct permissions.");
    }

    private void assertEffectivePermissions(TestContext context, JsonArray permissions,
                                            Map<String, Integer> expectedPermissions) {
        context.assertNotNull(permissions, "User effective permissions cannot be null.");

        if (expectedPermissions != null) {
            context.assertEquals(expectedPermissions.size(), permissions.size(),
                    "User should have 'n' effective permissions.");

            for (Object prm : permissions) {
                JsonObject permission = (JsonObject) prm;
                context.assertEquals(expectedPermissions.get(permission.getString(Resource.RESOURCE_ID)),
                        permission.getInteger(Permission.RWX),
                        "Read, write, execute grants do not match.");
            }
        }
    }

    private Vertex addGroup(String groupId, long id) {
        return graph.addVertex(T.id, id, T.label, Schema.V_GROUP, Group.GROUP_ID, groupId);
    }

    private void addMembership(Vertex member, Vertex group) {
        member.addEdge(Schema.E_MEMBER_OF, group);
        group.addEdge(Schema.E_MEMBER, member);
    }

    private void addPermission(Vertex subject, Vertex resource, int rwx) {
        subject.addEdge(Schema.E_HAS_PERMISSION, resource, Schema.E_P_RWX, rwx);
    }

    private void createGroups() {
        // Add groups.
        Vertex group1 = addGroup("Group 1", identity++);
        Vertex group2 = addGroup("Group 2", identity++);
        Vertex group3 = addGroup("Group 3", identity++);

        addMembership(group2, group1);
        addMembership(group3, group1);
    }
}

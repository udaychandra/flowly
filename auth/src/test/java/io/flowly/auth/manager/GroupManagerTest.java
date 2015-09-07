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
import io.flowly.core.security.Group;
import io.flowly.core.security.User;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a>Uday Tatiraju</a>
 */
@RunWith(VertxUnitRunner.class)
public class GroupManagerTest extends BaseManagerTest {
    private GroupManager groupManager;

    @Before
    public void setUp() {
        super.setUp();

        graph = TinkerGraph.open();
        groupManager = new GroupManager(graph);
        identity = 1L;

        graph.createIndex(Group.GROUP_ID, Vertex.class);
        createUsers();
    }

    @Test
    public void testCreateGroup(TestContext context) {
        context.assertTrue(groupManager.create(makeGroup()).size() == 0, "Assertion of simple group creation failed.");
        assertGroupAttributes(context, groupManager.get("Group 1"));
    }

    @Test
    public void testUpdateGroup(TestContext context) {
        Group group = makeGroup();
        group.put(Group.USERS_TO_ADD, new JsonArray().add(1L).add(2L).add(3L).add(4L));
        context.assertTrue(groupManager.create(group).size() == 0, "Assertion of simple group creation failed.");

        group = new Group(groupManager.get("Group 1"));
        group.setGroupId("Group 1 Update");
        group.put(Group.USERS_TO_ADD, new JsonArray().add(1L).add(3L).add(5L));
        group.put(Group.USERS_TO_REMOVE, new JsonArray().add(2L));
        context.assertTrue(groupManager.update(group).size() == 0, "Failed to update group.");
    }

    @Test
    public void testSearchAll(TestContext context) {
        Map<String, Integer> expectedGroups = new HashMap<>();

        for (int i=0; i<13; i++) {
            Group group = generateGroup("gid" + i, null);
            expectedGroups.put(group.getGroupId(), 0);
            context.assertTrue(groupManager.create(group).size() == 0);
        }

        JsonArray groups = groupManager.search(1, 10);
        context.assertTrue(groups.size() == 11, "Result of search all groups not as expected.");
    }

    // TODO: Permissions, Members, Memberships and Search.

    private void createUsers() {
        graph.addVertex(T.label, Schema.V_USER, T.id, identity++, Schema.V_P_USER_ID, "user1");
        graph.addVertex(T.label, Schema.V_USER, T.id, identity++, Schema.V_P_USER_ID, "user2");
        graph.addVertex(T.label, Schema.V_USER, T.id, identity++, Schema.V_P_USER_ID, "user3");
        graph.addVertex(T.label, Schema.V_USER, T.id, identity++, Schema.V_P_USER_ID, "user4");
        graph.addVertex(T.label, Schema.V_USER, T.id, identity++, Schema.V_P_USER_ID, "user5");
    }

    private void assertGroupAttributes(TestContext context, JsonObject group) {
        Group grp = new Group(group);
        context.assertEquals("Group 1", grp.getGroupId(), "Group Id not as expected.");
        context.assertEquals("Group 1 Description", grp.getDescription(), "Group description not as expected.");
    }

    private Group makeGroup() {
        return generateGroup("Group 1", "Group 1 Description");
    }

    private Group generateGroup(String groupId, String description) {
        Group group = new Group();
        group.setGroupId(groupId);

        if (description != null) {
            group.setDescription(description);
        }

        return group;
    }
}

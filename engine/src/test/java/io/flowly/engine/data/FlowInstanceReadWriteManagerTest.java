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

package io.flowly.engine.data;

import io.flowly.engine.BaseTestWithVertx;
import io.flowly.engine.JsonKeys;
import io.flowly.engine.data.manager.FlowInstanceReadManager;
import io.flowly.engine.data.manager.FlowlyGraph;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * @author <a>Uday Tatiraju</a>
 */
@RunWith(VertxUnitRunner.class)
public class FlowInstanceReadWriteManagerTest extends BaseTestWithVertx {
    private Graph graph;
    private FlowInstanceReadManager instanceManager;

    @Before
    public void setUp() {
        graph = new FlowlyGraph(new JsonObject().
                put(JsonKeys.DB_STORAGE_DIRECTORY, storageDirectory + File.separator + System.currentTimeMillis())).
                getInstance();
        MockFlowGraphGenerator.populateGraph(graph, 1, 20, 20);

        instanceManager = new FlowInstanceReadManager(graph);
    }

    @After
    public void tearDown(TestContext context) {
        try {
            graph.close();
        }
        catch (Exception ex) {
            context.fail(ex);
        }
    }

    @Test
    public void testGetInbox(TestContext context) {
        JsonObject inbox = instanceManager.getInbox("uday", 1, 10);
        context.assertNotNull(inbox, "Inbox cannot be null.");
        context.assertTrue(inbox.getInteger(JsonKeys.COUNT) > 0, "Number of tasks cannot be empty");
    }
}

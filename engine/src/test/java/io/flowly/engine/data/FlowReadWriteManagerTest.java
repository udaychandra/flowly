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
import io.flowly.engine.assets.ConnectingObject;
import io.flowly.engine.assets.EndEvent;
import io.flowly.engine.assets.Flow;
import io.flowly.engine.assets.FlowObject;
import io.flowly.engine.assets.Process;
import io.flowly.engine.assets.StartEvent;
import io.flowly.engine.data.manager.FlowReadWriteManager;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * @author <a>Uday Tatiraju</a>
 */
@RunWith(VertxUnitRunner.class)
public class FlowReadWriteManagerTest extends BaseTestWithVertx {
    private FlowReadWriteManager routerManager;

    @Before
    public void setUp() {
        super.setUp();
        routerManager = new FlowReadWriteManager(TinkerGraph.open());
    }

    @Test
    public void testMakeValidRouter(TestContext context) throws IOException {
        Flow flow = new Process("validFlow", "Test Process", null, null);

        FlowObject startEvent = new StartEvent("1", null, null, null);
        flow.addFlowObject(startEvent);

        FlowObject endEvent = new EndEvent("3", null, null, null);
        flow.addFlowObject(endEvent);

        ConnectingObject connectingObject = new ConnectingObject("2", null, null, null, "1", "3");
        flow.addConnectingObject(connectingObject);

        context.assertTrue(routerManager.saveRouter(flow), "Making the router table for a flow should not fail");
    }

    @Test
    public void testMakeNoStartEventInValidRouter(TestContext context) throws IOException {
        Flow flow = new Process("invalidFlow", null, null, null);

        FlowObject endEvent = new EndEvent("3", null, null, null);
        flow.addFlowObject(endEvent);

        ConnectingObject connectingObject = new ConnectingObject("2", null, null, null, "1", "3");
        flow.addConnectingObject(connectingObject);

        context.assertFalse(routerManager.saveRouter(flow), "Making the router table for an invalid flow should fail");
    }

    @Test
    public void testMakeNoEndEventInValidRouter(TestContext context) throws IOException {
        Flow flow = new Process("invalidFlow", null, null, null);

        FlowObject startEvent = new StartEvent("1", null, null, null);
        flow.addFlowObject(startEvent);

        ConnectingObject connectingObject = new ConnectingObject("2", null, null, null, "1", "3");
        flow.addConnectingObject(connectingObject);

        context.assertFalse(routerManager.saveRouter(flow), "Making the router table for a flow should not fail");
    }
}
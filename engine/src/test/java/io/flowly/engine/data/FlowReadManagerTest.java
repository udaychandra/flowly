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

import io.flowly.core.data.FlowInstanceMetadata;
import io.flowly.core.data.FlowInstanceStep;
import io.flowly.engine.BaseTestWithVertx;
import io.flowly.engine.assets.InteractiveService;
import io.flowly.engine.assets.Process;
import io.flowly.engine.data.manager.FlowReadWriteManager;
import io.flowly.core.parser.Parser;
import io.flowly.engine.parser.AssetParser;
import io.flowly.engine.router.Route;
import io.flowly.engine.utils.PathUtils;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * @author <a>Uday Tatiraju</a>
 */
@RunWith(VertxUnitRunner.class)
public class FlowReadManagerTest extends BaseTestWithVertx {
    private FlowReadWriteManager routerManager;
    private FlowInstanceMetadata metadata;
    private Parser parser;

    private String processFilePath = PathUtils.createPathWithPrefix(APPS_FOLDER,
            APP_2_ID.replace(PathUtils.DOT, File.separator), PathUtils.PROCESSES_FOLDER, "1429373120533.json");
    private String serviceFilePath = PathUtils.createPathWithPrefix(APPS_FOLDER,
            APP_2_ID.replace(PathUtils.DOT, File.separator), PathUtils.INTERACTIVE_SERVICES_FOLDER,
            "1234123412340.json");

    @Before
    public void setUp(TestContext context) {
        super.setUp();

        routerManager = new FlowReadWriteManager(TinkerGraph.open());
        processFilePath = getClass().getResource(processFilePath).getPath();
        metadata = new FlowInstanceMetadata();
        metadata.setCurrentStep(new FlowInstanceStep());
        Async async = context.async();
        parser = new AssetParser(vertx.fileSystem());

        parser.parse(processFilePath, Process.class, res -> {
            if (res.succeeded()) {
                Process process = res.result();
                context.assertTrue(routerManager.saveRouter(process));
                async.complete();
            }
            else {
                context.fail(res.cause());
            }

        });
    }

    @Test
    public void testStartRoute(TestContext context) {
        metadata.setFlowId("1429373120533");
        metadata.getCurrentStep().setFlowObjectId("0");

        Route route = routerManager.nextRoute(metadata);
        context.assertTrue(route.isValid(), "Route should be valid.");
        context.assertFalse(route.isEnd(), "Route should not end.");
        context.assertEquals(1, route.getNextList().size(), "Next route count is not as expected.");
        context.assertEquals("1001", route.getNext().getFlowObjectId(), "Next route not as expected.");
    }

    @Test
    public void testDecisionGatewayRoute(TestContext context) {
        metadata.setFlowId("1429373120533");
        metadata.getCurrentStep().setFlowObjectId("1005");
        metadata.getCurrentStep().addConnectingObjectId("1006");

        Route route = routerManager.nextRoute(metadata);
        context.assertTrue(route.isValid(), "Route should be valid.");
        context.assertFalse(route.isEnd(), "Route should not end.");
        context.assertEquals(1, route.getNextList().size(), "Next route count is not as expected.");
        context.assertEquals("1007", route.getNext().getFlowObjectId(), "Next route not as expected.");
    }

    @Test
    public void testEndRoute(TestContext context) {
        metadata.setFlowId("1429373120533");
        metadata.getCurrentStep().setFlowObjectId("1009");

        Route route = routerManager.nextRoute(metadata);
        context.assertTrue(route.isValid(), "Route should be valid.");
        context.assertTrue(route.isEnd(), "Route should end.");
        context.assertEquals(0, route.getNextList().size(), "Next route count is not as expected.");
    }

    @Test
    public void testInvalidRoute(TestContext context) {
        metadata.setFlowId("invalid");
        metadata.getCurrentStep().setFlowObjectId("222");

        Route route = routerManager.nextRoute(metadata);
        context.assertFalse(route.isValid(), "Route should not be valid.");
    }

    @Test
    public void testServiceRoute(TestContext context) {
        Async async = context.async();

        serviceFilePath = getClass().getResource(serviceFilePath).getPath();
        parser.parse(serviceFilePath, InteractiveService.class, res -> {
            if (res.succeeded()) {
                InteractiveService service = res.result();
                context.assertTrue(routerManager.saveRouter(service));

                metadata.setFlowId("1234123412340");
                metadata.getCurrentStep().setFlowObjectId("3001");

                Route route = routerManager.nextRoute(metadata);
                context.assertTrue(route.isValid(), "Route should be valid.");
                context.assertFalse(route.isEnd(), "Route should not end.");
                context.assertEquals(1, route.getNextList().size(), "Next route count is not as expected.");
                context.assertEquals("888999000", route.getNext().getSubFlowId(), "Next route sub flow is not as expected.");

                async.complete();
            }
            else {
                context.fail(res.cause());
            }
        });
    }
}
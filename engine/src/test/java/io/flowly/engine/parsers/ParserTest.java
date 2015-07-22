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

package io.flowly.engine.parsers;

import io.flowly.core.parser.JsonParser;
import io.flowly.engine.App;
import io.flowly.engine.BaseTestWithVertx;
import io.flowly.engine.parser.AssetParser;
import io.flowly.engine.assets.Flow;
import io.flowly.engine.assets.Process;
import io.flowly.engine.utils.PathUtils;
import io.flowly.engine.assets.InteractiveService;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * @author <a>Uday Tatiraju</a>
 */
@RunWith(VertxUnitRunner.class)
public class ParserTest extends BaseTestWithVertx {
    @Test
    public void testParseProcess(TestContext context) {
        assertParsedFlow(context, "process.json", Process.class, 3, 4, 3, 1);
    }

    @Test
    public void testParseInteractiveService(TestContext context) {
        assertParsedFlow(context, "interactive_service.json", InteractiveService.class, 4, 4, 3, -1);
    }

    @Test
    public void testParseMicroService(TestContext context) {
        assertParsedFlow(context, "micro_service.json", InteractiveService.class, 5, 6, 6, -1);
    }

    @Test
    public void testApp(TestContext context) {
        Async async = context.async();
        String appConfigFilePath = getClass().getResource(File.separator + "app.json").getPath();

        new JsonParser(vertx.fileSystem()).parse(appConfigFilePath, App.class, handler -> {
            App app = (App) handler.result();
            context.assertTrue(app.getId().equals(APP_1_ID), "App id is not as expected.");
            async.complete();
        });
    }

    private <T> void assertParsedFlow(TestContext context, String flowName, Class<T> flowType, int expectedVariables,
                                    int expectedFlowObjects, int expectedConnectingObjects, int expectedSwimLanes) {
        Async async = context.async();
        String flowPath = getFlowPath(flowName);

        new AssetParser(vertx.fileSystem()).parse(flowPath, flowType, handler -> {
            if (handler.succeeded()) {
                Flow flow = (Flow) handler.result();

                context.assertEquals(expectedVariables, flow.getVariables().size(),
                        "Number of variables in the flow do not match the expected count - " +
                                flowType.getSimpleName());

                context.assertEquals(expectedFlowObjects, flow.getFlowObjects().size(),
                        "Number of flow objects in the flow do not match the expected count - " +
                                flowType.getSimpleName());

                context.assertEquals(expectedConnectingObjects, flow.getConnectingObjects().size(),
                        "Number of connecting objects in the flow do not match the expected count - " +
                                flowType.getSimpleName());

                if (expectedSwimLanes > -1 && flow instanceof Process) {
                    context.assertEquals(expectedSwimLanes, ((Process) flow).getSwimLanes().size(),
                            "Number of swimlanes in the flow do not match the expected count - " +
                                    flowType.getSimpleName());
                }

                async.complete();
            }
            else {
                context.fail(handler.cause());
            }
        });
    }

    private String getFlowPath(String flowName) {
        String path = PathUtils.createPathWithPrefix("flows", flowName);
        return getClass().getResource(path).getPath();
    }
}
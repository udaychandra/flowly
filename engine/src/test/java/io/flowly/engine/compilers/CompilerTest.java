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

package io.flowly.engine.compilers;

import io.flowly.engine.App;
import io.flowly.engine.BaseTestWithVertx;
import io.flowly.engine.assets.Flow;
import io.flowly.engine.assets.InteractiveService;
import io.flowly.engine.parser.AssetParser;
import io.flowly.engine.utils.PathUtils;
import io.flowly.engine.assets.Process;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

@RunWith(VertxUnitRunner.class)
/**
 * @author <a>Uday Tatiraju</a>
 */
public class CompilerTest extends BaseTestWithVertx {
    @Test
    public void testCompileProcess() throws IOException {
        String processPath = getFlowPath(APP_1_ID, PathUtils.PROCESSES_FOLDER, "2429373120533.json");
        String expectedCompiledProcessPath = getCompiledJavaScriptFilePath("process_simple_one.js");
        assertCompileFlow(processPath, Process.class, APP_1_ID, expectedCompiledProcessPath);
    }

    @Test
    public void testCompileProcessWithDecisionGateway() throws IOException {
        String processPath = getFlowPath(APP_2_ID, PathUtils.PROCESSES_FOLDER, "1429373120533.json");
        String expectedCompiledProcessPath = getCompiledJavaScriptFilePath("process_with_decision_gateway.js");
        assertCompileFlow(processPath, Process.class, APP_2_ID, expectedCompiledProcessPath);
    }

    @Test
    public void testCompileInteractiveService() {
        String servicePath = getFlowPath(APP_2_ID, PathUtils.INTERACTIVE_SERVICES_FOLDER, "1234123412340.json");
        String expectedCompiledServicePath = getCompiledJavaScriptFilePath("interactive_service_one.js");
        assertCompileFlow(servicePath, InteractiveService.class, APP_2_ID, expectedCompiledServicePath);
    }

    @Test
    public void testCompileProcessWithSubFlow() throws IOException {
        String processPath = getFlowPath(APP_2_ID, PathUtils.PROCESSES_FOLDER, "1429373120550.json");
        String expectedCompiledProcessPath = getCompiledJavaScriptFilePath("process_with_micro_service_as_sub_flow.js");
        assertCompileFlow(processPath, Process.class, APP_2_ID, expectedCompiledProcessPath);
    }

    @Test
    public void testCompileProcessWithInteractiveService() {
        String processPath = getFlowPath(APP_2_ID, PathUtils.PROCESSES_FOLDER, "1429373120535.json");
        String expectedCompiledProcessPath = getCompiledJavaScriptFilePath("process_with_interactive_service.js");
        assertCompileFlow(processPath, Process.class, APP_2_ID, expectedCompiledProcessPath);
    }

    private <T> void assertCompileFlow(String flowFilePath, Class<T> flowType, String appId,
                                       String expectedCompiledFlowFileName) {
        // Parse the flow.
        Flow flow = (Flow) new AssetParser(vertx.fileSystem()).parseBlocking(flowFilePath, flowType);
        App app = new App(appId, appsDirectory);
        flow.setApp(app);

        // Compile flow object.
        Compiler compiler = new VerboseCompiler(vertx.fileSystem());
        StringBuilder output = new StringBuilder();
        flow.compile(compiler, null, output);
        compiler.writeFlow(flow, output);

        // Compare expected and actual verticle scripts.
        Buffer buffer = vertx.fileSystem().readFileBlocking(expectedCompiledFlowFileName);

        Assert.assertArrayEquals("Actual compiled verticle is not the same as expected verticle.",
                buffer.getBytes(), Buffer.buffer(output.toString()).getBytes());
    }

    private String getFlowPath(String appId, String flowFolder, String flowFileName) {
        String flowPath = PathUtils.createPathWithPrefix(flowFileName == null, APPS_FOLDER,
                appId.replace(PathUtils.DOT, File.separator), flowFolder, flowFileName);
        return getClass().getResource(flowPath).getPath();
    }
}
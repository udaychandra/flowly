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

import io.flowly.engine.assets.DecisionGateway;
import io.flowly.engine.assets.EndEvent;
import io.flowly.engine.assets.Flow;
import io.flowly.engine.assets.InlineScript;
import io.flowly.engine.assets.InteractiveService;
import io.flowly.engine.assets.MicroService;
import io.flowly.engine.assets.Process;
import io.flowly.engine.assets.StartEvent;
import io.flowly.engine.assets.SubFlow;
import io.flowly.engine.assets.View;
import io.flowly.engine.utils.PathUtils;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;

/**
 * Compiles assets in verbose mode.
 * Useful for debugging during development of flowly apps.
 *
 * @author <a>Uday Tatiraju</a>
 */
// TODO: Pretty print javascript output.
public class VerboseCompiler implements Compiler {
    private FileSystem fileSystem;

    public VerboseCompiler(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    public void compile(Process process, Flow flow, StringBuilder output) {
        CompileProcess.compile(process, output);
    }

    @Override
    public void compile(StartEvent startEvent, Flow flow, StringBuilder output) {
        CompileStartEvent.compile(startEvent, flow, output);
    }

    @Override
    public void compile(EndEvent endEvent, Flow flow, StringBuilder output) {
        CompileEndEvent.compile(endEvent, flow, output);
    }

    @Override
    public void compile(InlineScript inlineScript, Flow flow, StringBuilder output) {
        CompileInlineScript.compile(inlineScript, flow, output, fileSystem, flow.getApp().getAppFolder());
    }

    @Override
    public void compile(SubFlow subFlow, Flow flow, StringBuilder output) {
        CompileSubFlow.compile(subFlow, flow, output);
    }

    @Override
    public void compile(DecisionGateway decisionGateway, Flow flow, StringBuilder output) {
        CompileDecisionGateway.compile(decisionGateway, flow, output);
    }

    @Override
    public void compile(InteractiveService interactiveService, Flow flow, StringBuilder output) {
        CompileInteractiveService.compile(interactiveService, output);
    }

    @Override
    public void compile(MicroService microService, Flow flow, StringBuilder output) {
        CompileMicroService.compile(microService, output);
    }

    @Override
    public void compile(View view, Flow flow, StringBuilder output) {
        CompileView.compile(view, flow, output);
    }

    @Override
    public String writeFlow(Flow flow, StringBuilder output) {
        String verticlesPath = PathUtils.createPath(flow.getApp().getAppFolder(), PathUtils.VERTICLES_FOLDER);
        String scriptPath = PathUtils.createPath(verticlesPath, flow.getId() + PathUtils.DOT_JS_SUFFIX);

        if (!fileSystem.existsBlocking(verticlesPath)) {
            fileSystem.mkdirBlocking(verticlesPath);
        }

        if (fileSystem.existsBlocking(scriptPath)) {
            fileSystem.deleteBlocking(scriptPath);
        }

        fileSystem.createFileBlocking(scriptPath);
        fileSystem.writeFileBlocking(scriptPath, Buffer.buffer(output.toString()));

        return scriptPath;
    }
}

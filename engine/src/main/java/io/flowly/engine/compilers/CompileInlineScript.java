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

import io.flowly.engine.assets.Flow;
import io.flowly.engine.assets.InlineScript;
import io.flowly.engine.utils.PathUtils;
import io.flowly.engine.App;
import io.vertx.core.file.FileSystem;

/**
 * Compiles an inline script into JavaScript.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class CompileInlineScript {
    public static void compile(InlineScript inlineScript, Flow flow, StringBuilder output,
                               FileSystem fileSystem, String appFolderPath) {
        Compiler.start(output, flow.getApp().getId(), inlineScript.getId());

        Compiler.loadInstance(output);
        loadScript(output, inlineScript.getScriptRefId(), fileSystem, appFolderPath);

        Compiler.hop(output, null);
        Compiler.end(output);
    }

    private static void loadScript(StringBuilder output, String scriptId, FileSystem fileSystem, String appFolderPath) {
        // Empty script.
        if (scriptId == null || scriptId.length() == 0) {
            return;
        }

        output.append(Compiler.LINE_BREAK);
        output.append("    /* ############### Begin inline script: ").append(scriptId).append(" ############### */");
        output.append(Compiler.LINE_BREAK);

        String scriptPath = PathUtils.createPath(appFolderPath,
                PathUtils.SCRIPTS_FOLDER, scriptId + PathUtils.DOT_JS_SUFFIX);
        output.append(new String(fileSystem.readFileBlocking(scriptPath).getBytes()));

        output.append(Compiler.LINE_BREAK);
        output.append("    /* ############### End inline script ############### */");
        output.append(Compiler.LINE_BREAK);
        output.append(Compiler.LINE_BREAK);
    }
}

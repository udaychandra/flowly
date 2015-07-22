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
import io.flowly.engine.assets.StartEvent;

/**
 * Compiles a start event into JavaScript.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class CompileStartEvent {
    public static void compile(StartEvent startEvent, Flow flow, StringBuilder output) {
        Compiler.start(output, flow.getApp().getId(), startEvent.getId());

        Compiler.loadInstance(output);
        output.append("    var inputData = instance.inputData;");
        output.append(Compiler.LINE_BREAK);

        output.append("    if (inputData) {");
        output.append(Compiler.LINE_BREAK);
        Compiler.mapInputData(output, flow.getVariables());
        output.append(Compiler.LINE_BREAK);
        // Clear input data
        output.append("    instance.inputData = null;");
        output.append(Compiler.LINE_BREAK);
        output.append("    }");

        output.append(Compiler.LINE_BREAK);
        output.append(Compiler.LINE_BREAK);

        Compiler.hop(output, null);
        Compiler.end(output);
    }
}

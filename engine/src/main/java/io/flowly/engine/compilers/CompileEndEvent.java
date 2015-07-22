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

import io.flowly.core.data.FlowInstance;
import io.flowly.engine.JsonKeys;
import io.flowly.engine.assets.EndEvent;
import io.flowly.engine.assets.Flow;

/**
 * Compiles an end event into JavaScript.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class CompileEndEvent {
    public static void compile(EndEvent endEvent, Flow flow, StringBuilder output) {
        Compiler.start(output, flow.getApp().getId(), endEvent.getId());

        Compiler.loadInstance(output);
        if (Compiler.createOutputData(output, flow.getVariables())) {
            output.append(Compiler.TAB_BREAK).append(JsonKeys.INSTANCE).append(Compiler.DOT).
                    append(FlowInstance.OUTPUT_DATA).append(" = outputData;");
            output.append(Compiler.LINE_BREAK);
            output.append(Compiler.LINE_BREAK);
        }

        Compiler.hop(output, null);
        Compiler.end(output);
    }
}

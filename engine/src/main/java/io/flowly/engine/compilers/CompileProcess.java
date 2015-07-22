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

import io.flowly.engine.assets.Process;

/**
 * Compiles a process flow into JavaScript.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class CompileProcess {
    public static void compile(Process process, StringBuilder output) {
        output.append("var eb = vertx.eventBus();");
        output.append(Compiler.LINE_BREAK);
        output.append(Compiler.LINE_BREAK);
    }
}

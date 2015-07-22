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

import io.flowly.engine.assets.StartEvent;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
/**
 * @author <a>Uday Tatiraju</a>
 */
public class CompileStartEventTest extends BaseCompilerTest {
    @Test
    public void compileStartEventWithNoInputTest() {
        StartEvent startEvent = new StartEvent("1001", "Start", null, null);

        CompileStartEvent.compile(startEvent, flow, output);
        assertCompiler("start_event.js");
    }
}

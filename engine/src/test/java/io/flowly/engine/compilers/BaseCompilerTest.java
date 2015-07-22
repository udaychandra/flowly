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
import io.flowly.engine.assets.Process;
import io.vertx.core.buffer.Buffer;
import org.junit.Assert;
import org.junit.Before;

/**
 * @author <a>Uday Tatiraju</a>
 */
public abstract class BaseCompilerTest extends BaseTestWithVertx {
    protected Flow flow;
    protected StringBuilder output;

    @Before
    public void setup() {
        flow = new Process();
        flow.setApp(new App(APP_1_ID, appsDirectory));

        output = new StringBuilder();
    }

    protected void assertCompiler(String compareWithJavaScriptInFile) {
        String path = getCompiledJavaScriptFilePath(compareWithJavaScriptInFile);

        // Compare expected and actual JavaScripts.
        Buffer buffer = vertx.fileSystem().readFileBlocking(path);

        Assert.assertArrayEquals("Actual compiled JavaScript is not the same as expected JavaScript.",
                buffer.getBytes(), Buffer.buffer(output.toString()).getBytes());
    }
}

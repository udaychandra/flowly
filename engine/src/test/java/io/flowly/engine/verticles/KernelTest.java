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

package io.flowly.engine.verticles;

import io.vertx.ext.unit.TestContext;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a>Uday Tatiraju</a>
 */
@RunWith(VertxUnitRunner.class)
public class KernelTest extends BaseVerticleTest {
    @Before
    public void before() {
        super.before();
    }

    @Test
    public void testDeployApp(TestContext context) {
        deployKernelVerticle(context, null);
    }
}
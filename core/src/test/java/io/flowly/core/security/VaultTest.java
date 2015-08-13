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

package io.flowly.core.security;

import io.flowly.core.BaseTestWithVertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 * @author <a>Uday Tatiraju</a>
 */
@RunWith(VertxUnitRunner.class)
public class VaultTest extends BaseTestWithVertx {
    String rootPath = getClass().getResource(File.separator).getPath();

    @Test
    public void saveDataTest(TestContext context) {
        String storePath = rootPath + "test.p12";
        Vault vault = new Vault("secret", storePath, vertx.fileSystem());

        context.assertTrue(vault.saveData("key", "value"), "Data should be saved to the Vault.");
    }

    @Test
    public void getDataTest(TestContext context) {
        String storePath = rootPath + "test.p12";
        Vault vault = new Vault("secret", storePath, vertx.fileSystem());

        context.assertTrue(vault.saveData("key", "value"), "Data should be saved to the Vault.");
        context.assertEquals("value", vault.getData("key"), "Data retrieved from the Vault is not as expected.");
    }
}

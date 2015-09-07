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
    private String path = getClass().getResource(File.separator).getPath() +
            File.separator + System.currentTimeMillis();

    @Test
    public void saveDataTest(TestContext context) throws Exception {
        Vault vault = new Vault("test".toCharArray(), path, vertx.fileSystem());

        context.assertTrue(vault.saveData("key", "value".toCharArray()), "Data should be saved to the vault.");
    }

    @Test
    public void getDataTest(TestContext context) throws Exception {
        Vault vault = new Vault("test".toCharArray(), path, vertx.fileSystem());

        context.assertTrue(vault.saveData("key", "value".toCharArray()), "Data should be saved to the vault.");
        context.assertEquals("value", new String(vault.getData("key")),
                "Data retrieved from the vault is not as expected.");
    }
}

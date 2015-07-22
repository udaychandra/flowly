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

package io.flowly.engine.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * @author <a>Uday Tatiraju</a>
 */
public class PathUtilsTest {
    @Test
    public void testCreatePathWithPrefix() {
        String expectedPath = File.separator + "token1" + File.separator + "token2";
        String actualPath = PathUtils.createPathWithPrefix("token1", "token2");
        Assert.assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testCreatePathWithPrefixSkipNull() {
        String expectedPath = File.separator + "token1" + File.separator + "token2";
        String actualPath = PathUtils.createPathWithPrefix(true, "token1", null, "token2");
        Assert.assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testCreatePathWithPrefixSkipEmpty() {
        String expectedPath = File.separator + "token1" + File.separator + "token2";
        String actualPath = PathUtils.createPathWithPrefix(true, "token1", "", "", "token2", "");
        Assert.assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testCreatePath() {
        String expectedPath = "token1" + File.separator + "token2";
        String actualPath = PathUtils.createPath("token1", "token2");
        Assert.assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testCreatePathSkipNull() {
        String expectedPath = "token1" + File.separator + "token2";
        String actualPath = PathUtils.createPath(true, "token1", null, "token2");
        Assert.assertEquals(expectedPath, actualPath);
    }

    @Test
    public void testCreatePathSkipEmpty() {
        String expectedPath = "token1" + File.separator + "token2";
        String actualPath = PathUtils.createPath(true, "token1", "", "", "token2", "");
        Assert.assertEquals(expectedPath, actualPath);
    }
}

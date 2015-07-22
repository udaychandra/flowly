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

package io.flowly.engine;

import io.flowly.engine.utils.PathUtils;

import java.io.File;

/**
 * Base class that defines general functionality that are used by
 * all engine unit tests.
 *
 * @author <a>Uday Tatiraju</a>
 */
public abstract class BaseTest {
    public static final String DATABASE_FOLDER = "databases";
    public static final String APPS_FOLDER = "apps";
    public static final String APP_1_ID = "com.test.app1";
    public static final String APP_2_ID = "com.test.app2";

    // Absolute path to the storage root.
    protected String storageDirectory = BaseTest.class.getResource(File.separator + DATABASE_FOLDER).getPath();

    // Absolute path to apps root folder.
    protected String appsDirectory = BaseTest.class.getResource(File.separator + APPS_FOLDER).getPath();

    protected String getCompiledJavaScriptFilePath(String compiledFileName) {
        String path = PathUtils.createPathWithPrefix("flows", "compiled", compiledFileName);
        return getClass().getResource(path).getPath();
    }
}

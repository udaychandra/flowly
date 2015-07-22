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

package io.flowly.core.parser;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.file.FileSystem;

/**
 * Parses JSON by reading files and converts JSON into POJOs.
 *
 * @author <a>Uday Tatiraju</a>
 */
public interface Parser {
    /**
     * Retrieves the JSON file based on the provided path, parses the JSON file and populates
     * an instance of the given class with the JSON data.
     *
     * @param filePath path to the JSON file.
     * @param classOfT the class used to instantiate an object and populate it with the parsed data.
     * @param resultHandler callback that is to be invoked after the object is populated with JSON data.
     */
    <T extends Object> void parse(String filePath, Class<T> classOfT, Handler<AsyncResult<T>> resultHandler);

    /**
     * Retrieves the JSON file based on the provided path, parses the JSON file and populates
     * an instance of the given class with the JSON data. This is the blocking version
     * of {@link #parse(String, Class, Handler)}.
     *
     * @param filePath path to the JSON file.
     * @param classOfT the class used to instantiate an object and populate it with the parsed data.
     */
    <T extends Object> T parseBlocking(String filePath, Class<T> classOfT);
}

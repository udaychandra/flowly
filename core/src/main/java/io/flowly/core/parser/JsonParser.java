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

import com.google.gson.Gson;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;

/**
 * Parser that reads JSON from given files and converts the JSON into specified class instance.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class JsonParser implements Parser {
    private FileSystem fileSystem;
    private Gson gson;

    public JsonParser(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
        this.gson = new Gson();
    }

    public JsonParser(FileSystem fileSystem, Gson gson) {
        this.fileSystem = fileSystem;
        this.gson = gson;
    }

    @Override
    public <T extends Object> void parse(String filePath, Class<T> classOfT, Handler<AsyncResult<T>> resultHandler) {
        Future<T> future = Future.future();
        future.setHandler(resultHandler);

        fileSystem.readFile(filePath, res -> {
            if (res.succeeded()) {
                T asset = parse(res.result(), classOfT);
                future.complete(asset);
            }
            else {
                future.fail(res.cause());
            }
        });
    }

    @Override
    public <T extends Object> T parseBlocking(String filePath, Class<T> classOfT) {
        return parse(fileSystem.readFileBlocking(filePath), classOfT);
    }

    private <T extends Object> T parse(Buffer buffer, Class<T> classOfT) {
        return gson.fromJson(new String(buffer.getBytes()), classOfT);
    }
}
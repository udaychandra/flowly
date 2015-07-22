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

package io.flowly.engine.parser;

import com.google.gson.GsonBuilder;
import io.flowly.core.parser.JsonParser;
import io.flowly.engine.assets.FlowObject;
import io.vertx.core.file.FileSystem;

public class AssetParser extends JsonParser {
    public AssetParser(FileSystem fileSystem) {
        super(fileSystem,
                new GsonBuilder().registerTypeAdapter(FlowObject.class, new FlowObjectTypeAdapter()).create());
    }
}

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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.flowly.engine.assets.FlowObject;

import java.lang.reflect.Type;

/**
 * Adapter to deserialize {@link io.flowly.engine.assets.FlowObject} objects.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class FlowObjectTypeAdapter implements JsonDeserializer<FlowObject> {
    @Override
    public FlowObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        String type = json.getAsJsonObject().get("type").getAsString();
        return context.deserialize(json, getFlowObjectType(type));
    }

    private Type getFlowObjectType(String type) {
        try {
            return Class.forName("io.flowly.engine.assets." + type);
        }
        catch (ClassNotFoundException e) {
            // This will throw an error anyway since it's an interface.
            return FlowObject.class;
        }
    }
}

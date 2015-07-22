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

package io.flowly.core.data;

import io.vertx.core.json.JsonObject;

import java.util.Map;

/**
 * Represents everything about a flow instance - data, input data, output data and metadata.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class FlowInstance extends JsonObject {
    // Keys.
    public static final String META_DATA = "metaData";
    public static final String DATA = "data";
    public static final String INPUT_DATA = "inputData";
    public static final String OUTPUT_DATA = "outputData";

    public FlowInstance() {
        super();
        setData(new JsonObject());
    }

    public FlowInstance(String json) {
        super(json);
        initialize();
    }

    public FlowInstance(Map<String, Object> map) {
        super(map);
        initialize();
    }

    @SuppressWarnings("unchecked")
    private void initialize() {
        if (containsKey(META_DATA)) {
            Object metadata = getMap().get(META_DATA);
            Map metadataMap = (metadata instanceof JsonObject) ? ((JsonObject) metadata).getMap() : (Map) metadata;
            setMetadata(new FlowInstanceMetadata(metadataMap));
        }

        if (getData() == null) {
            setData(new JsonObject());
        }
    }

    public FlowInstance(Long instanceId, FlowMetadata flowMetadata) {
        this();

        FlowInstanceMetadata metadata = new FlowInstanceMetadata(flowMetadata.getMap());
        metadata.setInstanceId(instanceId);
        setMetadata(metadata);
    }

    public FlowInstanceMetadata getMetadata() {
        return (FlowInstanceMetadata) getMap().get(META_DATA);
    }

    public void setMetadata(FlowInstanceMetadata metadata) {
        put(META_DATA, metadata);
    }

    public JsonObject getData() {
        return getJsonObject(DATA);
    }

    public void setData(JsonObject data) {
        getMap().put(DATA, data);
    }

    public JsonObject getInputData() {
        return getJsonObject(INPUT_DATA);
    }

    public void setInputData(JsonObject inputData) {
        getMap().put(INPUT_DATA, inputData);
    }

    public JsonObject getOutputData() {
        return getJsonObject(OUTPUT_DATA);
    }

    public void setOutputData(JsonObject outputData) {
        getMap().put(OUTPUT_DATA, outputData);
    }
}

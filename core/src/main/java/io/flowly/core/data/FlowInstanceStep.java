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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;

/**
 * Represents a step (flow object metadata) in a given flow instance.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class FlowInstanceStep extends JsonObject {
    // Keys.
    public static final String _FLOW_OBJECT_ID = "_flowObjectId_";
    public static final String _FLOW_OBJECT_INSTANCE_ID = "_flowObjectInstanceId_";
    public static final String _FLOW_OBJECT_CONNECTING_OBJECT_IDS = "_flowObjectConnectingObjectIds_";
    public static final String STEP_INDEX = "stepIndex";
    public static final String _SUB_FLOW_ID = "_subFlowId_";

    public FlowInstanceStep() {
        super();
    }

    public FlowInstanceStep(Map<String, Object> map) {
        super(map);
    }

    public String getFlowObjectId() {
        return getString(_FLOW_OBJECT_ID);
    }

    public void setFlowObjectId(String currentFlowObjectId) {
        put(_FLOW_OBJECT_ID, currentFlowObjectId);
    }

    public Long getFlowObjectInstanceId() {
        return getLong(_FLOW_OBJECT_INSTANCE_ID);
    }

    public void setFlowObjectInstanceId(Long flowObjectInstanceId) {
        put(_FLOW_OBJECT_INSTANCE_ID, flowObjectInstanceId);
    }

    public int getStepIndex() {
        return getInteger(STEP_INDEX, -1);
    }

    public void setStepIndex(int currentStepIndex) {
        put(STEP_INDEX, currentStepIndex);
    }

    public String getSubFlowId() {
        return getString(_SUB_FLOW_ID);
    }

    public void setSubFlowId(String subFlowId) {
        put(_SUB_FLOW_ID, subFlowId);
    }

    @SuppressWarnings("unchecked")
    public List<String> getConnectingObjectIds() {
        JsonArray connectingObjectIds = getJsonArray(_FLOW_OBJECT_CONNECTING_OBJECT_IDS);
        return connectingObjectIds != null ? connectingObjectIds.getList() : null;
    }

    public void addConnectingObjectId(String connectingObjectId) {
        JsonArray connectingObjectIds = getJsonArray(_FLOW_OBJECT_CONNECTING_OBJECT_IDS);

        if (connectingObjectIds == null) {
            connectingObjectIds = new JsonArray();
            put(_FLOW_OBJECT_CONNECTING_OBJECT_IDS, connectingObjectIds);
        }

        connectingObjectIds.add(connectingObjectId);
    }
}

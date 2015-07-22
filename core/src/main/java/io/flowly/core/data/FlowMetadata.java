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
import java.util.Objects;

/**
 * Represents the metadata of a flow.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class FlowMetadata extends JsonObject {
    // Keys.
    public static final String FLOW_ID = "flowId";
    public static final String APP_ID = "appId";
    public static final String FLOW_TYPE = "flowType";

    protected static final String FLOW_TYPE_PROCESS = "Process";

    public FlowMetadata() {
        super();
    }

    public FlowMetadata(String json) {
        super(json);
    }

    public FlowMetadata(Map<String, Object> map) {
        super(map);
    }

    public String getFlowId() {
        return getString(FLOW_ID);
    }

    public void setFlowId(String flowId) {
        put(FLOW_ID, flowId);
    }

    public String getFlowType() {
        return getString(FLOW_TYPE);
    }

    public void setFlowType(String type) {
        put(FLOW_TYPE, type);
    }

    public String getAppId() {
        return getString(APP_ID);
    }

    public void setAppId(String appId) {
        put(APP_ID, appId);
    }

    public boolean persistenceEnabled() {
        String flowType = getFlowType();
        return flowType != null && flowType.equals(FLOW_TYPE_PROCESS);
    }

    public void validate() {
        Objects.requireNonNull(getFlowId(), "Flow id cannot be null.");
        Objects.requireNonNull(getAppId(), "App id cannot be null.");
    }
}

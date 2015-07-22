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
 * Holds the metadata of a flow's instance.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class FlowInstanceMetadata extends FlowMetadata {
    // Keys.
    public static final String INSTANCE_ID = "instanceId";
    public static final String _PARENT_FLOW_OBJECT_ID = "_parentFlowObjectId_";
    public static final String _PARENT_FLOW_OBJECT_INSTANCE_ID = "_parentFlowObjectInstanceId_";
    public static final String CURRENT_STEP = "currentStep";

    public FlowInstanceMetadata() {
        super();
    }

    public FlowInstanceMetadata(String json) {
        super(json);
        initialize();
    }


    public FlowInstanceMetadata(Map<String, Object> map) {
        super(map);
        initialize();
    }

    public Long getInstanceId() {
        return getLong(INSTANCE_ID);
    }

    public void setInstanceId(Long instanceId) {
        put(INSTANCE_ID, instanceId);
    }

    public String getParentFlowObjectId() {
        return getString(_PARENT_FLOW_OBJECT_ID);
    }

    public void setParentFlowObjectId(String parentFlowObjectId) {
        put(_PARENT_FLOW_OBJECT_ID, parentFlowObjectId);
    }

    public Long getParentFlowObjectInstanceId() {
        return getLong(_PARENT_FLOW_OBJECT_INSTANCE_ID);
    }

    public void setParentFlowObjectInstanceId(Long parentFlowObjectInstanceId) {
        put(_PARENT_FLOW_OBJECT_INSTANCE_ID, parentFlowObjectInstanceId);
    }

    public FlowInstanceStep getCurrentStep() {
        return (FlowInstanceStep) getMap().get(CURRENT_STEP);
    }

    public void setCurrentStep(FlowInstanceStep flowInstanceStep) {
        put(CURRENT_STEP, flowInstanceStep);
    }

    @SuppressWarnings("unchecked")
    private void initialize() {
        if (containsKey(CURRENT_STEP)) {
            Object currentStep = getMap().get(CURRENT_STEP);
            Map currentStepMap = currentStep instanceof JsonObject ?
                    ((JsonObject) currentStep).getMap() : (Map) currentStep;
            setCurrentStep(new FlowInstanceStep(currentStepMap));
        }
    }
}

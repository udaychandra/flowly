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

package io.flowly.engine.assets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Compiles a given flow. Creates a map based on flow object ids.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class CompiledFlow {
    // A flow can have just one start event.
    private FlowObject startEvent;

    private List<FlowObject> endEvents = new ArrayList<>();
    Map<String, FlowObject> flowObjectMap;
    private String flowId;
    private String invalidMessage;

    public CompiledFlow(Flow flow) {
        flowId = flow.getId();
        flowObjectMap = new HashMap<>();

        for (FlowObject flowObject : flow.getFlowObjects()) {
            flowObjectMap.put(flowObject.getId(), flowObject);

            if (flowObject instanceof StartEvent) {
                startEvent = flowObject;
            }
            else if (flowObject instanceof EndEvent) {
                endEvents.add(flowObject);
            }
        }
    }

    public FlowObject getStartEvent() {
        return startEvent;
    }

    public List<FlowObject> getEndEvents() {
        return endEvents;
    }

    public Map<String, FlowObject> getFlowObjectMap() {
        return flowObjectMap;
    }

    public boolean isValid() {
        invalidMessage = null;

        if (getStartEvent() == null) {
            invalidMessage = "Start event not found for flow: " + flowId;
            return false;
        }

        if (getEndEvents().isEmpty()) {
            invalidMessage = "End event(s) not found for flow: " + flowId;
            return false;
        }

        return true;
    }

    public String getInvalidMessage() {
        return invalidMessage;
    }
}

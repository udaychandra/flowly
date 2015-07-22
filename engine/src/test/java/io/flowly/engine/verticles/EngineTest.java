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

package io.flowly.engine.verticles;

import io.flowly.core.data.FlowMetadata;
import io.flowly.engine.EngineAddresses;
import io.flowly.engine.JsonKeys;
import io.flowly.engine.assets.Process;
import io.flowly.core.data.FlowInstance;
import io.flowly.core.data.FlowInstanceMetadata;
import io.flowly.core.data.FlowInstanceStep;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import io.vertx.ext.unit.junit.VertxUnitRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a>Uday Tatiraju</a>
 */
@RunWith(VertxUnitRunner.class)
public class EngineTest extends BaseVerticleTest {
    private static class Checker {
        private Long instanceId;

        public Long getInstanceId() {
            return instanceId;
        }

        public void setInstanceId(Long instanceId) {
            this.instanceId = instanceId;
        }
    }

    @Before
    public void before(TestContext context) {
        super.before();
        deployKernelVerticle(context, null);
    }

    @Test
    public void testEndToEndSimpleInstance(TestContext context) {
        assertEndToEndInstance(context, APP_1_ID, "2429373120533", Process.class.getSimpleName());
    }

    @Test
    public void testEndToEndInstanceWithDecisionGateway(TestContext context) {
        assertEndToEndInstance(context, APP_2_ID, "1429373120533", Process.class.getSimpleName());
    }

    @Test
    public void testEndToEndInstanceWithMicroServiceAsSubFlow(TestContext context) {
        assertEndToEndInstance(context, APP_2_ID, "1429373120550", Process.class.getSimpleName());
    }

    @Test
    public void testEndToEndInstanceWithInteractiveServiceAsSubFlow(TestContext context) {
        // Register handler to mock user interaction.
        vertx.eventBus().consumer(EngineAddresses.FLOW_LIFECYCLE_EVENT, h-> {
            JsonObject event = (JsonObject) h.body();
            String eventType = event.getString(JsonKeys.FLOW_EVENT_TYPE);

            if (eventType.equals(JsonKeys.FLOW_WAIT_UI_EVENT)) {
                vertx.eventBus().send(EngineAddresses.START_FLOW_INSTANCE_TASK,
                        event.getLong(FlowInstanceStep._FLOW_OBJECT_INSTANCE_ID));
            }
            else if (eventType.equals(JsonKeys.FLOW_START_UI_EVENT)) {
                // Complete user interaction.
                FlowInstanceMetadata metadata = new FlowInstanceMetadata();

                FlowInstanceStep currentStep = new FlowInstanceStep();
                currentStep.setFlowObjectInstanceId(event.getLong(FlowInstanceStep._FLOW_OBJECT_INSTANCE_ID));
                metadata.setCurrentStep(currentStep);

                JsonObject data = new JsonObject();
                data.put("emailSubject", "Mock email subject.");
                data.put("emailBody", "Mock email body.");

                FlowInstance instance = new FlowInstance();
                instance.setData(data);
                instance.setMetadata(metadata);

                vertx.eventBus().send(EngineAddresses.COMPLETE_FLOW_INSTANCE_TASK, instance);
            }
        });

        assertEndToEndInstance(context, APP_2_ID, "1429373120535", Process.class.getSimpleName());
    }

    private void assertEndToEndInstance(TestContext context, String appId, String flowId, String flowType) {
        FlowMetadata flowMetadata = createFlowMetadata(appId, flowId, flowType);

        Async async = context.async();
        final Checker checker = new Checker();

        // Register flow event listener.
        vertx.eventBus().consumer(EngineAddresses.FLOW_LIFECYCLE_EVENT, h -> {
            JsonObject event = (JsonObject) h.body();
            String eventType = event.getString(JsonKeys.FLOW_EVENT_TYPE);
            Long instanceId = event.getLong(JsonKeys.INSTANCE_ID);
            Long parentFlowObjectInstanceId = event.getLong(FlowInstanceMetadata._PARENT_FLOW_OBJECT_INSTANCE_ID);

            // Do not assert sub flows, start events and await events.
            if ((!eventType.equals(JsonKeys.FLOW_START_EVENT) && !eventType.equals(JsonKeys.FLOW_WAIT_UI_EVENT)) &&
                    parentFlowObjectInstanceId == null) {
                context.assertEquals(checker.getInstanceId(), instanceId);
                context.assertEquals(JsonKeys.FLOW_COMPLETE_EVENT, eventType);
                async.complete();
            }
        });

        deployApp(appId, h -> {
            if (h.succeeded()) {
                vertx.eventBus().send(EngineAddresses.START_FLOW_INSTANCE, flowMetadata, context.asyncAssertSuccess(s -> {
                    checker.setInstanceId((Long) s.body());
                    context.assertNotNull(checker.getInstanceId(), "Flow instance id is not set.");
                }));
            }
            else {
                context.fail();
            }
        });
    }

    private FlowMetadata createFlowMetadata(String appId, String flowId, String flowType) {
        FlowMetadata flowMetadata = new FlowMetadata();
        flowMetadata.setFlowId(flowId);
        flowMetadata.setFlowType(flowType);
        flowMetadata.setAppId(appId);

        return flowMetadata;
    }
}
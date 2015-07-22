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

package io.flowly.core.codecs;

import io.flowly.core.data.FlowInstanceMetadata;
import io.flowly.core.data.FlowInstanceStep;
import io.vertx.core.buffer.Buffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a>Uday Tatiraju</a>
 */
public class FlowInstanceMetadataCodecTest {
    private FlowInstanceMetadataCodec codec = new FlowInstanceMetadataCodec();
    private FlowInstanceMetadata instanceMetadata;

    @Before
    public void setUp() {
        instanceMetadata = createInstanceMetadata();
    }

    @Test
    public void transformObjectTest() {
        assertFlowInstanceMetadata(codec.transform(instanceMetadata));
    }

    @Test(expected = IllegalStateException.class)
    public void encodeObjectToWireTest() {
        codec.encodeToWire(Buffer.buffer(), instanceMetadata);
    }

    @Test(expected = IllegalStateException.class)
    public void decodeObjectFromWireTest() {
        codec.decodeFromWire(0, Buffer.buffer());
    }

    protected static void assertFlowInstanceMetadata(FlowInstanceMetadata instanceMetadata) {
        FlowMetadataCodecTest.assertFlowMetadata(instanceMetadata);

        Assert.assertEquals("Instance id not as expected.", 100l, instanceMetadata.getInstanceId().longValue());

        FlowInstanceStep step = instanceMetadata.getCurrentStep();
        Assert.assertNotNull("Current step cannot be null.", step);
        Assert.assertEquals("Flow object instance id is not as expected.",
                200l, step.getFlowObjectInstanceId().longValue());
        Assert.assertEquals("Flow object id not as expected", "1001", step.getFlowObjectId());
        Assert.assertEquals("Step index not as expected", 0, step.getStepIndex());
    }

    protected static FlowInstanceMetadata createInstanceMetadata() {
        FlowInstanceMetadata instanceMetadata = new FlowInstanceMetadata(FlowMetadataCodecTest.
                createMetadata().getMap());
        instanceMetadata.setInstanceId(100l);

        FlowInstanceStep currentStep = new FlowInstanceStep();
        currentStep.setFlowObjectId("1001");
        currentStep.setFlowObjectInstanceId(200l);
        currentStep.setStepIndex(0);
        instanceMetadata.setCurrentStep(currentStep);

        return instanceMetadata;
    }
}

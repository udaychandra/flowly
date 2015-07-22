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

import io.flowly.core.data.FlowInstance;
import io.flowly.core.data.FlowInstanceMetadata;
import io.vertx.core.buffer.Buffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a>Uday Tatiraju</a>
 */
public class FlowInstanceCodecTest {
    private FlowInstanceCodec codec = new FlowInstanceCodec();
    private FlowInstance instance;

    @Before
    public void setUp() {
        instance = new FlowInstance();
        instance.setMetadata(FlowInstanceMetadataCodecTest.createInstanceMetadata());
        instance.getData().put("sample", "sample");
    }

    @Test
    public void transformObjectTest() {
        assertFlowInstance(codec.transform(instance));
    }

    @Test
    public void encodeObjectToWireTest() {
        Buffer buffer = Buffer.buffer();
        codec.encodeToWire(buffer, instance);

        Assert.assertEquals("Encoded flow instance length not as expected  ", 205, buffer.length());
    }

    @Test
    public void decodeObjectFromWireTest() {
        Buffer buffer = Buffer.buffer();
        codec.encodeToWire(buffer, instance);
        assertFlowInstance(codec.decodeFromWire(0, buffer));
    }

    protected static void assertFlowInstance(FlowInstance instance) {
        Assert.assertNotNull("Instance cannot be null.", instance);
        Assert.assertNotNull("Instance data cannot be null", instance.getData());
        Assert.assertEquals("Instance data not as expected", "sample", instance.getData().getString("sample"));

        FlowInstanceMetadata instanceMetadata = instance.getMetadata();
        FlowInstanceMetadataCodecTest.assertFlowInstanceMetadata(instanceMetadata);
    }
}

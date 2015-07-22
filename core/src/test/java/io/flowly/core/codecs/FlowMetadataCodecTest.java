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

import io.flowly.core.data.FlowMetadata;
import io.vertx.core.buffer.Buffer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a>Uday Tatiraju</a>
 */
public class FlowMetadataCodecTest {
    private FlowMetadataCodec codec = new FlowMetadataCodec();
    private FlowMetadata metadata;

    @Before
    public void setUp() {
        metadata = createMetadata();
    }

    @Test
    public void transformObjectTest() {
        assertFlowMetadata(codec.transform(metadata));
    }

    @Test
    public void encodeObjectToWireTest() {
        Buffer buffer = Buffer.buffer();
        codec.encodeToWire(buffer, metadata);

        Assert.assertEquals("Encoded flow metadata length not as expected  ", 65, buffer.length());
    }

    @Test
    public void decodeObjectFromWireTest() {
        Buffer buffer = Buffer.buffer();
        codec.encodeToWire(buffer, metadata);
        assertFlowMetadata(codec.decodeFromWire(0, buffer));
    }

    protected static void assertFlowMetadata(FlowMetadata metadata) {
        Assert.assertEquals("Flow ids are not equal.", "123", metadata.getFlowId());
        Assert.assertEquals("Flow types are not equal.", "process", metadata.getFlowType());
        Assert.assertEquals("App ids are not equal.", "com.test.app1", metadata.getAppId());
    }

    protected static FlowMetadata createMetadata() {
        FlowMetadata metadata = new FlowMetadata();
        metadata.setFlowId("123");
        metadata.setFlowType("process");
        metadata.setAppId("com.test.app1");

        return metadata;
    }
}

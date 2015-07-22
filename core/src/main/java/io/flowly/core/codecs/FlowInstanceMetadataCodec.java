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
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Vert.x message codec used to pass {@link io.flowly.core.data.FlowInstanceMetadata} objects
 * on the local event bus.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class FlowInstanceMetadataCodec implements MessageCodec<FlowInstanceMetadata, FlowInstanceMetadata> {
    public static final String NAME = "flowInstanceMetadata";

    @Override
    public void encodeToWire(Buffer buffer, FlowInstanceMetadata metadata) {
        throw new IllegalStateException("Encoding the FlowInstanceMetadata object over the wire" +
                " is not supported by this codec.");
    }

    @Override
    public FlowInstanceMetadata decodeFromWire(int pos, Buffer buffer) {
        throw new IllegalStateException("Decoding the FlowInstanceMetadata object over the wire" +
                " is not supported by this codec.");
    }

    @Override
    public FlowInstanceMetadata transform(FlowInstanceMetadata metadata) {
        // If the object is on the local event bus, just pass the instance.
        // Onus is on the consumer to ensure mutable state of the object.
        return metadata;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}

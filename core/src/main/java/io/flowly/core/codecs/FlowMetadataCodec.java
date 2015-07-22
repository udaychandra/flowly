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
import io.netty.util.CharsetUtil;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Vert.x message codec used to serialize and deserialize {@link io.flowly.core.data.FlowMetadata} objects.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class FlowMetadataCodec implements MessageCodec<FlowMetadata, FlowMetadata> {
    public static final String NAME = "flowMetadata";

    @Override
    public void encodeToWire(Buffer buffer, FlowMetadata flowMetadata) {
        // Same as encoding a JsonObject.
        String strJson = flowMetadata.encode();
        byte[] encoded = strJson.getBytes(CharsetUtil.UTF_8);
        buffer.appendInt(encoded.length);
        Buffer buff = Buffer.buffer(encoded);
        buffer.appendBuffer(buff);
    }

    @Override
    public FlowMetadata decodeFromWire(int pos, Buffer buffer) {
        // Same as decoding a JsonObject.
        int length = buffer.getInt(pos);
        pos += 4;
        byte[] encoded = buffer.getBytes(pos, pos + length);
        String str = new String(encoded, CharsetUtil.UTF_8);
        return new FlowMetadata(str);
    }

    @Override
    public FlowMetadata transform(FlowMetadata flowMetadata) {
        // If the object is on the local event bus, just pass the instance.
        // Onus is on the consumer to ensure mutable state of the object.
        return flowMetadata;
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

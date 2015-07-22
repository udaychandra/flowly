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

package io.flowly.engine.codecs;

import io.flowly.engine.data.FlowInstanceWrapper;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Codec that passes FlowInstanceWrapper objects directly in local event bus communications.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class FlowInstanceWrapperCodec implements MessageCodec<FlowInstanceWrapper, FlowInstanceWrapper> {
    public static final String NAME = "flowInstanceWrapper";

    @Override
    public void encodeToWire(Buffer buffer, FlowInstanceWrapper flowInstanceWrapper) {
        throw new IllegalStateException("Encoding the FlowInstanceWrapper object over the wire is not supported by this codec.");
    }

    @Override
    public FlowInstanceWrapper decodeFromWire(int pos, Buffer buffer) {
        throw new IllegalStateException("Decoding the FlowInstanceWrapper object over the wire is not supported by this codec.");
    }

    @Override
    public FlowInstanceWrapper transform(FlowInstanceWrapper flowInstanceWrapper) {
        return flowInstanceWrapper;
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

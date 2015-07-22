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

import io.flowly.engine.assets.MicroService;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Codec that passes MicroService objects directly in local event bus communications.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class MicroServiceCodec implements MessageCodec<MicroService, MicroService> {
    @Override
    public void encodeToWire(Buffer buffer, MicroService microService) {
        throw new IllegalStateException("Encoding the MicroService object over the wire is not supported by this codec.");
    }

    @Override
    public MicroService decodeFromWire(int pos, Buffer buffer) {
        throw new IllegalStateException("Decoding the MicroService object over the wire is not supported by this codec.");
    }

    @Override
    public MicroService transform(MicroService microService) {
        return microService;
    }

    @Override
    public String name() {
        return "microService";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}

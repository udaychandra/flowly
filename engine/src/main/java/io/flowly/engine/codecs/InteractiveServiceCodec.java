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

import io.flowly.engine.assets.InteractiveService;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Codec that passes InteractiveService objects directly in local event bus communications.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class InteractiveServiceCodec implements MessageCodec<InteractiveService, InteractiveService> {
    @Override
    public void encodeToWire(Buffer buffer, InteractiveService interactiveService) {
        throw new IllegalStateException("Encoding the InteractiveService object over the wire is not supported by this codec.");
    }

    @Override
    public InteractiveService decodeFromWire(int pos, Buffer buffer) {
        throw new IllegalStateException("Decoding the InteractiveService object over the wire is not supported by this codec.");
    }

    @Override
    public InteractiveService transform(InteractiveService interactiveService) {
        return interactiveService;
    }

    @Override
    public String name() {
        return "interactiveService";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}

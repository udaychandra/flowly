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

import io.flowly.engine.router.Route;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Codec that passes the Route objects directly in local event bus communications.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class RouteCodec implements MessageCodec<Route, Route> {
    @Override
    public void encodeToWire(Buffer buffer, Route route) {
        throw new IllegalStateException("Encoding the Route object over the wire is not supported by this codec.");
    }

    @Override
    public Route decodeFromWire(int pos, Buffer buffer) {
        throw new IllegalStateException("Decoding the Route object over the wire is not supported by this codec.");
    }

    @Override
    public Route transform(Route route) {
        return route;
    }

    @Override
    public String name() {
        return "route";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}

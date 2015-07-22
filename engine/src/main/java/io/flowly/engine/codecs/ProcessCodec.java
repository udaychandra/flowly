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

import io.flowly.engine.assets.Process;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * Codec that passes the Process objects directly in local event bus communications.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class ProcessCodec implements MessageCodec<Process, Process> {

    @Override
    public void encodeToWire(Buffer buffer, Process process) {
        throw new IllegalStateException("Encoding the Process object over the wire is not supported by this codec.");
    }

    @Override
    public Process decodeFromWire(int pos, Buffer buffer) {
        throw new IllegalStateException("Decoding the Process object over the wire is not supported by this codec.");
    }

    @Override
    public Process transform(Process process) {
        return process;
    }

    @Override
    public String name() {
        return "process";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}

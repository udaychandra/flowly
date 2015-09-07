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

package io.flowly.core.verticles;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;

/**
 * Wrapper that holds an event bus message consumer handler and event bus address.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class ConsumerRegistration<T> {
    private boolean localOnly;
    private String address;
    private Handler<Message<T>> messageHandler;

    public ConsumerRegistration(String address, Handler<Message<T>> messageHandler) {
        this(address, messageHandler, false);
    }

    public ConsumerRegistration(String address, Handler<Message<T>> messageHandler, boolean localOnly) {
        this.address = address;
        this.messageHandler = messageHandler;
        this.localOnly = localOnly;
    }

    public Handler<Message<T>> getMessageHandler() {
        return messageHandler;
    }

    public String getAddress() {
        return address;
    }

    public boolean isLocalOnly() {
        return localOnly;
    }
}

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

package io.flowly.engine.data;

import io.flowly.core.data.FlowInstance;
import io.flowly.engine.router.Route;

/**
 * A wrapper object that is used to pass additional info along with a flow instance
 * to the repository verticle.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class FlowInstanceWrapper {
    private FlowInstance instance;
    private boolean start;
    private boolean saveMetadata;
    private String Status;
    private Route.Next next;

    public FlowInstanceWrapper(FlowInstance instance, boolean start, boolean saveMetadata,
                               String status, Route.Next next) {
        this.instance = instance;
        this.start = start;
        this.saveMetadata = saveMetadata;
        Status = status;
        this.next = next;
    }

    public FlowInstance getInstance() {
        return instance;
    }

    public boolean isStart() {
        return start;
    }

    public boolean saveMetadata() {
        return saveMetadata;
    }

    public String getStatus() {
        return Status;
    }

    public Route.Next getNext() {
        return next;
    }
}

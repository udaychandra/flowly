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

package io.flowly.engine.assets;

/**
 * Represents a sequence line between two flow objects.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class ConnectingObject extends BasicFlowObject {
    private String fromId;
    private String toId;

    public ConnectingObject() {
    }

    public ConnectingObject(String id, String name, String description, String type, String fromId, String toId) {
        super(id, name, description, type);
        this.fromId = fromId;
        this.toId = toId;
    }

    public String getFromId() {
        return fromId;
    }

    public String getToId() {
        return toId;
    }
}

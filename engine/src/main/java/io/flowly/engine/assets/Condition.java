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
 * Represents a truthy condition in a decision gateway. Used to describe the output
 * sequence line to be followed when the specified condition is met.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class Condition extends BasicAsset {
    private String when;
    private String connectTo;

    public Condition() {
    }

    public Condition(String id, String name, String description, String when, String connectTo) {
        super(id, name, description);
        this.when = when;
        this.connectTo = connectTo;
    }

    public String getWhen() {
        return when;
    }

    public String getConnectTo() {
        return connectTo;
    }
}

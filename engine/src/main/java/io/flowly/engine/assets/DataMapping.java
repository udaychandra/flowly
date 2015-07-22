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
 * Defines input or output mapping of a variable in a flow or sub flow.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class DataMapping extends BasicAsset {
    private String from;
    private String to;
    private String scope;

    public DataMapping() {
    }

    public DataMapping(String id, String name, String description, String from, String to, String scope) {
        super(id, name, description);
        this.from = from;
        this.to = to;
        this.scope = scope;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getScope() {
        return scope;
    }
}

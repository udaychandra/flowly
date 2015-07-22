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
 * Represents an input, output or private variable defined in a flow.
 * Data objects are instantiated based on the variables when a flow is run.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class Variable extends BasicAsset {
    // Constants that define a variable scope.
    public static final String INPUT_SCOPE = "Input";
    public static final String OUTPUT_SCOPE = "Output";
    public static final String PRIVATE_SCOPE = "Private";

    private String scope;

    public Variable() {
    }

    public Variable(String id, String name, String description, String scope) {
        super(id, name, description);
        this.scope = scope;
    }

    public String getScope() {
        return scope;
    }
}

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

import io.flowly.engine.compilers.Compiler;

/**
 * Represents the flow object that signals the end of a flow.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class EndEvent extends BasicFlowObject {
    public EndEvent() {
    }

    public EndEvent(String id, String name, String description, String type) {
        super(id, name, description, type);
    }

    @Override
    public void compile(Compiler compiler, Flow flow, StringBuilder output) {
        compiler.compile(this, flow, output);
    }
}

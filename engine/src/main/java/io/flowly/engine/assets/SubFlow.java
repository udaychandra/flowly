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
 * A flow object that defines a sub flow.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class SubFlow extends BasicFlowObject {
    private String subFlowId;
    private String subFlowType;
    private String address;

    public SubFlow() {
    }

    public SubFlow(String id, String name, String description, String type, String subFlowId,
                   String subFlowType, String address) {
        super(id, name, description, type);
        this.subFlowId = subFlowId;
        this.subFlowType = subFlowType;
        this.address = address;
    }

    public String getSubFlowId() {
        return subFlowId;
    }

    public String getSubFlowType() {
        return subFlowType;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public void compile(Compiler compiler, Flow flow, StringBuilder output) {
        compiler.compile(this, flow, output);
    }
}

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

import java.util.ArrayList;
import java.util.List;

/**
 * The abstract base class that can be extended to define specific flow objects.
 *
 * @author <a>Uday Tatiraju</a>
 */
public abstract class BasicFlowObject extends BasicAsset implements FlowObject {
    private String type;
    private List<DataMapping> dataMappings = new ArrayList<>();

    public BasicFlowObject() {
    }

    public BasicFlowObject(String id, String name, String description, String type) {
        super(id, name, description);
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }

    public List<DataMapping> getDataMappings() {
        return dataMappings;
    }

    public void addDataMapping(DataMapping dataMapping) {
        this.dataMappings.add(dataMapping);
    }

    @Override
    public void compile(Compiler compiler, Flow flow, StringBuilder output) {}
}

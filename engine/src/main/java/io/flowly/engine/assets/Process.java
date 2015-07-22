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

import io.flowly.engine.App;
import io.flowly.engine.compilers.Compiler;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a business process definition (BPD).
 *
 * @author <a>Uday Tatiraju</a>
 */
public class Process extends BasicFlow {
    private List<SwimLane> swimLanes;

    public Process() {
    }

    public Process(String id, String name, String description, App app) {
        super(id, name, description, null, app);
        swimLanes = new ArrayList<>();
    }

    public List<SwimLane> getSwimLanes() {
        return swimLanes;
    }

    public void addSwimLane(SwimLane swimLane) {
        this.swimLanes.add(swimLane);
    }

    public void compile(Compiler compiler, Flow flow, StringBuilder output) {
        compiler.compile(this, null, output);

        for (FlowObject flowObject : getFlowObjects()) {
            flowObject.compile(compiler, this, output);
        }
    }

    @Override
    public String getType() {
        return "Process";
    }
}

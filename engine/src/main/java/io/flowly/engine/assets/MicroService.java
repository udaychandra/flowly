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

/**
 * Represents a service flow that will be deployed as a micro service (verticle)
 * on a vert.x instance.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class MicroService extends BasicFlow {
    public MicroService() {
    }

    public MicroService(String id, String name, String description, App app) {
        super(id, name, description, null, app);
    }

    @Override
    public void compile(Compiler compiler, Flow flow, StringBuilder output) {
        compiler.compile(this, null, output);

        for (FlowObject flowObject : getFlowObjects()) {
            flowObject.compile(compiler, this, output);
        }
    }

    @Override
    public String getType() {
        return "MicroService";
    }
}

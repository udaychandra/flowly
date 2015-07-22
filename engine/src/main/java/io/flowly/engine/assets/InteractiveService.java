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
 * A flow that represents a service that includes an interactive component,
 * like a {@link io.flowly.engine.assets.View}.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class InteractiveService extends BasicFlow {
    public InteractiveService() {
    }

    public InteractiveService(String id, String name, String description, App app) {
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
        return "InteractiveService";
    }
}

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
 * Represents a flow object that defines inline JavaScript.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class InlineScript extends BasicFlowObject {
    private String scriptRefId;

    public InlineScript() {
    }

    public InlineScript(String id, String name, String description, String type, String scriptRefId) {
        super(id, name, description, type);
        this.scriptRefId = scriptRefId;
    }

    public String getScriptRefId() {
        return scriptRefId;
    }

    @Override
    public void compile(Compiler compiler, Flow flow, StringBuilder output) {
        compiler.compile(this, flow, output);
    }
}

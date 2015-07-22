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

import java.util.List;

/**
 * Represents the basic building block of a {@link io.flowly.engine.assets.Flow}.
 *
 * @author <a>Uday Tatiraju</a>
 */
public interface FlowObject extends Asset {
    String getType();

    List<DataMapping> getDataMappings();
    void addDataMapping(DataMapping dataMapping);

    /**
     * Compile the flow object into JavaScript. Concrete classes should call the compiler's compile
     * method and pass a reference to self - visitor pattern.
     * @param compiler the compiler that can parse the asset and generate JavaScript.
     * @param flow the optional container flow that contains this flow object.
     * @param output Represents the output of the compiler (JavaScript).
     */
    void compile(Compiler compiler, Flow flow, StringBuilder output);
}

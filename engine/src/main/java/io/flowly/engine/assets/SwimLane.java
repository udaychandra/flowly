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
 * Represents a container for a set of flow objects within a {@link io.flowly.engine.assets.Process}.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class SwimLane extends BasicAsset {
    public SwimLane() {
    }

    public SwimLane(String id, String name, String description) {
        super(id, name, description);
    }
}

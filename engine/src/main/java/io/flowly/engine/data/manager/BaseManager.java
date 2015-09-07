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

package io.flowly.engine.data.manager;

import io.flowly.core.data.manager.GraphManager;
import org.apache.tinkerpop.gremlin.structure.Graph;

/**
 * Defines constants shared by all data managers.
 *
 * @author <a>Uday Tatiraju</a>
 */
public abstract class BaseManager extends GraphManager {
    public static final String STATUS_IN_PROGRESS = "In progress";
    public static final String STATUS_FAILED = "Failed";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_NEW = "New";
    public static final String STATUS_USER_INTERACTING = "User interacting";

    public BaseManager(Graph graph) {
        super(graph);
    }
}

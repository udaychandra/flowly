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

package io.flowly.auth.manager;

import io.flowly.auth.graph.Schema;
import io.flowly.core.data.Resource;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class that defines a vertx instance and common methods.
 * The vertx instance is started at the beginning of a test and
 * closed at the end of the test.
 *
 * @author <a>Uday Tatiraju</a>
 */
public abstract class BaseManagerTest {
    protected Vertx vertx;
    protected TinkerGraph graph;
    protected List<Vertex> resources;
    protected long identity;

    @Before
    public void setUp() {
        vertx = Vertx.vertx();
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    protected void createResources() {
        resources = new ArrayList<>();
        resources.add(graph.addVertex(T.id, identity++, T.label, Schema.V_RESOURCE,
                Resource.RESOURCE_ID, "Studio"));
        resources.add(graph.addVertex(T.id, identity++, T.label, Schema.V_RESOURCE,
                Resource.RESOURCE_ID, "Console"));
        resources.add(graph.addVertex(T.id, identity++, T.label, Schema.V_RESOURCE,
                Resource.RESOURCE_ID, "Launchpad"));
        resources.add(graph.addVertex(T.id, identity++, T.label, Schema.V_RESOURCE,
                Resource.RESOURCE_ID, "Dashboard"));
    }
}

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

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;
import java.util.Objects;

/**
 * Base class that defines common methods that are shared by all data managers.
 *
 * @author <a>Uday Tatiraju</a>
 */
public abstract class BaseManager {
    public static final String STATUS_IN_PROGRESS = "In progress";
    public static final String STATUS_FAILED = "Failed";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_NEW = "New";
    public static final String STATUS_USER_INTERACTING = "User interacting";

    private Boolean supportsTransactions;
    protected Graph graph;

    public BaseManager(Graph graph) {
        this.graph = Objects.requireNonNull(graph, "Manager cannot be constructed without a graph.");
        supportsTransactions = graph.features().graph().supportsTransactions();
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    protected Vertex getVertex(Object id) {
        Iterator<Vertex> vertices = graph.vertices(id);
        return vertices.hasNext() ? vertices.next() : null;
    }

    protected Object getPropertyValue(Vertex vertex, String propertyName) {
        Property property = vertex.property(propertyName);
        return property.isPresent() ? property.value() : null;
    }

    protected void commit() {
        if (supportsTransactions) {
            graph.tx().commit();
        }
    }

    protected void rollback() {
        if (supportsTransactions) {
            graph.tx().rollback();
        }
    }
}

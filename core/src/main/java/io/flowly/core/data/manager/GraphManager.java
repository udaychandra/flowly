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

package io.flowly.core.data.manager;

import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;
import java.util.Objects;

/**
 * Base class that defines common methods that are shared by all graph data managers.
 *
 * @author <a>Uday Tatiraju</a>
 */
public abstract class GraphManager {
    protected static final int ADDITIONAL_RECORDS = 1;
    private static final Logger logger = LoggerFactory.getLogger(GraphManager.class);

    private Boolean supportsTransactions;
    protected Graph graph;

    public GraphManager(Graph graph) {
        this.graph = Objects.requireNonNull(graph, "Manager cannot be constructed without a graph.");
        this.supportsTransactions = graph.features().graph().supportsTransactions();
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public Vertex getVertex(Object id) {
        Iterator<Vertex> vertices = graph.vertices(id);
        return vertices.hasNext() ? vertices.next() : null;
    }

    /**
     * Remove the vertex from the graph.
     *
     * @param id the vertex id in auth graph.
     * @return an empty list or a list of validation errors based on whether the vertex was deleted or not.
     */
    public JsonArray delete(Object id) {
        JsonArray errors = new JsonArray();

        try {
            Vertex vertex = getVertex(id);

            if (vertex != null) {
                // removing a vertex removes all its incident edges as well.
                vertex.remove();
            }
            else {
                errors.add("Vertex does not exist: " + id);
            }

            commit();
        }
        catch (Exception ex) {
            rollback();
            String error = "Unable to delete vertex: " + id;
            logger.error(error, ex);
            errors.add(error);
        }

        return errors;
    }

    public <T> T getPropertyValue(Element element, String propertyName) {
        Property<T> property = element.property(propertyName);
        return property.isPresent() ? property.value() : null;
    }

    public <T> void setPropertyValue(Element element, String propertyName, T propertyValue) {
        setPropertyValue(element, propertyName, propertyValue, null);
    }

    public <T> void setPropertyValue(Element element, String propertyName, T propertyValue, T defaultValue) {
        if (propertyValue == null && defaultValue == null) {
            return;
        }

        T value = propertyValue == null ? defaultValue : propertyValue;
        element.property(propertyName, value);
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

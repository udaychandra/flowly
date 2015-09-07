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
import io.flowly.core.data.manager.GraphManager;
import io.flowly.core.security.Permission;
import io.flowly.core.security.User;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.List;

/**
 * Defines CRUD operations on resources in flowly.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class ResourceManager extends BaseManager {
    private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);

    public ResourceManager(Graph graph) {
        super(graph);
    }

    /**
     * Create a resource node in the auth graph.
     *
     * @param resource JSON object representing the resource attributes.
     *                 Ex: {
     *                     "resourceId": "Studio",
     *                     "description": "Allows users to design and develop flowly apps."
     *                 }
     * @return an empty list or a list of validation errors based on whether the resource was created or not.
     */
    @Override
    public JsonArray create(JsonObject resource) {
        Resource newResource = new Resource(resource);
        JsonArray errors = newResource.validate();

        if (errors.size() == 0) {
            try {
                Vertex resourceVertex = graph.addVertex(T.label, Schema.V_RESOURCE,
                        Schema.V_P_RESOURCE_ID, newResource.getResourceId());
                setPropertyValue(resourceVertex, Schema.V_P_DESCRIPTION, newResource.getDescription());

                commit();
            }
            catch (Exception ex) {
                rollback();
                String error = "Unable to create resource: " + newResource.getResourceId();
                logger.error(error, ex);
                errors.add(error);
            }
        }

        return errors;
    }

    /**
     * Update the attributes of a resource in the auth graph.
     *
     * @param resource JSON object representing the resource attributes.
     *                 Ex: {
     *                     "id": 12345,
     *                     "resourceId": "Studio",
     *                     "description": "Allows users to design and develop flowly apps."
     *                 }
     *
     * @return an empty list or a list of validation errors based on whether the resource was updated or not.
     */
    @Override
    public JsonArray update(JsonObject resource) {
        Resource newResource = new Resource(resource);
        JsonArray errors = newResource.validate(true);

        if (errors.size() == 0) {
            try {
                Vertex resourceVertex = getVertex(newResource.getId());
                setPropertyValue(resourceVertex, Schema.V_P_RESOURCE_ID, newResource.getResourceId());
                setPropertyValue(resourceVertex, Schema.V_P_DESCRIPTION, newResource.getDescription());

                commit();
            }
            catch (Exception ex) {
                rollback();
                String error = "Unable to update resource: " + newResource.getResourceId();
                logger.error(error, ex);
                errors.add(error);
            }
        }

        return errors;
    }

    /**
     * Get the resource based on the unique id assigned by the auth graph.
     *
     * @param id the resource vertex id in auth graph.
     * @return JSON object representing the resource.
     *         Ex: {
     *             "id": 12345,
     *             "resourceId": "Studio",
     *             "description": "Allows users to design and develop flowly apps."
     *         }
     */
    @Override
    public JsonObject get(Long id) {
        try {
            Vertex resourceVertex = getVertex(id);
            JsonObject resource = makeResourceObject(resourceVertex);
            commit();

            return resource;
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to retrieve resource: " + id, ex);
            return null;
        }
    }

    /**
     * Get the resource based on the resource Id.
     *
     * @param resourceId the resource id that identifies the resource in the auth graph.
     * @return JSON object representing the resource.
     *         Ex: {
     *             "id": 12345,
     *             "resourceId": "Studio",
     *             "description": "Allows users to design and develop flowly apps."
     *         }
     */
    @Override
    public JsonObject get(String resourceId) {
        try {
            JsonObject resource = null;
            GraphTraversal<Vertex, Vertex> traversal = graph.traversal().
                    V().has(Schema.V_RESOURCE, Schema.V_P_RESOURCE_ID, resourceId);

            if (traversal.hasNext()) {
                resource = makeResourceObject(traversal.next());
            }

            commit();
            return resource;
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to retrieve resource: " + resourceId, ex);
            return null;
        }
    }

    @Override
    public Handler<Message<JsonObject>> getHandler() {
        return message -> {
            JsonObject args = message.body();
            message.reply(get(args.getString(Resource.RESOURCE_ID)));
        };
    }

    /**
     * Search for resources based on provided criteria.
     * By default, resources are sorted by RESOURCE_ID in ascending order.
     *
     * @param pageNumber the page number used to retrieve resources.
     * @param pageSize the maximum number of resources that fill a page.
     * @return a list of resources.
     */
    @Override
    public JsonArray search(int pageNumber, int pageSize) {
        int low = (pageNumber - 1) * pageSize;
        int high = low + pageSize;

        try {
            List<Resource> resources = graph.traversal().V().hasLabel(Schema.V_RESOURCE).order().
                    by(Schema.V_P_RESOURCE_ID, Order.incr).range(low, high + ADDITIONAL_RECORDS).
                    map(m -> makeResourceObject(m.get())).toList();

            commit();
            return new JsonArray(resources);
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to search for users.", ex);
            return null;
        }
    }

    private Resource makeResourceObject(Vertex resourceVertex) {
        Resource resource = new Resource();
        resource.setId(resourceVertex.id());
        resource.setResourceId(getPropertyValue(resourceVertex, Schema.V_P_RESOURCE_ID));
        resource.setDescription(getPropertyValue(resourceVertex, Schema.V_P_DESCRIPTION));

        return resource;
    }
}

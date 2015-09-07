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

import io.flowly.core.data.Resource;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a>Uday Tatiraju</a>
 */
@RunWith(VertxUnitRunner.class)
public class ResourceManagerTest extends BaseManagerTest {
    private ResourceManager resourceManager;

    @Before
    public void setUp() {
        super.setUp();

        graph = TinkerGraph.open();
        resourceManager = new ResourceManager(graph);
        identity = 1L;

        graph.createIndex(Resource.RESOURCE_ID, Vertex.class);
    }

    @Test
    public void testCreateResource(TestContext context) {
        Resource resource = makeResource();
        context.assertTrue(resourceManager.create(resource).size() == 0, "Errors thrown while creating a resource.");
        assertResource(context, new Resource(resourceManager.get(resource.getResourceId())), "Test", "Description");
    }

    @Test
    public void testUpdateResource(TestContext context) {
        Resource resource = makeResource();
        context.assertTrue(resourceManager.create(resource).size() == 0, "Errors thrown while creating a resource.");

        resource = new Resource(resourceManager.get(resource.getResourceId()));
        resource.setResourceId("Test Change");
        resource.remove(Resource.DESCRIPTION);

        context.assertTrue(resourceManager.update(resource).size() == 0, "Errors thrown while updating a resource.");
        assertResource(context, new Resource(resourceManager.get(resource.getResourceId())), "Test Change", null);
    }

    @Test
    public void testDeleteResource(TestContext context) {
        Resource resource = makeResource();
        context.assertTrue(resourceManager.create(resource).size() == 0, "Errors thrown while creating a resource.");

        resource = new Resource(resourceManager.get(resource.getResourceId()));
        context.assertTrue(resourceManager.delete(resource.getId()).size() == 0,
                "Errors thrown while deleting a resource.");
    }

    @Test
    public void testSearchResources(TestContext context) {
        createResources();
        context.assertEquals(4, resourceManager.search(1, 10).size(),
                "Result for search all resources not as expected.");
    }

    private Resource makeResource() {
        Resource resource = new Resource();
        resource.setResourceId("Test");
        resource.setDescription("Description");

        return resource;
    }

    private void assertResource(TestContext context, Resource resource,
                                String expectedResourceId, String expectedDescription) {
        context.assertEquals(expectedResourceId, resource.getResourceId(), "Resource ids not same.");

        if (expectedDescription != null) {
            context.assertEquals(expectedDescription, resource.getDescription(), "Descriptions not same.");
        }
    }
}

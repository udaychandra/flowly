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

package io.flowly.auth;

import io.flowly.auth.graph.AuthGraph;
import io.flowly.auth.manager.GroupManager;
import io.flowly.auth.manager.ResourceManager;
import io.flowly.auth.manager.UserManager;
import io.flowly.core.Failure;
import io.flowly.core.verticles.ConsumerRegistration;
import io.flowly.core.verticles.VerticleUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Worker verticle that handles all CRUD requests on users, groups and resources.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class AccessVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(AccessVerticle.class);

    private UserManager userManager;
    private GroupManager groupManager;
    private ResourceManager resourceManager;

    @Override
    public void start(Future<Void> startFuture) {
        if (!context.isWorkerContext()) {
            Failure failure = new Failure(200, "Access verticle should be deployed as worker verticle.");
            logger.error(failure);
            startFuture.fail(failure.getMessage());
        }

        try {
            initManagers();

            // Register message handlers.
            VerticleUtils.registerHandlers(vertx.eventBus(), logger, createMessageHandlers(), h -> {
                if (h.succeeded()) {
                    logger.info("Deployed access verticle.");
                    startFuture.complete();
                }
                else {
                    startFuture.fail(h.cause());
                }
            });
        }
        catch (Exception ex) {
            Failure failure = new Failure(201, "Unable to initialize graph managers.", ex);
            logger.error(failure);
            startFuture.fail(failure.getMessage());
        }
    }

    private void initManagers() throws Exception {
        Graph graph = new AuthGraph(config(), vertx.fileSystem()).getInstance();
        userManager = new UserManager(graph);
        groupManager = new GroupManager(graph);
        resourceManager = new ResourceManager(graph);
    }

    /**
     * Create message handlers for flow instance requests.
     * WARNING: Event bus is not protected. Use caution if you decide to publicly expose the listeners.
     *
     * @return queue of consumer registrations.
     */
    private Queue<ConsumerRegistration<JsonObject>> createMessageHandlers() {
        Queue<ConsumerRegistration<JsonObject>> registrations = new LinkedList<>();
        registrations.add(new ConsumerRegistration<>(LocalAddresses.GET_USER, userManager.getHandler(), true));
        registrations.add(new ConsumerRegistration<>(LocalAddresses.CREATE_USER, userManager.createHandler(), true));
        registrations.add(new ConsumerRegistration<>(LocalAddresses.UPDATE_USER, userManager.updateHandler(), true));
        registrations.add(new ConsumerRegistration<>(LocalAddresses.SEARCH_USER, userManager.searchHandler(), true));
        registrations.add(new ConsumerRegistration<>(LocalAddresses.AUTHENTICATE_USER,
                userManager.authenticateHandler(), true));

        registrations.add(new ConsumerRegistration<>(LocalAddresses.GET_GROUP, groupManager.getHandler(), true));
        registrations.add(new ConsumerRegistration<>(LocalAddresses.CREATE_GROUP, groupManager.createHandler(), true));
        registrations.add(new ConsumerRegistration<>(LocalAddresses.UPDATE_GROUP, groupManager.updateHandler(), true));
        registrations.add(new ConsumerRegistration<>(LocalAddresses.SEARCH_GROUP, groupManager.searchHandler(), true));

        registrations.add(new ConsumerRegistration<>(LocalAddresses.GET_RESOURCE,
                resourceManager.getHandler(), true));
        registrations.add(new ConsumerRegistration<>(LocalAddresses.CREATE_RESOURCE,
                resourceManager.createHandler(), true));
        registrations.add(new ConsumerRegistration<>(LocalAddresses.UPDATE_RESOURCE,
                resourceManager.updateHandler(), true));
        registrations.add(new ConsumerRegistration<>(LocalAddresses.SEARCH_RESOURCE,
                resourceManager.searchHandler(), true));

        return registrations;
    }
}

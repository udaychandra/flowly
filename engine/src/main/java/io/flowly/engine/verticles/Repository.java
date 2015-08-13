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

package io.flowly.engine.verticles;

import io.flowly.core.Failure;
import io.flowly.core.verticles.ConsumerRegistration;
import io.flowly.core.verticles.VerticleUtils;
import io.flowly.engine.EngineAddresses;
import io.flowly.engine.data.manager.FlowInstanceReadWriteManager;
import io.flowly.engine.data.manager.FlowlyGraph;
import io.flowly.engine.data.manager.FlowReadWriteManager;
import io.flowly.engine.data.manager.UserManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.tinkerpop.gremlin.structure.Graph;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Worker verticle that handles all graph operations on the backend.
 * Runs an instance of titan and uses the configured back store - Berkeley DB or Cassandra.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class Repository extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(Repository.class);

    private FlowInstanceReadWriteManager instanceManager;
    private FlowReadWriteManager flowManager;
    private UserManager userManager;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        try {
            // One graph to rule them all.
            Graph graph = new FlowlyGraph(config()).getInstance();
            instanceManager = new FlowInstanceReadWriteManager(graph);
            flowManager = new FlowReadWriteManager(graph);
            userManager = new UserManager(graph);

            // Register message handlers.
            VerticleUtils.registerHandlers(vertx.eventBus(), logger, createMessageHandlers(), h -> {
                if (h.succeeded()) {
                    logger.info("Deployed repository verticle.");
                    startFuture.complete();
                }
                else {
                    startFuture.fail(h.cause());
                }
            });
        }
        catch (Exception ex) {
            Failure failure = new Failure(2000, "Unable to prepare flowly graph for repository verticle.", ex);
            logger.error(failure.getError(), failure.getCause());
            startFuture.fail(failure.getCause());
        }
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        stopFuture.complete();
        logger.info("Undeployed repository verticle.");
    }

    /**
     * Create message handlers to deploy apps or flows.
     *
     * @return queue of consumer registrations.
     */
    private Queue<ConsumerRegistration> createMessageHandlers() {
        Queue<ConsumerRegistration> registrations = new LinkedList<>();

        registrations.add(new ConsumerRegistration(EngineAddresses.REPO_FLOW_SAVE,
                flowManager.saveFlowHandler(), true));
        registrations.add(new ConsumerRegistration(EngineAddresses.REPO_FLOW_DELETE,
                flowManager.deleteFlowHandler(), true));
        registrations.add(new ConsumerRegistration(EngineAddresses.REPO_FLOW_NEXT_ROUTE,
                flowManager.flowNextRouteHandler(), true));

        registrations.add(new ConsumerRegistration(EngineAddresses.GET_USER_INBOX,
                instanceManager.getInboxHandler()));
        registrations.add(new ConsumerRegistration(EngineAddresses.GET_USER_FLOWS,
                flowManager.getFlowsHandler()));
        registrations.add(new ConsumerRegistration(EngineAddresses.GET_FLOW_INSTANCE_TASK,
                instanceManager.getInstanceAtFlowObjectHandler()));

        registrations.add(new ConsumerRegistration(EngineAddresses.REPO_FLOW_CREATE_INSTANCE,
                instanceManager.createInstanceHandler(), true));
        registrations.add(new ConsumerRegistration(EngineAddresses.REPO_FLOW_SAVE_INSTANCE,
                instanceManager.saveInstanceHandler(), true));
        registrations.add(new ConsumerRegistration(EngineAddresses.REPO_FLOW_COMPLETE_INSTANCE,
                instanceManager.completeInstanceHandler(), true));
        registrations.add(new ConsumerRegistration(EngineAddresses.REPO_FLOW_FAIL_INSTANCE,
                instanceManager.failInstanceHandler(), true));

        registrations.add(new ConsumerRegistration(EngineAddresses.REPO_FLOW_CREATE_FLOW_OBJECT_INSTANCE,
                instanceManager.createFlowObjectInstanceHandler(), true));

        registrations.add(new ConsumerRegistration(EngineAddresses.REPO_ASSIGN_TASK,
                userManager.assignTaskHandler()));
        registrations.add(new ConsumerRegistration(EngineAddresses.REPO_UPDATE_TASK,
                userManager.updateTaskHandler()));

        return registrations;
    }
}

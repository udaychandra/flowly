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

package io.flowly.core.verticles;

import io.flowly.core.Failure;
import io.flowly.core.ObjectKeys;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class that assists in the deployment of verticles and registration of message consumers.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class VerticleUtils {
    /**
     * Recursively deploy a list of verticles. Fails fast, i.e. if a verticle fails, the remaining verticles
     * are not deployed.
     *
     * @param verticleDeployments the stack of verticles to be deployed. By the end of the method execution
     *                            the stack will most likely become empty.
     * @param deployedVerticles set that is used to store the deployment ids of verticles.
     * @param vertx vertx platform used to deploy verticles.
     * @param resultHandler result handler that is invoked after verticle deployments are complete.
     */
    public static void deployVerticles(Stack<VerticleDeployment> verticleDeployments,
                                       Set<JsonObject> deployedVerticles,
                                       Vertx vertx, Handler<AsyncResult<Void>> resultHandler) {
        Future<Void> deployedFuture = Future.future();
        deployedFuture.setHandler(resultHandler);

        deployVerticles(verticleDeployments, deployedVerticles, vertx, deployedFuture);
    }

    /**
     * Undeploy verticles based on the given list of deployment ids.
     *
     * @param deployementIds the list of ids associated with deployed verticles.
     * @param vertx vertx platform used to undeploy verticles.
     * @param resultHandler result handler that is invoked after the verticles are undeployed.
     */
    public static void undeployVerticles(Iterator<String> deployementIds, Vertx vertx,
                                         Handler<AsyncResult<Void>> resultHandler) {
        Future<Void> undeployedFuture = Future.future();
        undeployedFuture.setHandler(resultHandler);

        undeployVerticles(deployementIds, vertx, undeployedFuture);
    }

    public static void undeployVerticles(Set<JsonObject> deployedVerticles, Vertx vertx,
                                         Handler<AsyncResult<Void>> resultHandler) {
        Future<Void> undeployedFuture = Future.future();
        undeployedFuture.setHandler(resultHandler);

        List<String> deploymentIds = new ArrayList<>();
        Iterator<JsonObject> iterator = deployedVerticles.iterator();

        while (iterator.hasNext()) {
            deploymentIds.add(iterator.next().getString(ObjectKeys.DEPLOYMENT_ID));
        }

        undeployVerticles(deploymentIds.iterator(), vertx, resultHandler);
    }

    /**
     * Registers all the message handlers in the queue on the event bus.
     *
     * @param eventBus the event bus on which given message handlers are registered.
     * @param logger the logger used to capture registration failures.
     * @param registrations queue of consumer registrations.
     * @param resultHandler handler that is invoked after all handlers are registered on the event bus.
     */
    public static <T> void registerHandlers(EventBus eventBus, Logger logger, Queue<ConsumerRegistration<T>> registrations,
                                        Handler<AsyncResult<Void>> resultHandler) {
        Future<Void> future = Future.future();
        future.setHandler(resultHandler);

        recursivelyRegisterHandlers(eventBus, logger, registrations, future);
    }

    private static void deployVerticles(Stack<VerticleDeployment> verticleDeployments,
                                        Set<JsonObject> deployedVerticles,
                                        Vertx vertx, Future<Void> deployedFuture) {
        VerticleDeployment verticleDeployment = verticleDeployments.pop();

        vertx.deployVerticle(verticleDeployment.getName(), verticleDeployment.getDeploymentOptions(), d -> {
            if (d.succeeded()) {
                // Add the deployment id to the provided map.
                if (deployedVerticles != null) {
                    JsonObject deployedVerticle = new JsonObject().
                            put(ObjectKeys.ID, verticleDeployment.getId()).
                            put(ObjectKeys.DEPLOYMENT_ID, d.result());
                    deployedVerticles.add(deployedVerticle);
                }

                // Convey success if all verticle deployments are complete.
                if (verticleDeployments.empty()) {
                    deployedFuture.complete();
                } else {
                    deployVerticles(verticleDeployments, deployedVerticles, vertx, deployedFuture);
                }
            }
            // Fail fast - if any of the verticle deployment fails, stop further processing.
            else {
                deployedFuture.fail(d.cause());
            }
        });
    }

    private static void undeployVerticles(Iterator<String> deployementIds, Vertx vertx,
                                          Future<Void> undeployedFuture) {
        // Ensure that vertx platform has the deployment ids.
        // The list specified by the caller might be stale.
        List<String> actualDeploymentIds = new ArrayList<>();
        Set<String> vertxDeploymentIds = vertx.deploymentIDs();

        while (deployementIds.hasNext()) {
            String deploymentId = deployementIds.next();
            if (vertxDeploymentIds.contains(deploymentId)) {
                actualDeploymentIds.add(deploymentId);
            }
        }

        if (actualDeploymentIds.isEmpty()) {
            undeployedFuture.complete();
        }
        else {
            AtomicInteger counter = new AtomicInteger(0);

            for (String deploymentId : actualDeploymentIds) {
                vertx.undeploy(deploymentId, d -> {
                    if (d.succeeded()) {
                        if (counter.incrementAndGet() == actualDeploymentIds.size()) {
                            undeployedFuture.complete();
                        }
                    }
                    else if (!undeployedFuture.failed()) {
                        undeployedFuture.fail(d.cause());
                    }
                });
            }
        }
    }

    private static <T> void registerHandlers(EventBus eventBus, Logger logger,
                                             Queue<ConsumerRegistration<T>> registrations, Future<Void> future) {
        ConsumerRegistration<T> registration = registrations.remove();
        if (registration.isLocalOnly()) {
            eventBus.localConsumer(registration.getAddress(), registration.getMessageHandler());
            recursivelyRegisterHandlers(eventBus, logger, registrations, future);
        }
        else {
            eventBus.consumer(registration.getAddress(), registration.getMessageHandler()).
                    completionHandler(result -> {
                        if (result.succeeded()) {
                            recursivelyRegisterHandlers(eventBus, logger, registrations, future);
                        }
                        else {
                            Failure failure = new Failure(100,
                                    "Failed to register handler: " + registration.getAddress(), result.cause());
                            logger.error(failure.getError(), failure.getCause());
                            future.fail(failure);
                        }
                    });
        }
    }

    private static <T> void recursivelyRegisterHandlers(EventBus eventBus, Logger logger,
                                                    Queue<ConsumerRegistration<T>> registrations, Future<Void> future) {
        if (!registrations.isEmpty()) {
            registerHandlers(eventBus, logger, registrations, future);
        }
        else {
            future.complete();
        }
    }
}

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

import io.flowly.core.verticles.ConsumerRegistration;
import io.flowly.core.verticles.VerticleDeployment;
import io.flowly.core.verticles.VerticleUtils;
import io.flowly.engine.App;
import io.flowly.engine.EngineAddresses;
import io.flowly.engine.assets.Flow;
import io.flowly.engine.assets.InteractiveService;
import io.flowly.engine.assets.MicroService;
import io.flowly.engine.assets.Process;
import io.flowly.engine.compilers.Compiler;
import io.flowly.engine.compilers.VerboseCompiler;
import io.flowly.core.Failure;
import io.flowly.core.parser.Parser;
import io.flowly.engine.parser.AssetParser;
import io.flowly.engine.utils.PathUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.LocalMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Parse, compile and deploy individual flows or all flows in an app.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class Build extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(Build.class);

    // Key to the shared map that holds a list of all deployed apps and their flows.
    private static final String DEPLOYED_APPS_KEY = "io.flowly.apps.deployed";

    private FileSystem fileSystem;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        fileSystem = vertx.fileSystem();

        try {
            // Register message handlers.
            VerticleUtils.registerHandlers(vertx.eventBus(), logger, createMessageHandlers(), h -> {
                if (h.succeeded()) {
                    logger.info("Deployed builder verticle");
                    startFuture.complete();
                }
                else {
                    startFuture.fail(h.cause());
                }
            });
        }
        catch (Exception ex) {
            Failure failure = new Failure(4000, "Unable to deploy builder verticle.", ex);
            logger.error(failure.getError(), failure.getCause());
            startFuture.fail(failure);
        }
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        try {
            vertx.sharedData().getLocalMap(DEPLOYED_APPS_KEY).clear();
            stopFuture.complete();

            logger.info("Undeployed builder verticle");
        }
        catch (Exception ex) {
            Failure failure = new Failure(4001, "Unable to close compiler graph database.", ex);
            logger.error(failure.getError(), failure.getCause());
            stopFuture.fail(failure);
        }
    }

    /**
     * Create message handlers to deploy apps or flows.
     *
     * @return queue of consumer registrations.
     */
    private Queue<ConsumerRegistration> createMessageHandlers() {
        Queue<ConsumerRegistration> registrations = new LinkedList<>();
        registrations.add(new ConsumerRegistration(EngineAddresses.DEPLOY_APP, deployAppHandler()));
        registrations.add(new ConsumerRegistration(EngineAddresses.UNDEPLOY_APP, undeployHandler()));

        return registrations;
    }

    private Handler<Message<Object>> deployAppHandler() {
        return message -> {
            App app = App.toApp((JsonObject) message.body());
            LocalMap<String, Boolean> deployedAppsMap = vertx.sharedData().getLocalMap(DEPLOYED_APPS_KEY);
            Boolean appExists = deployedAppsMap.get(app.getId());

            if (appExists != null && appExists) {
                Failure failure = new Failure(4002, "App is already deployed: " + app.getId());
                logger.error(failure.getError());
                message.fail(failure.getCode(), failure.getMessage());
            }
            else {
                // Add the app to the local map.
                deployedAppsMap.put(app.getId(), true);

                String appFolder = app.getAppFolder();
                String verticlesPath = PathUtils.createPath(appFolder, PathUtils.VERTICLES_FOLDER);

                vertx.executeBlocking(future -> {
                    try {
                        prepareVerticlesFolderBlocking(app, verticlesPath);

                        // Parse flows.
                        List<Flow> flows = parseFlowsBlocking(appFolder);

                        // Build flows.
                        buildFlowsBlocking(app, flows, future, deployedAppsMap);
                    }
                    catch (Exception ex) {
                        Failure failure = new Failure(4003, "Unable to compile flow verticles for app: " +
                                app.getId(), ex);
                        logger.error(failure.getError(), failure.getCause());
                        future.fail(failure);
                    }

                }, r -> {
                    if (r.succeeded()) {
                        message.reply(true);
                    }
                    else {
                        message.fail(4003, r.cause().getMessage());
                    }
                });
            }
        };
    }

    private Handler<AsyncResult<Void>> deployFlowsHandler(Future<Object> future, App app,
                                                          Set<JsonObject> deployedVerticles,
                                                          LocalMap<String, Boolean> deployedAppsMap) {
        return deployedHandler -> {
            if (deployedHandler.succeeded()) {
                logger.info("Deployed " + deployedVerticles.size() + " flow verticle(s) for app: " + app.getId());
                future.complete();
            }
            else {
                // Unfortunate, but let's move on. Undeploy the successful ones (like a rollback)
                // and clear the map.
                deployedAppsMap.remove(app.getId());

                VerticleUtils.undeployVerticles(deployedVerticles, vertx, undeployedHandler -> {
                    Failure failure = new Failure(4004,
                            "Unable to compile flow verticles for app: " + app.getId(),
                            deployedHandler.cause());
                    logger.error(failure.getError(), failure.getCause());

                    if (undeployedHandler.failed()) {
                        logger.fatal("Unable to clean up after a failed compilation. App: " + app.getId(),
                                undeployedHandler.cause());
                    }

                    future.fail(failure);
                });
            }
        };
    }

    private Handler<Message<Object>> undeployHandler() {
        return message -> {
            App app = App.toApp((JsonObject) message.body());
            LocalMap<String, Set<String>> deployedAppsMap = vertx.sharedData().getLocalMap(DEPLOYED_APPS_KEY);
            Set<String> deploymentIds = deployedAppsMap.get(app.getId());

            if (deploymentIds == null) {
                Failure failure = new Failure(4005, "App is not deployed: " + app.getId());
                logger.error(failure.getError());
                message.fail(failure.getCode(), failure.getMessage());
            }
            else {
                VerticleUtils.undeployVerticles(deploymentIds.iterator(), vertx, h -> {
                    if (h.failed()) {
                        Failure failure = new Failure(4006, "Unable to undeploy app: " + app.getId(), h.cause());
                        logger.error(failure.getError(), failure.getCause());
                        message.fail(failure.getCode(), failure.getMessage());
                    }
                    else {
                        message.reply(true);
                    }
                });
            }
        };
    }

    private void prepareVerticlesFolderBlocking(App app, String verticlesPath) {
        logger.info("Clean flow verticles in app: " + app.getId() + " at " + verticlesPath);

        // Clean existing flow verticles.
        if (fileSystem.existsBlocking(verticlesPath)) {
            fileSystem.deleteRecursiveBlocking(verticlesPath, true);
        }

        // Create flow verticles folder.
        fileSystem.mkdirBlocking(verticlesPath);
    }

    /**
     * Parse all flows - processes, interactive services and micro services.
     *
     * @return list of flows parsed from JSON files.
     */
    private List<Flow> parseFlowsBlocking(String appFolder) {
        List<Flow> flows = new ArrayList<>();

        // Parse all flow definitions.
        Parser parser = new AssetParser(fileSystem);
        List<String> filePaths;

        filePaths = fileSystem.readDirBlocking(PathUtils.createPath(appFolder, PathUtils.PROCESSES_FOLDER));
        for (String filePath : filePaths) {
            flows.add(parser.parseBlocking(filePath, Process.class));
        }

        filePaths = fileSystem.readDirBlocking(PathUtils.createPath(appFolder, PathUtils.INTERACTIVE_SERVICES_FOLDER));
        for (String filePath : filePaths) {
            flows.add(parser.parseBlocking(filePath, InteractiveService.class));
        }

        filePaths = fileSystem.readDirBlocking(PathUtils.createPath(appFolder, PathUtils.MICRO_SERVICES_FOLDER));
        for (String filePath : filePaths) {
            flows.add(parser.parseBlocking(filePath, MicroService.class));
        }

        return flows;
    }

    /**
     * Compile the flows to JavaScript verticles and prepare for flow deployments.
     *
     * @param flows the list of flows to be compiled and written to the file system as JavaScript verticles.
     * @return list of verticles to be deployed.
     */
    private Stack<VerticleDeployment> compileFlowsBlocking(App app, List<Flow> flows) {
        Stack<VerticleDeployment> verticleDeployments = new Stack<>();
        DeploymentOptions deploymentOptions = new DeploymentOptions();
        Compiler compiler = new VerboseCompiler(fileSystem);

        for (Flow flow : flows) {
            StringBuilder output = new StringBuilder();
            flow.setApp(app);
            flow.compile(compiler, null, output);

            verticleDeployments.push(new VerticleDeployment(flow.getId(),
                    "js:" + compiler.writeFlow(flow, output), deploymentOptions));
        }

        return verticleDeployments;
    }

    private void buildFlowsBlocking(App app, List<Flow> flows, Future<Object> future,
                                    LocalMap<String, Boolean> deployedAppsMap) {
        if (flows.isEmpty()) {
            future.complete();
        }
        else {
            // Compile flows and prepare for verticle deployments (JavaScript files).
            Stack<VerticleDeployment> flowDeployments = compileFlowsBlocking(app, flows);
            logger.info("Compiled flow verticles count in app: " + app.getId() + " are: " + flows.size());

            // Persist flow and its router.
            saveFlows(flows, resultHandler -> {
                if (resultHandler.succeeded()) {
                    Set<JsonObject> deployedVerticles = new HashSet<>();

                    // Deploy compiled flows.
                    VerticleUtils.deployVerticles(flowDeployments, deployedVerticles,
                            vertx, deployFlowsHandler(future, app, deployedVerticles, deployedAppsMap));
                }
                else {
                    future.fail(resultHandler.cause());
                }
            });
        }
    }

    private void saveFlows(List<Flow> flows, Handler<AsyncResult<Void>> resultHandler) {
        Future<Void> future = Future.future();
        future.setHandler(resultHandler);

        // Save flow and its router in the repository.
        AtomicInteger counter = new AtomicInteger(0);

        for (Flow flow : flows) {
            vertx.eventBus().send(EngineAddresses.REPO_FLOW_SAVE, flow, reply -> {
                if (reply.succeeded() && (Boolean) reply.result().body()) {
                    if (counter.incrementAndGet() == flows.size()) {
                        future.complete();
                    }
                }
                else if (!future.failed()) {
                    future.fail(reply.cause());
                }
            });
        }
    }
}
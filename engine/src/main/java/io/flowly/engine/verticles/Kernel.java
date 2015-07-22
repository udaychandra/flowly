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
import io.flowly.core.data.FlowInstance;
import io.flowly.core.data.FlowInstanceMetadata;
import io.flowly.core.data.FlowMetadata;
import io.flowly.core.verticles.VerticleDeployment;
import io.flowly.engine.App;
import io.flowly.engine.assets.InteractiveService;
import io.flowly.engine.assets.MicroService;
import io.flowly.engine.assets.Process;
import io.flowly.engine.EngineAddresses;
import io.flowly.engine.JsonKeys;
import io.flowly.core.verticles.VerticleUtils;
import io.flowly.core.codecs.FlowInstanceCodec;
import io.flowly.core.codecs.FlowInstanceMetadataCodec;
import io.flowly.engine.codecs.FlowInstanceWrapperCodec;
import io.flowly.core.codecs.FlowMetadataCodec;
import io.flowly.engine.codecs.InteractiveServiceCodec;
import io.flowly.engine.codecs.MicroServiceCodec;
import io.flowly.engine.codecs.ProcessCodec;
import io.flowly.engine.codecs.RouteCodec;
import io.flowly.engine.data.FlowInstanceWrapper;
import io.flowly.core.parser.JsonParser;
import io.flowly.core.parser.Parser;
import io.flowly.engine.router.Route;
import io.flowly.engine.utils.PathUtils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents flowly's kernel. Spawns multiple verticles to build,
 * deploy and run flowly apps.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class Kernel extends AbstractVerticle {
    public static final Map<String, DeliveryOptions> DELIVERY_OPTIONS;

    static {
        DELIVERY_OPTIONS = new HashMap<>();
        DELIVERY_OPTIONS.put(FlowMetadataCodec.NAME, new DeliveryOptions().setCodecName(FlowMetadataCodec.NAME));
        DELIVERY_OPTIONS.put(FlowInstanceMetadataCodec.NAME,
                new DeliveryOptions().setCodecName(FlowInstanceMetadataCodec.NAME));
        DELIVERY_OPTIONS.put(FlowInstanceCodec.NAME,
                new DeliveryOptions().setCodecName(FlowInstanceCodec.NAME));
    }

    private static final Logger logger = LoggerFactory.getLogger(Kernel.class);

    private String appsDirectory;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        appsDirectory = config().getString(JsonKeys.APPS_DIRECTORY);

        // Register message codecs on the event bus.
        vertx.eventBus().registerDefaultCodec(Process.class, new ProcessCodec());
        vertx.eventBus().registerDefaultCodec(InteractiveService.class, new InteractiveServiceCodec());
        vertx.eventBus().registerDefaultCodec(MicroService.class, new MicroServiceCodec());
        vertx.eventBus().registerDefaultCodec(Route.class, new RouteCodec());
        vertx.eventBus().registerDefaultCodec(FlowInstanceWrapper.class, new FlowInstanceWrapperCodec());
        vertx.eventBus().registerDefaultCodec(FlowInstance.class, new FlowInstanceCodec());
        vertx.eventBus().registerDefaultCodec(FlowMetadata.class, new FlowMetadataCodec());
        vertx.eventBus().registerDefaultCodec(FlowInstanceMetadata.class, new FlowInstanceMetadataCodec());

        deploySystemVerticles(startFuture);
    }

    /**
     * Deploy the engine's system services (verticles).
     *
     * @param startFuture a future action used to indicate whether the kernel was successfully deployed or not.
     */
    private void deploySystemVerticles(Future<Void> startFuture) {
        // Set the deployment configurations that are common for all system verticles.
        DeploymentOptions normalDeploymentOptions = new DeploymentOptions().setConfig(config());
        DeploymentOptions workerDeploymentOptions = new DeploymentOptions().setConfig(config()).setWorker(true);
        Stack<VerticleDeployment> systemVerticles = new Stack<>();

        // Add all the system verticles.
        systemVerticles.push(new VerticleDeployment(Repository.class.getName(), null, workerDeploymentOptions));
        systemVerticles.push(new VerticleDeployment(Build.class.getName(), null, normalDeploymentOptions));
        systemVerticles.push(new VerticleDeployment(Engine.class.getName(), null, normalDeploymentOptions));

        VerticleUtils.deployVerticles(systemVerticles, null, vertx, d -> {
            if (d.succeeded()) {
                logger.info("Global system verticles deployed.");

                if (config().getBoolean(JsonKeys.SCAN_APPS_ON_KERNEL_START, true)) {
                    deployApps(startFuture);
                }
                else {
                    startFuture.complete();
                }
            }
            else {
                Failure failure = new Failure(1000, "Failed to deploy global system verticles", d.cause());
                logger.error(failure.getError(), failure.getCause());
                startFuture.fail(failure);
            }
        });
    }

    private void deployApps(Future<Void> startFuture) {
        vertx.executeBlocking(f -> {
            List<Path> appConfigFilePaths = PathUtils.findAppConfigFilePaths(appsDirectory);
            Parser parser = new JsonParser(vertx.fileSystem());

            if (appConfigFilePaths.size() > 0) {
                AtomicInteger counter = new AtomicInteger(0);

                // Wait for all apps to deploy.
                for (Path appConfigFilePath : appConfigFilePaths) {
                    App app = parser.parseBlocking(appConfigFilePath.toString(), App.class);
                    app.setAppRootFolder(appsDirectory);

                    vertx.eventBus().send(EngineAddresses.DEPLOY_APP, app.toJson(), reply -> {
                        if (reply.succeeded()) {
                            if (counter.incrementAndGet() == appConfigFilePaths.size()) {
                                f.complete();
                            }
                        }
                        else if (!f.failed()) {
                            f.fail(reply.cause());
                        }
                    });
                }
            }
            else {
                f.complete();
            }

        }, false, r -> {
            if (r.succeeded()) {
                startFuture.complete();
            }
            else {
                startFuture.fail(r.cause());
            }
        });
    }
}
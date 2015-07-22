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

import io.flowly.engine.App;
import io.flowly.engine.BaseTestWithVertx;
import io.flowly.engine.EngineAddresses;
import io.flowly.engine.JsonKeys;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;

/**
 * @author <a>Uday Tatiraju</a>
 */
public abstract class BaseVerticleTest extends BaseTestWithVertx {
    protected JsonObject config;

    public void before() {
        // TODO: @After annotation not working. Ask vertx DEV group.
        if (vertx != null) {
            vertx.close();
        }

        vertx = Vertx.vertx();
        config = new JsonObject().
                put(JsonKeys.APPS_DIRECTORY, appsDirectory).
                put(JsonKeys.DB_STORAGE_DIRECTORY, storageDirectory).
                put(JsonKeys.PUBLISH_FLOW_LIFE_CYCLE_EVENTS, true).
                put(JsonKeys.SCAN_APPS_ON_KERNEL_START, false);
    }

    protected void deployKernelVerticle(TestContext context, Handler<String> handler) {
        DeploymentOptions options = new DeploymentOptions().setConfig(config);

        if (handler == null) {
            vertx.deployVerticle(new Kernel(), options, context.asyncAssertSuccess());
        }
        else {
            vertx.deployVerticle(new Kernel(), options, context.asyncAssertSuccess(handler));
        }
    }

    protected void deployApp(String appId, Handler<AsyncResult<Message<Object>>> handler) {
        App app = new App(appId, appsDirectory);
        vertx.eventBus().send(EngineAddresses.DEPLOY_APP, app.toJson(), handler);
    }
}

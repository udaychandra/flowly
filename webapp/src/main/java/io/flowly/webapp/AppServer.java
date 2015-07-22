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

package io.flowly.webapp;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Public facing flowly web server.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class AppServer extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(AppServer.class);

    private static final String APP_ROOT = "app";
    private static final String STATIC_FILES_PATTERN = "^.*\\.(css|js|ico)$";

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        StaticHandler staticHandler = StaticHandler.create().setWebRoot(APP_ROOT);

        router.routeWithRegex(HttpMethod.GET, STATIC_FILES_PATTERN).handler(staticHandler);

        FlowApiRouter apiRouter = new FlowApiRouter(vertx);
        router.mountSubRouter("/api", apiRouter.getRouter());

        // TODO: Configure port.
        server.requestHandler(router::accept).listen(8080);
        startFuture.complete();
    }
}

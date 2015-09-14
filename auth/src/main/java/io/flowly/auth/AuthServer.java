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

import io.flowly.auth.router.GroupRouter;
import io.flowly.auth.router.ResourceRouter;
import io.flowly.auth.router.UserRouter;
import io.flowly.core.Failure;
import io.flowly.core.ObjectKeys;
import io.flowly.core.security.Vault;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Public facing access management web server.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class AuthServer extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(AuthServer.class);

    private static final String STATIC_FILES_PATTERN = "^.*\\.(html|css|js|ico)$";

    private JWTAuth authProvider;

    @Override
    public void start(Future<Void> startFuture) {
        try {
            vertx.deployVerticle(new AccessVerticle(),
                    new DeploymentOptions().setConfig(config()).setWorker(true), initServer(startFuture));
        }
        catch (Exception ex) {
            Failure failure = new Failure(101, "Unable to start auth server.", ex);
            logger.error(failure.getMessage(), failure.getCause());
            startFuture.fail(failure);
        }
    }

    private Handler<AsyncResult<String>> initServer(Future<Void> startFuture) {
        return h -> {
            if (h.succeeded()) {
                initAuthProvider(config());

                // TODO: URGENT - Make this https.
                HttpServer server = vertx.createHttpServer();
                Router router = Router.router(vertx);
                router.route().handler(BodyHandler.create());

                // TODO: Add patterns from config.
                Set<HttpMethod> allowedMethods = new HashSet<>();
                allowedMethods.add(HttpMethod.GET);
                allowedMethods.add(HttpMethod.POST);

                router.route().handler(CorsHandler.create("http://localhost:.*").
                        allowedHeader("Authorization").
                        allowedMethods(allowedMethods).allowCredentials(true));

                StaticHandler staticHandler = StaticHandler.create();
                router.routeWithRegex(HttpMethod.GET, STATIC_FILES_PATTERN).handler(staticHandler);

                new UserRouter(vertx, authProvider, router);
                new GroupRouter(vertx, authProvider, router);
                new ResourceRouter(vertx, authProvider, router);

                int port = config().getInteger(ObjectKeys.HTTP_PORT);
                server.requestHandler(router::accept).listen(port);
                logger.info("Auth server running on port: " + port);

                startFuture.complete();
            }
            else {
                startFuture.fail(h.cause());
            }
        };
    }

    private void initAuthProvider(JsonObject config) {
        JsonObject authConfig = new JsonObject().put(ObjectKeys.KEY_STORE, new JsonObject()
                .put("path", config.getString(ObjectKeys.VAULT_PATH))
                .put("type", Vault.JCEKS_KEY_STORE)
                .put("password", config.getString(ObjectKeys.VAULT_KEY)));

        authProvider = new ExtJwtAuthProvider(vertx, authConfig);
    }
}

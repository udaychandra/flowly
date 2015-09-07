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

package io.flowly.auth.router;

import io.flowly.core.router.BaseRouter;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.JWTAuthHandler;

/**
 * Defines routes for common CRUD operations on users, groups and resources.
 *
 * @author <a>Uday Tatiraju</a>
 */
public abstract class CrudRouter extends BaseRouter {
    public static final String AUTH_API_READ_ACCESS = "AuthApi:R";
    public static final String AUTH_API_WRITE_ACCESS = "AuthApi:W";

    protected JWTAuth authProvider;

    public CrudRouter(Vertx vertx, JWTAuth authProvider, Router router, String basePath) {
        super(router, vertx.eventBus());

        this.authProvider = authProvider;

        router.route(HttpMethod.GET, basePath).
                handler(JWTAuthHandler.create(authProvider).addAuthority(AUTH_API_READ_ACCESS));
        router.route(HttpMethod.POST, basePath).
                handler(JWTAuthHandler.create(authProvider).addAuthority(AUTH_API_WRITE_ACCESS));
    }
}

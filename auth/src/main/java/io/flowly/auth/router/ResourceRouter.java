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

import io.flowly.auth.LocalAddresses;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.JWTAuthHandler;

/**
 * @author <a>Uday Tatiraju</a>
 */
public class ResourceRouter extends CrudRouter {
    public ResourceRouter(Vertx vertx, JWTAuth authProvider, Router router) {
        super(vertx, authProvider, router, "/resource/*");

        prepareSearchRoute("/resource/search", LocalAddresses.SEARCH_RESOURCE, "Unable to search for resources.");
        prepareRoute(HttpMethod.POST, "/resource/create", LocalAddresses.CREATE_RESOURCE,
                "Unable to create resource.", false);
        prepareRoute(HttpMethod.POST, "/resource/update", LocalAddresses.UPDATE_RESOURCE,
                "Unable to update resource.", false);
        prepareRoute(HttpMethod.GET, "/resource/get", LocalAddresses.GET_RESOURCE, "Unable to get resource.", true);
    }
}

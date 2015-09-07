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
public class GroupRouter extends CrudRouter {
    public GroupRouter(Vertx vertx, JWTAuth authProvider, Router router) {
        super(vertx, authProvider, router, "/group/*");

        prepareSearchRoute("/group/search", LocalAddresses.SEARCH_GROUP, "Unable to search for groups.");
        prepareRoute(HttpMethod.POST, "/group/create", LocalAddresses.CREATE_GROUP, "Unable to create group.", false);
        prepareRoute(HttpMethod.POST, "/group/update", LocalAddresses.UPDATE_GROUP, "Unable to update group.", false);
        prepareRoute(HttpMethod.GET, "/group/get", LocalAddresses.GET_GROUP, "Unable to get group.", true);
    }
}

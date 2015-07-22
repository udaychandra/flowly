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

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

/**
 * @author <a>Uday Tatiraju</a>
 */
public abstract class BaseRouter {
    public static final String JSON_CONTENT_TYPE = "application/json";

    public void writeResponse(RoutingContext routingContext, String body) {
        HttpServerResponse response = routingContext.response();
        response.setChunked(true);
        response.write(body).end();
    }
}

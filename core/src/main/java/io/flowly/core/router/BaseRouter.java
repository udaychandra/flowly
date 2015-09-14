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

package io.flowly.core.router;

import io.flowly.core.ObjectKeys;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Holds common functionality used by all end points.
 *
 * @author <a>Uday Tatiraju</a>
 */
public abstract class BaseRouter {
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String JSON_CONTENT_TYPE = "application/json";

    private static final Logger logger = LoggerFactory.getLogger(BaseRouter.class);

    protected Router router;
    protected EventBus eventBus;

    public BaseRouter(Router router, EventBus eventBus) {
        this.router = router;
        this.eventBus = eventBus;
    }

    protected JsonObject getSearchArgs(RoutingContext routingContext) {
        JsonObject args = new JsonObject();
        args.put(ObjectKeys.PAGE_NUMBER,
                Integer.parseInt(routingContext.request().getParam(ObjectKeys.PAGE_NUMBER)));
        args.put(ObjectKeys.PAGE_SIZE,
                Integer.parseInt(routingContext.request().getParam(ObjectKeys.PAGE_SIZE)));

        return args;
    }

    protected <T> void prepareRoute(HttpMethod httpMethod, String path, String address,
                                                       String error, boolean chunkedResponse) {
        router.route(httpMethod, path).handler(requestHandler -> {
            try {
                eventBus.<T>send(address, requestHandler.getBodyAsJson(), reply -> {
                    T result = reply.result().body();

                    if (result != null) {
                        writeSuccessResponse(requestHandler, result.toString(), chunkedResponse);
                    }
                    else {
                        writeErrorResponse(requestHandler, error);
                    }
                });
            }
            catch (Exception ex) {
                logger.error(error, ex);
                writeErrorResponse(requestHandler, error);
            }
        });
    }

    protected void prepareSearchRoute(String path, String address, String errorMessage) {
        router.route(HttpMethod.GET, path).handler(requestHandler -> {
            try {
                eventBus.<JsonArray>send(address, getSearchArgs(requestHandler), reply -> {
                    JsonArray results = reply.result().body();

                    if (results != null) {
                        writeSuccessResponse(requestHandler, results.encode(), true);
                    }
                    else {
                        writeErrorResponse(requestHandler, errorMessage);
                    }
                });
            }
            catch (Exception ex) {
                logger.error(errorMessage, ex);
                writeErrorResponse(requestHandler, errorMessage);
            }
        });
    }

    public static void writeSuccessResponse(RoutingContext routingContext, String body, boolean chunked) {
        HttpServerResponse response = routingContext.response();

        if (chunked) {
            response.setChunked(true);
        }
        else {
            setContentLength(response, body);
        }

        response.write(body).end();
    }

    public static void writeErrorResponse(RoutingContext routingContext, String body) {
        HttpServerResponse response = routingContext.response();
        setContentLength(response, body);
        response.setStatusCode(500).write(body).end();
    }

    public static void setContentLength(HttpServerResponse response, String body) {
        response.putHeader(CONTENT_LENGTH, "" + body.length());
    }
}

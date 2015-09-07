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

import io.flowly.core.ObjectKeys;
import io.flowly.core.codecs.FlowInstanceCodec;
import io.flowly.core.data.FlowInstance;
import io.flowly.core.verticles.ClusterAddresses;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * Reusable router that exposes REST end-points to invoke flowly API.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class FlowApiRouter extends BaseRouter {
    private static final Logger logger = LoggerFactory.getLogger(FlowApiRouter.class);

    private Router router;

    public FlowApiRouter(Vertx vertx) {
        FlowInstanceCodec flowInstanceCodec = new FlowInstanceCodec();
        vertx.eventBus().registerDefaultCodec(FlowInstance.class, flowInstanceCodec);

        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        prepareGetInboxRoute(router, vertx);
        prepareGetFlowsRoute(router, vertx);
        prepareStartFlowRoute(router, vertx);

        prepareStartFlowTaskRoute(router, vertx);
        prepareGetFlowTaskRoute(router, vertx);
        prepareSaveFlowTaskRoute(router, vertx);
    }

    public Router getRouter() {
        return router;
    }

    private void prepareGetInboxRoute(Router api, Vertx vertx) {
        Route getInboxRoute = api.route(HttpMethod.GET, "/inbox/:subjectId").produces(JSON_CONTENT_TYPE);
        getInboxRoute.handler(routingContext -> {
            String subjectId = routingContext.request().getParam(ObjectKeys.SUBJECT_ID);
            logger.info("Get inbox request received: " + subjectId);

            JsonObject args = new JsonObject();
            args.put(ObjectKeys.SUBJECT_ID, subjectId);

            String pageNumber = routingContext.request().getParam(ObjectKeys.PAGE_NUMBER);
            if (pageNumber != null) {
                args.put(ObjectKeys.PAGE_NUMBER, Integer.parseInt(pageNumber));
            }

            String pageSize = routingContext.request().getParam(ObjectKeys.PAGE_SIZE);
            if (pageSize != null) {
                args.put(ObjectKeys.PAGE_SIZE, Integer.parseInt(pageSize));
            }

            vertx.eventBus().send(ClusterAddresses.GET_USER_INBOX, args, reply -> {
                JsonObject inbox = (JsonObject) reply.result().body();
                writeResponse(routingContext, inbox.encode());
            });
        });
    }

    private void prepareStartFlowRoute(Router api, Vertx vertx) {
        Route startFlowRoute = api.route(HttpMethod.POST, "/flow/start");
        startFlowRoute.handler(routingContext -> {
            JsonObject flowMetadata = routingContext.getBodyAsJson();

            logger.info("Start flow request received: " + flowMetadata);

            vertx.eventBus().send(ClusterAddresses.START_FLOW_INSTANCE, flowMetadata, reply -> {
                Long instanceId = (Long) reply.result().body();
                writeResponse(routingContext, instanceId.toString());
            });
        });
    }

    private void prepareStartFlowTaskRoute(Router api, Vertx vertx) {
        Route startFlowTaskRoute = api.route(HttpMethod.POST, "/flow/task/start/:taskId");
        startFlowTaskRoute.handler(routingContext -> {
            Long taskId = Long.parseLong(routingContext.request().getParam(ObjectKeys.TASK_ID));

            logger.info("Start flow task request received: " + taskId);

            vertx.eventBus().send(ClusterAddresses.START_FLOW_INSTANCE_TASK, taskId);
            writeResponse(routingContext, taskId.toString());
        });
    }

    private void prepareGetFlowTaskRoute(Router api, Vertx vertx) {
        Route getFlowTaskRoute = api.route(HttpMethod.GET, "/flow/task/get/:taskId");
        getFlowTaskRoute.handler(routingContext -> {
            Long taskId = Long.parseLong(routingContext.request().getParam(ObjectKeys.TASK_ID));

            logger.info("Get flow task request received: " + taskId);

            vertx.eventBus().send(ClusterAddresses.GET_FLOW_INSTANCE_TASK, taskId, reply -> {
                writeResponse(routingContext, ((JsonObject) reply.result().body()).encode());
            });
        });
    }

    private void prepareSaveFlowTaskRoute(Router api, Vertx vertx) {
        Route saveFlowTaskRoute = api.route(HttpMethod.POST, "/flow/task/save/:taskId");
        saveFlowTaskRoute.handler(routingContext -> {
            Long taskId = Long.parseLong(routingContext.request().getParam(ObjectKeys.TASK_ID));
            FlowInstance task = new FlowInstance(routingContext.getBodyAsJson().getMap());

            logger.info("Save flow task request received: " + taskId);

            vertx.eventBus().send(ClusterAddresses.SAVE_FLOW_INSTANCE_TASK, task);
        });
    }

    private void prepareGetFlowsRoute(Router api, Vertx vertx) {
        Route getFlowsRoute = api.route(HttpMethod.GET, "/flows").produces(JSON_CONTENT_TYPE);
        getFlowsRoute.handler(routingContext -> {
            String subjectId = routingContext.request().getParam(ObjectKeys.SUBJECT_ID);
            logger.info("Get flows request received: " + subjectId);

            vertx.eventBus().send(ClusterAddresses.GET_USER_FLOWS, subjectId, reply -> {
                JsonArray flows = (JsonArray) reply.result().body();
                writeResponse(routingContext, flows.encode());
            });
        });
    }
}

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

package io.flowly.engine.data.manager;

import io.flowly.core.data.FlowInstanceMetadata;
import io.flowly.core.data.FlowInstanceStep;
import io.flowly.core.data.FlowMetadata;
import io.flowly.engine.router.Route;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;


import java.util.List;

/**
 * Defines get operations related to flows defined in an app - processes and services.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class FlowReadManager extends BaseManager {
    private static final Logger logger = LoggerFactory.getLogger(FlowReadManager.class);

    public FlowReadManager(Graph graph) {
        super(graph);
    }

    public Handler<Message<Object>> flowNextRouteHandler() {
        return message -> {
            FlowInstanceMetadata metadata = (FlowInstanceMetadata) message.body();
            message.reply(nextRoute(metadata));
        };
    }

    public Route nextRoute(FlowInstanceMetadata metadata) {
        Route route = new Route();
        FlowInstanceStep currentStep = metadata.getCurrentStep();

        try {
            route.setPreviousFlowObjectId(currentStep.getFlowObjectId());

            GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V();
            traversal.has(Schema.V_P_CURRENT_FLOW_OBJECT_ID, currentStep.getFlowObjectId()).
                    has(Schema.V_P_ROUTE_FLOW_ID, metadata.getFlowId());

            List<String> connectingObjectIds = currentStep.getConnectingObjectIds();

            if (connectingObjectIds != null) {
                traversal.has(Schema.V_P_CONNECTING_OBJECT_ID, P.within(connectingObjectIds));
            }

            for (Vertex routeVertex : traversal.toList()) {
                VertexProperty<String> subFlowId = routeVertex.property(Schema.P_SUB_FLOW_ID);
                route.addNext((String) routeVertex.property(Schema.V_P_NEXT_FLOW_OBJECT_ID).value(),
                        subFlowId.isPresent() ? subFlowId.value() : null);

                route.setValid(true);
            }

            commit();
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to get next route for flow: " + metadata.getFlowId(), ex);
            route.setValid(false);
        }

        return route;
    }

    public Handler<Message<Object>> getFlowsHandler() {
        return message -> {
            String userId = (String) message.body();
            message.reply(getFlows(userId));
        };
    }

    public JsonArray getFlows(String subjectId) {
        JsonArray flows = new JsonArray();

        try {
            for (Vertex flowVertex : graph.traversal().V().has(Schema.V_P_FLOW_ID).toList()) {
                FlowMetadata flowMetadata = new FlowMetadata();
                flowMetadata.setFlowId((String) flowVertex.property(Schema.V_P_FLOW_ID).value());
                flowMetadata.setFlowType((String) flowVertex.property(Schema.V_P_FLOW_TYPE).value());
                flowMetadata.setAppId((String) flowVertex.property(Schema.V_P_APP_ID).value());

                flows.add(flowMetadata);
            }

            commit();
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to get flows for: " + subjectId, ex);
            flows.clear();
        }

        return flows;
    }
}

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

import io.flowly.engine.assets.CompiledFlow;
import io.flowly.engine.assets.ConnectingObject;
import io.flowly.engine.assets.Flow;
import io.flowly.engine.assets.FlowObject;
import io.flowly.engine.assets.SubFlow;
import io.flowly.engine.assets.View;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Handles save operations of flow metadata and flow routes.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class FlowReadWriteManager extends FlowReadManager {
    private static final Logger logger = LoggerFactory.getLogger(FlowReadWriteManager.class);

    public FlowReadWriteManager(Graph graph) {
        super(graph);
    }

    public Handler<Message<Object>> saveFlowHandler() {
        return message -> {
            Flow flow = (Flow) message.body();
            message.reply(saveFlowMetadata(flow) && saveRouter(flow));
        };
    }

    public Handler<Message<Object>> deleteFlowHandler() {
        return message -> {
            Flow flow = (Flow) message.body();
            message.reply(deleteFlowMetada(flow) && deleteRouter(flow));
        };
    }

    public boolean saveFlowMetadata(Flow flow) {
        try {
            deleteFlowMetadataVertex(flow);

            // Create flow vertex.
            Vertex flowMetadataVertex = graph.addVertex(Schema.V_FLOW_METADATA);
            flowMetadataVertex.property(Schema.V_P_FLOW_ID, flow.getId());
            flowMetadataVertex.property(Schema.V_P_FLOW_TYPE, flow.getType());
            flowMetadataVertex.property(Schema.V_P_APP_ID, flow.getApp().getId());
            // TODO: Add flow properties like isPublic, isProtected, type.

            commit();
            return true;
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to save flow: " + flow.getId(), ex);
            return false;
        }
    }

    public  boolean deleteFlowMetada(Flow flow) {
        try {
            deleteFlowMetadataVertex(flow);
            commit();
            return true;
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to delete flow: " + flow.getId(), ex);
            return false;
        }
    }

    public boolean saveRouter(Flow flow) {
        try {
            CompiledFlow compiledFlow = new CompiledFlow(flow);
            String flowId = flow.getId();

            if (!compiledFlow.isValid()) {
                logger.info(compiledFlow.getInvalidMessage());
                return false;
            }

            // Remove existing routes.
            deleteRouteVertices(flow.getId());

            // Add start route for flow.
            createRouteVertex(flowId, "0", compiledFlow.getStartEvent().getId(), "0", null);

            // Loop through the connecting objects and load the routes.
            for (ConnectingObject connectingObject : flow.getConnectingObjects()) {
                String subFlowId = null;
                FlowObject flowObject = compiledFlow.getFlowObjectMap().get(connectingObject.getToId());

                if (flowObject instanceof SubFlow) {
                    subFlowId = ((SubFlow) flowObject).getSubFlowId();
                }
                else if (flowObject instanceof View) {
                    subFlowId = ((View) flowObject).getRefViewId();
                }

                createRouteVertex(flowId, connectingObject.getFromId(), connectingObject.getToId(),
                        connectingObject.getId(), subFlowId);
            }

            for (FlowObject flowObject : compiledFlow.getEndEvents()) {
                createRouteVertex(flowId, flowObject.getId(), "-1", "0", null);
            }

            commit();

            logger.info("Router created and saved for flow: " + flowId);
            return true;
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to create and save router for flow: " + flow.getId(), ex);
            return false;
        }
    }

    public boolean deleteRouter(Flow flow) {
        try {
            deleteRouteVertices(flow.getId());
            commit();
            return true;
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to delete flow router: " + flow.getId(), ex);
            return false;
        }
    }

    private void createRouteVertex(String flowId, String currentFlowObjectId, String nextFlowObjectId,
                                   String connectingObjectId, String subFlowId) {
        Vertex routeVertex = graph.addVertex(Schema.V_FLOW_ROUTE);

        routeVertex.property(Schema.V_P_ROUTE_FLOW_ID, flowId);
        routeVertex.property(Schema.V_P_CURRENT_FLOW_OBJECT_ID, currentFlowObjectId);
        routeVertex.property(Schema.V_P_NEXT_FLOW_OBJECT_ID, nextFlowObjectId);
        routeVertex.property(Schema.V_P_CONNECTING_OBJECT_ID, connectingObjectId);

        if (subFlowId != null) {
            routeVertex.property(Schema.P_SUB_FLOW_ID, subFlowId);
        }
    }

    private void deleteRouteVertices(String flowId) {
        for (Vertex routeVertex : graph.traversal().V().has(Schema.V_P_ROUTE_FLOW_ID, flowId).toList()) {
            routeVertex.remove();
        }
    }

    private void deleteFlowMetadataVertex(Flow flow) {
        for (Vertex flowVertex : graph.traversal().V().has(Schema.V_P_FLOW_ID, flow.getId()).toList()) {
            flowVertex.remove();
        }
    }
}

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

import io.flowly.core.data.FlowInstance;
import io.flowly.core.data.FlowInstanceMetadata;
import io.flowly.core.data.FlowInstanceStep;
import io.flowly.core.data.FlowMetadata;
import io.flowly.core.codecs.FlowInstanceCodec;
import io.flowly.engine.data.FlowInstanceWrapper;
import io.flowly.engine.router.Route;
import io.flowly.engine.verticles.Kernel;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Handles create, update and delete operations on flow instances.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class FlowInstanceReadWriteManager extends FlowInstanceReadManager {
    private static final Logger logger = LoggerFactory.getLogger(FlowInstanceReadWriteManager.class);

    public FlowInstanceReadWriteManager(Graph graph) {
        super(graph);
    }

    public Handler<Message<Object>> createInstanceHandler() {
        return message -> {
            FlowMetadata flowMetadata = (FlowMetadata) message.body();
            DeliveryOptions options = Kernel.DELIVERY_OPTIONS.get(FlowInstanceCodec.NAME);
            message.reply(createInstance(flowMetadata), options);
        };
    }

    /**
     * Create an instance of the given flow. If the flow is persistence enabled (ex: process),
     * create an instance vertex in the graph and retrieve the instance id.
     *
     * @param flowMetadata meta data that describes the flow.
     * @return an instance of the given flow.
     */
    public FlowInstance createInstance(FlowMetadata flowMetadata) {
        try {
            Vertex processVertex = graph.addVertex(Schema.V_PROCESS_INSTANCE);
            addFlowObjectProperties(processVertex, flowMetadata.getFlowId(), null);
            Long instanceId = (Long) processVertex.id();
            commit();

            return new FlowInstance(instanceId, flowMetadata);
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to create a new flow instance vertex. Flow: " + flowMetadata.getFlowId(), ex);
            return null;
        }
    }

    public Handler<Message<Object>> createFlowObjectInstanceHandler() {
        return message -> {
            FlowInstanceWrapper wrapper = (FlowInstanceWrapper) message.body();
            message.reply(createFlowObjectInstance(wrapper.getInstance(), wrapper.getNext(), wrapper.isStart()));
        };
    }

    /**
     * Start or move a given flow or sub-flow instance by conditionally ending the current flow object instance (token)
     * and preparing a new one based on the next route.
     *
     * @param instance represents the flow or sub-flow instance.
     * @param next the next flow object instance to create.
     * @param isStart indicates if the current token (if any) is to be completed (move) or not.
     * @return truthy value that indicates whether flow object instance was created or not.
     */
    public Long createFlowObjectInstance(FlowInstance instance, Route.Next next, boolean isStart) {
        FlowInstanceMetadata metadata = instance.getMetadata();
        FlowInstanceStep currentStep = metadata.getCurrentStep();
        Long instanceId = metadata.getInstanceId();

        try {
            Vertex fromVertex;

            // Start a new flow instance.
            if (currentStep == null) {
                fromVertex = getVertex(instanceId);
            }
            else {
                fromVertex = getVertex(currentStep.getFlowObjectInstanceId());

                // If it is a sub-flow, set the status of the flow object instance to in-progress.
                String status = isStart ?  STATUS_IN_PROGRESS : STATUS_COMPLETED;

                updateFlowObjectProperties(fromVertex, instance, status, isStart);
            }

            Vertex toVertex = graph.addVertex(Schema.V_FLOW_OBJECT_INSTANCE);
            addFlowObjectProperties(toVertex, next.getFlowObjectId(), next.getSubFlowId());

            String edgeLabel = (currentStep != null && isStart) ? Schema.E_FLOW_INTO : Schema.E_FLOW_TO;
            fromVertex.addEdge(edgeLabel, toVertex);

            Long flowObjectInstanceId = (Long) toVertex.id();
            commit();
            return flowObjectInstanceId;
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to create flow object instance token: " + instanceId, ex);
            return null;
        }
    }

    public Handler<Message<Object>> completeInstanceHandler() {
        return message -> {
            FlowInstance instance = (FlowInstance) message.body();
            message.reply(completeInstance(instance));
        };
    }

    /**
     * End given flow or sub-flow instance.
     *
     * @param instance represents the flow or sub-flow instance.
     * @return truthy value that indicates whether flow or sub-flow instance was marked as complete.
     */
    public boolean completeInstance(FlowInstance instance) {
        FlowInstanceMetadata metadata = instance.getMetadata();
        FlowInstanceStep currentStep = metadata.getCurrentStep();
        Long instanceId = metadata.getInstanceId();

        try {
            updateFlowObjectProperties(getVertex(currentStep.getFlowObjectInstanceId()),
                    instance, STATUS_COMPLETED, false);

            // No parents - mark the instance vertex as complete.
            if (metadata.getParentFlowObjectInstanceId() == null) {
                updateFlowObjectProperties(getVertex(instanceId), null, STATUS_COMPLETED, false);
            }

            commit();
            return true;
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to mark flow instance as complete: " + instanceId, ex);
            return false;
        }
    }

    public Handler<Message<Object>> saveInstanceHandler() {
        return message -> {
            FlowInstanceWrapper wrapper = (FlowInstanceWrapper) message.body();
            message.reply(saveInstance(wrapper.getInstance(), wrapper.getStatus(), wrapper.saveMetadata()));
        };
    }

    /**
     * Save the instance data and update the status of the flow object.
     *
     * @param instance the instance to be saved.
     * @param status the updated status of the flow object.
     * @param saveMetadata flag that indicates if the instance metadata has to be stored in the graph.
     * @return truthy value indicating the success or failure of save operation on the instance.
     */
    public boolean saveInstance(FlowInstance instance, String status, boolean saveMetadata) {
        FlowInstanceMetadata metadata = instance.getMetadata();

        try {
            updateFlowObjectProperties(getVertex(metadata.getCurrentStep().getFlowObjectInstanceId()),
                    instance, status, saveMetadata);
            commit();
            return true;
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to save flow instance: " + metadata, ex);
            return false;
        }
    }

    public Handler<Message<Object>> failInstanceHandler() {
        return message -> {
            FlowInstance instance = (FlowInstance) message.body();
            message.reply(failInstance(instance));
        };
    }

    /**
     * Updated the status of instance to "Failed" and save the instance.
     * @param instance the instance to be saved.
     * @return truthy value indicating whether the update was successful or not.
     */
    public boolean failInstance(FlowInstance instance) {
        FlowInstanceMetadata metadata = instance.getMetadata();
        FlowInstanceStep currentStep = metadata.getCurrentStep();

        try {
            Vertex flowObjectVertex = getVertex(currentStep.getFlowObjectInstanceId());
            updateFlowObjectProperties(flowObjectVertex, instance, STATUS_FAILED, true);

            // TODO: Get the cause of failure from the caller.
            flowObjectVertex.property(Schema.P_CAUSE, "Unknown");

            // TODO: Handle parent instance.
            commit();

            if (logger.isInfoEnabled()) {
                logger.info("Flow instance failed: " + metadata);
            }

            return true;
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to mark flow instance node as failed: " + metadata.getInstanceId(), ex);
            return false;
        }
    }

    private void addFlowObjectProperties(Vertex vertex, String flowObjectId, String subFlowId) {
        vertex.property(Schema.V_P_FLOW_OBJECT_ID, flowObjectId);
        vertex.property(Schema.P_STATUS, STATUS_IN_PROGRESS);

        if (subFlowId != null) {
            vertex.property(Schema.P_SUB_FLOW_ID, subFlowId);
        }
    }

    private void updateFlowObjectProperties(Vertex vertex, FlowInstance instance, String status, boolean saveMetadata) {
        vertex.property(Schema.P_STATUS, status);

        if (instance == null) {
            return;
        }

        if (saveMetadata) {
            vertex.property(Schema.V_P_META_DATA, instance.getMetadata().encode());
            vertex.property(Schema.V_P_INSTANCE_ID, instance.getMetadata().getInstanceId());
        }

        JsonObject data = instance.getData();
        if (data != null && data.getMap().size() > 0) {
            vertex.property(Schema.V_P_DATA, data.encode());
        }

        JsonObject inputData = instance.getInputData();
        if (inputData != null && inputData.getMap().size() > 0) {
            vertex.property(Schema.V_P_INPUT_DATA, inputData.encode());
        }

        JsonObject outputData = instance.getOutputData();
        if (outputData != null && outputData.getMap().size() > 0) {
            vertex.property(Schema.V_P_OUTPUT_DATA, outputData.encode());
        }
    }
}

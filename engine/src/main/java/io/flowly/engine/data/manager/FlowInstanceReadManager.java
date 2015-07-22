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

import io.flowly.engine.JsonKeys;
import io.flowly.core.data.FlowInstance;
import io.flowly.core.data.FlowInstanceMetadata;
import io.flowly.core.codecs.FlowInstanceCodec;
import io.flowly.engine.verticles.Kernel;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines get operations related to a flow instance.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class FlowInstanceReadManager extends BaseManager {
    private static final Logger logger = LoggerFactory.getLogger(FlowInstanceReadManager.class);
    private static final int ADDITIONAL_RECORDS = 5;

    private static final List<String> RECEIVED_STATUSES = new ArrayList<>();
    static {
        RECEIVED_STATUSES.add(STATUS_NEW);
        RECEIVED_STATUSES.add(STATUS_IN_PROGRESS);
    }

    public FlowInstanceReadManager(Graph graph) {
        super(graph);
    }

    public Handler<Message<Object>> getInstanceAtFlowObjectHandler() {
        return message -> {
            Long flowObjectInstanceId = (Long) message.body();
            DeliveryOptions options = Kernel.DELIVERY_OPTIONS.get(FlowInstanceCodec.NAME);
            message.reply(getInstanceAtFlowObject(flowObjectInstanceId), options);
        };
    }

    public FlowInstance getInstanceAtFlowObject(Long flowObjectInstanceId) {
        FlowInstance instance = new FlowInstance();

        try {
            Vertex atVertex = getVertex(flowObjectInstanceId);

            Object metadata = getPropertyValue(atVertex, Schema.V_P_META_DATA);
            if (metadata != null) {
                instance.setMetadata(new FlowInstanceMetadata((String) metadata));
            }

            Object data = getPropertyValue(atVertex, Schema.V_P_DATA);
            if (data != null) {
                instance.setData(new JsonObject((String) data));
            }

            commit();
            logger.info("Retrieved flow instance: " + instance.getMetadata());
            return instance;
        }
        catch (Exception ex) {
            rollback();
            instance = null;
            logger.error("Unable to retrieve flow instance: " + flowObjectInstanceId, ex);
        }

        return instance;
    }

    public Handler<Message<Object>> getInboxHandler() {
        return message -> {
            JsonObject args = (JsonObject) message.body();
            // TODO: Configure default values.
            message.reply(getInbox(args.getString(JsonKeys.SUBJECT_ID),
                    args.getInteger(JsonKeys.PAGE_NUMBER, 1), args.getInteger(JsonKeys.PAGE_SIZE, 50)));
        };
    }

    public JsonObject getInbox(String subjectId, int pageNumber, int pageSize) {
        int low = (pageNumber - 1) * pageSize;
        int high = low + pageSize;
        List<JsonObject> tasks;

        try {
            tasks = graph.traversal().V().has(Schema.V_P_SUBJECT_ID, subjectId).
                    outE(Schema.E_IS_ASSIGNED).has(Schema.E_P_RECEIVED_STATUS, P.within(RECEIVED_STATUSES)).
                    order().by(Schema.E_P_ASSIGNED_ON, Order.decr).range(low, high + ADDITIONAL_RECORDS).as("e_asg").
                    inV().as("v_svc").
                    choose(__.values(Schema.P_STATUS).is(RECEIVED_STATUSES.get(0)),
                            __.select("e_asg", "v_svc"),
                            __.repeat(__.outE().inV()).
                                    until(it -> !it.get().value(Schema.P_STATUS).equals(STATUS_COMPLETED)).as("v_view").
                                    select("e_asg", "v_svc", "v_view")).map(m -> {
                JsonObject task = new JsonObject();
                Vertex v = (Vertex) m.get().get("v_svc");
                Edge e = (Edge) m.get().get("e_asg");

                task.put(JsonKeys.TASK_ID, v.id());
                task.put(JsonKeys.INSTANCE_ID, v.property(Schema.V_P_INSTANCE_ID).value());
                task.put(Schema.E_P_RECEIVED_STATUS, e.property(Schema.E_P_RECEIVED_STATUS).value());
                task.put(Schema.P_SUB_FLOW_ID, v.property(Schema.P_SUB_FLOW_ID).value());

                // TODO: Get due dates and subject.

                if (m.get().containsKey("v_view")) {
                    Vertex view = (Vertex) m.get().get("v_view");

                    task.put(JsonKeys.VIEW_ID, view.id());
                    task.put(Schema.P_STATUS, view.value(Schema.P_STATUS).toString());
                    task.put(JsonKeys.VIEW_ROUTE, view.value(Schema.P_SUB_FLOW_ID).toString());
                }
                else {
                    task.put(Schema.P_STATUS, v.value(Schema.P_STATUS).toString());
                }

                return task;
            }).toList();

            commit();
        }
        catch (Exception ex) {
            rollback();
            tasks = new ArrayList<>();
            logger.error("Unable to get the inbox for user: " + subjectId, ex);
        }

        return createInbox(tasks, low);
    }

    private JsonObject createInbox(List<JsonObject> tasks, int low) {
        JsonObject inbox = new JsonObject();

        inbox.put(JsonKeys.COUNT, low + tasks.size());
        inbox.put(JsonKeys.TASKS, tasks);

        return inbox;
    }
}

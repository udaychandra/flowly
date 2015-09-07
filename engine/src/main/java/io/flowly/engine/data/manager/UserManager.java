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
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Defines operations on users and teams in flowly.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class UserManager extends BaseManager {
    private static final Logger logger = LoggerFactory.getLogger(UserManager.class);

    public UserManager(Graph graph) {
        super(graph);
    }

    public Handler<Message<Object>> assignTaskHandler() {
        return message -> {
            JsonObject args = (JsonObject) message.body();
            message.reply(assignTask(args.getString(JsonKeys.SUBJECT_ID), args.getLong(JsonKeys.TASK_ID)));
        };
    }

    public boolean assignTask(String subjectId, Long taskId) {
        boolean assigned = false;
        try {
            Vertex userVertex = getSubjectVertex(subjectId);
            Vertex taskVertex = getVertex(taskId);

            if (userVertex != null && taskVertex != null) {
                Edge isAssigned = userVertex.addEdge(Schema.E_IS_ASSIGNED, taskVertex);
                isAssigned.property(Schema.E_P_RECEIVED_STATUS, STATUS_NEW);
                isAssigned.property(Schema.E_P_ASSIGNED_ON, Calendar.getInstance().getTime());

                assigned = true;

                logger.info("Task (" + taskId + ") assigned to subject: " + subjectId);
            }

            commit();
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to assign task: " + taskId + " to subject: " + subjectId, ex);
            assigned = false;
        }

        return assigned;
    }

    public Handler<Message<Object>> updateTaskHandler() {
        return message -> {
            JsonObject args = (JsonObject) message.body();
            message.reply(updateTask(null, args.getLong(JsonKeys.TASK_ID), args.getString(JsonKeys.STATUS)));
        };
    }

    public boolean updateTask(String subjectId, Long taskId, String status) {
        boolean updated = false;

        try {
            Vertex taskVertex = getVertex(taskId);
            Iterator<Edge> edgeIterator = taskVertex.edges(Direction.IN, Schema.E_IS_ASSIGNED);
            if (edgeIterator.hasNext()) {
                Edge isAssigned = edgeIterator.next();
                isAssigned.property(Schema.E_P_RECEIVED_STATUS, status);

                updated = true;
            }
            else {
                logger.info("Unable to find the subject assignment edge.");
            }

            commit();
        }
        catch (Exception ex) {
            rollback();
            logger.error("Unable to update assignment: " + taskId + ", status: " + status, ex);
            updated = false;
        }

        return updated;
    }

    protected Vertex getSubjectVertex(String subjectId) {
        Vertex subjectVertex = null;

        List<Vertex> traversal = graph.traversal().V().has(Schema.V_P_SUBJECT_ID, subjectId).toList();
        if (traversal.size() == 1) {
            subjectVertex = traversal.get(0);
        }

        return subjectVertex;
    }
}

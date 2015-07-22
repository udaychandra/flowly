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

package io.flowly.engine.data;

import io.flowly.engine.data.manager.Schema;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author <a>Uday Tatiraju</a>
 */
public class MockFlowGraphGenerator {
    public static void populateGraph(Graph graph, int usersCount, int simpleFlowsCount, int complexFlowsCount) {
        String[] users = {
                "uday", "mady", "deshetti", "punam", "pradeep",
                "lucky", "jungle", "vani", "kiran", "swarun", "mj",
                "santhosh", "priyanka", "sravan", "keerthana"
        };

        List<Vertex> userVertices = new ArrayList<>();

        for (int i=0; i<usersCount; i++) {
            Vertex userVertex = graph.addVertex(Schema.V_SUBJECT);
            userVertex.property(Schema.V_P_SUBJECT_ID, users[i]);

            userVertices.add(userVertex);
        }

        graph.tx().commit();

        boolean[] trueFalse = {true, false};

        Random random1 = new Random();
        Random random2 = new Random();
        Random random3 = new Random();

        // Create n number of simple process flows with interactive service.
        for (int i=0; i<simpleFlowsCount; i++) {
            boolean isNew = trueFalse[random1.nextInt(2)];
            boolean isComplete = trueFalse[random2.nextInt(2)];

            Vertex serviceVertex = simpleFlow(graph, isNew, isComplete);

            // Select a user randomly.
            Vertex userVertex = userVertices.get(random3.nextInt(usersCount));
            Edge isAssigned = userVertex.addEdge(Schema.E_IS_ASSIGNED, serviceVertex);
            isAssigned.property(Schema.E_P_RECEIVED_STATUS, isNew ? "New" : (isComplete ? "Complete" : "In progress"));

            Calendar now = Calendar.getInstance();
            now.add(Calendar.SECOND, 1);
            isAssigned.property(Schema.E_P_ASSIGNED_ON, now.getTime());
        }

        graph.tx().commit();

        // Create m number of complex process flows with interactive service.
        for (int i=0; i<complexFlowsCount; i++) {
            boolean isComplete = trueFalse[random2.nextInt(2)];

            Vertex serviceVertex = complexFlowWithSubFlow(graph, isComplete);

            // Select a user randomly.
            Vertex userVertex = userVertices.get(0);
            Edge isAssigned = userVertex.addEdge(Schema.E_IS_ASSIGNED, serviceVertex);
            isAssigned.property(Schema.E_P_RECEIVED_STATUS, isComplete ? "Complete" : "In progress");

            Calendar now = Calendar.getInstance();
            now.add(Calendar.SECOND, 1);
            isAssigned.property(Schema.E_P_ASSIGNED_ON, now.getTime());
        }

        graph.tx().commit();
    }

    private static Vertex simpleFlow(Graph graph, boolean isNew, boolean isComplete) {
        Vertex processVertex = graph.addVertex(Schema.V_PROCESS_INSTANCE);
        processVertex.property(Schema.V_P_PROCESS_FLOW_ID, UUID.randomUUID().toString());
        processVertex.property(Schema.P_STATUS, isComplete ? "Complete" : "In progress");
        Long instanceId = (Long) processVertex.id();

        // this flow has 2 flow objects before it hits the interactive service flow object.
        Vertex flowObject1 = createCompletedFlowObject(graph);
        processVertex.addEdge(Schema.E_FLOW_TO, flowObject1);

        Vertex flowObject2 = createCompletedFlowObject(graph);
        flowObject1.addEdge(Schema.E_FLOW_TO, flowObject2);

        // Add interactive service flow object to the flow.
        Vertex serviceVertex = createInteractiveService(graph, instanceId, isNew, isComplete);
        flowObject2.addEdge(Schema.E_FLOW_TO, serviceVertex);

        return serviceVertex;
    }

    private static Vertex complexFlowWithSubFlow(Graph graph, boolean isComplete) {
        Vertex processVertex = graph.addVertex(Schema.V_PROCESS_INSTANCE);
        processVertex.property(Schema.V_P_PROCESS_FLOW_ID, UUID.randomUUID().toString());
        processVertex.property(Schema.P_STATUS, isComplete ? "Complete" : "In progress");
        Long instanceId = (Long) processVertex.id();

        // this flow has 3 flow objects before it hits the interactive service flow object.
        Vertex flowObject1 = createCompletedFlowObject(graph);
        processVertex.addEdge(Schema.E_FLOW_TO, flowObject1);

        Vertex flowObject2 = createCompletedFlowObject(graph);
        flowObject1.addEdge(Schema.E_FLOW_TO, flowObject2);

        Vertex flowObject3 = createCompletedFlowObject(graph);
        flowObject2.addEdge(Schema.E_FLOW_TO, flowObject3);

        // Add interactive service flow object to the flow.
        Vertex serviceVertex = createInteractiveService(graph, instanceId, false, isComplete);
        flowObject3.addEdge(Schema.E_FLOW_TO, serviceVertex);

        // start sub flow
        Vertex subFlowObject1 = createCompletedFlowObject(graph);
        serviceVertex.addEdge(Schema.E_FLOW_INTO, subFlowObject1);

        Vertex subFlowObject2 = createCompletedFlowObject(graph);
        subFlowObject1.addEdge(Schema.E_FLOW_TO, subFlowObject2);

        // Add view
        Vertex view = createView(graph, isComplete);
        subFlowObject2.addEdge(Schema.E_FLOW_TO, view);

        return serviceVertex;
    }

    private static Vertex createCompletedFlowObject(Graph graph) {
        Vertex flowObject = graph.addVertex(Schema.V_FLOW_OBJECT_INSTANCE);
        flowObject.property(Schema.V_P_FLOW_OBJECT_ID, UUID.randomUUID().toString());
        flowObject.property(Schema.P_STATUS, "Completed");

        return flowObject;
    }

    private static Vertex createInteractiveService(Graph graph, Long instanceId, boolean isNew, boolean isComplete) {
        Vertex serviceVertex = graph.addVertex(Schema.V_FLOW_OBJECT_INSTANCE);
        serviceVertex.property(Schema.V_P_INSTANCE_ID, instanceId);
        serviceVertex.property(Schema.V_P_FLOW_OBJECT_ID, UUID.randomUUID().toString());
        serviceVertex.property(Schema.P_SUB_FLOW_ID, "9999-9999-9999-9999");

        serviceVertex.property(Schema.P_STATUS, isNew ? "New" : (isComplete ? "Complete" : "In progress"));

        return serviceVertex;
    }

    private static Vertex createView(Graph graph, boolean isComplete) {
        Vertex view = graph.addVertex(Schema.V_FLOW_OBJECT_INSTANCE);
        view.property(Schema.V_P_FLOW_OBJECT_ID, UUID.randomUUID().toString());
        view.property(Schema.P_SUB_FLOW_ID, "io.test.app:helloView");

        view.property(Schema.P_STATUS, isComplete ? "Complete" : "User interacting");

        return view;
    }
}

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

import com.thinkaurelius.titan.core.EdgeLabel;
import com.thinkaurelius.titan.core.Multiplicity;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import io.flowly.engine.JsonKeys;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Date;

/**
 * Create a Titan graph by configuring the stotage and indexing options.
 * Prepares the graph schema when the graph is created for the first time.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class FlowlyGraph {
    private static final Logger logger = LoggerFactory.getLogger(FlowlyGraph.class);

    private TitanGraph graph;

    // TODO: Use Cassandra.
    public FlowlyGraph(JsonObject config) {
        TitanFactory.Builder titanConfig = TitanFactory.build();
        titanConfig.set(JsonKeys.DB_STORAGE_BACKEND, "berkeleyje");
        titanConfig.set(JsonKeys.DB_STORAGE_DIRECTORY, config.getString(JsonKeys.DB_STORAGE_DIRECTORY));

        graph = titanConfig.open();
        configureSchema(graph);
    }

    public Graph getInstance() {
        return graph;
    }

    // TODO: Create an upgradable schema by defining onCreate and onUpdate methods.
    // TODO: Read JSON files to create schema.
    private void configureSchema(TitanGraph graph) {
        TitanManagement management = graph.openManagement();
        boolean schemaCreated = false;

        if (management.containsVertexLabel(Schema.V_PROCESS_INSTANCE)) {
            logger.info("Flowly graph schema created in a previous run.");
        }
        else {
            logger.info("Creating flowly graph schema...");

            // Subject (user, group, team) vertex.
            management.makeVertexLabel(Schema.V_SUBJECT).make();
            PropertyKey subjectId = management.makePropertyKey(Schema.V_P_SUBJECT_ID).
                    dataType(String.class).make();
            // Graph-level composite index (only supports equality operator).
            management.buildIndex("subjectIdx", Vertex.class).addKey(subjectId).unique().buildCompositeIndex();

            // IsAssigned edge.
            EdgeLabel isAssigned = management.makeEdgeLabel(Schema.E_IS_ASSIGNED).
                    multiplicity(Multiplicity.ONE2MANY).make();
            PropertyKey receivedStatus = management.makePropertyKey(Schema.E_P_RECEIVED_STATUS).
                    dataType(String.class).make();
            PropertyKey assignedOn = management.makePropertyKey(Schema.E_P_ASSIGNED_ON).
                    dataType(Date.class).make();
            management.makePropertyKey(Schema.E_P_DUE_DATE).dataType(Date.class).make();
            management.makePropertyKey(Schema.E_P_AT_RISK_DATE).dataType(Date.class).make();
            // Build vertex-centric index on the edge.
            management.buildEdgeIndex(isAssigned, "isAssignedIdx", Direction.OUT, Order.decr,
                    assignedOn, receivedStatus);

            // Process instance vertex.
            management.makeVertexLabel(Schema.V_PROCESS_INSTANCE).make();
            management.makePropertyKey(Schema.V_P_PROCESS_FLOW_ID).dataType(String.class).make();

            // Flow object instance vertex.
            management.makeVertexLabel(Schema.V_FLOW_OBJECT_INSTANCE).make();
            management.makePropertyKey(Schema.V_P_INSTANCE_ID).dataType(Long.class).make();
            management.makePropertyKey(Schema.V_P_FLOW_OBJECT_ID).dataType(String.class).make();
            management.makePropertyKey(Schema.V_P_META_DATA).dataType(String.class).make();
            management.makePropertyKey(Schema.V_P_DATA).dataType(String.class).make();
            management.makePropertyKey(Schema.V_P_INPUT_DATA).dataType(String.class).make();
            management.makePropertyKey(Schema.V_P_OUTPUT_DATA).dataType(String.class).make();

            // Flow object instance edges.
            management.makeEdgeLabel(Schema.E_FLOW_TO).multiplicity(Multiplicity.ONE2MANY).make();
            management.makeEdgeLabel(Schema.E_FLOW_INTO).multiplicity(Multiplicity.ONE2ONE).make();
            management.makeEdgeLabel(Schema.E_FLOW_OUT).multiplicity(Multiplicity.ONE2ONE).make();

            // Flow vertex
            management.makeVertexLabel(Schema.V_FLOW_METADATA).make();
            management.makePropertyKey(Schema.V_P_FLOW_TYPE).dataType(String.class).make();
            management.makePropertyKey(Schema.V_P_APP_ID).dataType(String.class).make();
            // Build graph-level composite index.
            PropertyKey flowId = management.makePropertyKey(Schema.V_P_FLOW_ID).
                    dataType(String.class).make();
            management.buildIndex("flowIdx", Vertex.class).addKey(flowId).unique().buildCompositeIndex();

            // Flow route vertex.
            management.makeVertexLabel(Schema.V_FLOW_ROUTE).make();
            // TODO: Add index on current flow object id?
            management.makePropertyKey(Schema.V_P_CURRENT_FLOW_OBJECT_ID).dataType(String.class).make();
            management.makePropertyKey(Schema.V_P_CONNECTING_OBJECT_ID).dataType(String.class).make();
            management.makePropertyKey(Schema.V_P_NEXT_FLOW_OBJECT_ID).dataType(String.class).make();
            // Build graph-level composite index.
            PropertyKey routeFlowId = management.makePropertyKey(Schema.V_P_ROUTE_FLOW_ID).
                    dataType(String.class).make();
            management.buildIndex("routeFlowIdx", Vertex.class).addKey(routeFlowId).buildCompositeIndex();

            // General vertex properties.
            management.makePropertyKey(Schema.P_STATUS).dataType(String.class).make();
            management.makePropertyKey(Schema.P_SUBJECT).dataType(String.class).make();
            management.makePropertyKey(Schema.P_SUB_FLOW_ID).dataType(String.class).make();

            schemaCreated = true;
            logger.info("Flowly graph schema created.");
        }

        management.commit();

        // Create admin user vertex.
        if (schemaCreated) {
            Vertex adminVertex = graph.addVertex(Schema.V_SUBJECT);
            adminVertex.property(Schema.V_P_SUBJECT_ID, JsonKeys.ADMIN_USER_ID);
            graph.tx().commit();

            logger.info("Admin vertex created.");
        }
    }
}

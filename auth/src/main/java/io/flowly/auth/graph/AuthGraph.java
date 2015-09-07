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

package io.flowly.auth.graph;

import com.thinkaurelius.titan.core.Multiplicity;
import com.thinkaurelius.titan.core.PropertyKey;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import io.flowly.core.ObjectKeys;
import io.flowly.core.security.PasswordHash;
import io.flowly.core.security.Vault;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Objects;

/**
 * Create a Titan graph by configuring the storage and indexing options.
 * Prepares the graph schema when the graph is created for the first time.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class AuthGraph {
    private static final Logger logger = LoggerFactory.getLogger(AuthGraph.class);

    private TitanGraph graph;
    private Vault vault;

    public AuthGraph(JsonObject config, FileSystem fileSystem) throws Exception {
        prepareVault(config, fileSystem);
        prepareGraph(config);
    }

    public Graph getInstance() {
        return graph;
    }

    private void prepareVault(JsonObject config, FileSystem fileSystem) throws Exception {
        String path = Objects.requireNonNull(config.getString(ObjectKeys.VAULT_PATH),
                "Vault path must be specified.");
        String key = Objects.requireNonNull(config.getString(ObjectKeys.VAULT_KEY), "Vault key has to be specified");
        vault = new Vault(key.toCharArray(), path, fileSystem);
    }

    private void prepareGraph(JsonObject config) throws Exception {
        TitanFactory.Builder titanConfig = TitanFactory.build();

        // TODO: Use Cassandra.
        titanConfig.set(ObjectKeys.DB_STORAGE_BACKEND, "berkeleyje");
        titanConfig.set(ObjectKeys.DB_STORAGE_DIRECTORY, config.getString(ObjectKeys.DB_STORAGE_DIRECTORY));

        graph = titanConfig.open();
        configureSchema(graph);
    }

    // TODO: Create an upgradable schema by defining onCreate and onUpdate methods.
    // TODO: Read JSON files to create schema.
    private void configureSchema(TitanGraph graph) throws Exception {
        TitanManagement management = graph.openManagement();
        boolean schemaCreated = false;

        try {

            if (management.containsVertexLabel(Schema.V_USER)) {
                logger.info("Flowly auth graph schema created in a previous run.");
            }
            else {
                logger.info("Creating flowly auth graph schema...");

                // User vertex.
                management.makeVertexLabel(Schema.V_USER).make();
                PropertyKey userId = management.makePropertyKey(Schema.V_P_USER_ID).
                        dataType(String.class).make();
                management.makePropertyKey(Schema.V_P_FIRST_NAME).dataType(String.class).make();
                management.makePropertyKey(Schema.V_P_LAST_NAME).dataType(String.class).make();
                management.makePropertyKey(Schema.V_P_MIDDLE_NAME).dataType(String.class).make();
                management.makePropertyKey(Schema.V_P_IS_INTERNAL).dataType(Boolean.class).make();
                management.makePropertyKey(Schema.V_P_PASSWORD).dataType(String.class).make();
                // Graph-level composite index (only supports equality operator).
                management.buildIndex(Schema.V_IDX_USER_ID, Vertex.class).
                        addKey(userId).unique().buildCompositeIndex();

                // Group vertex
                management.makeVertexLabel(Schema.V_GROUP).make();
                PropertyKey groupId = management.makePropertyKey(Schema.V_P_GROUP_ID).
                        dataType(String.class).make();
                management.buildIndex(Schema.V_IDX_GROUP_ID, Vertex.class).
                        addKey(groupId).unique().buildCompositeIndex();

                // Common properties.
                management.makePropertyKey(Schema.V_P_NAME).dataType(String.class).make();
                management.makePropertyKey(Schema.V_P_DESCRIPTION).dataType(String.class).make();

                // Resource vertex
                management.makeVertexLabel(Schema.V_RESOURCE).make();
                PropertyKey resourceId = management.makePropertyKey(Schema.V_P_RESOURCE_ID).
                        dataType(String.class).make();
                // Graph-level composite index (only supports equality operator).
                management.buildIndex(Schema.V_IDX_RESOURCE_ID, Vertex.class).
                        addKey(resourceId).unique().buildCompositeIndex();

                // Member edge.
                management.makeEdgeLabel(Schema.E_MEMBER).multiplicity(Multiplicity.MULTI).make();

                // MemberOf edge.
                management.makeEdgeLabel(Schema.E_MEMBER_OF).multiplicity(Multiplicity.MULTI).make();

                // Permission edge.
                management.makeEdgeLabel(Schema.E_HAS_PERMISSION).multiplicity(Multiplicity.MULTI).make();
                management.makePropertyKey(Schema.E_P_RWX).dataType(Integer.class).make();

                schemaCreated = true;
                logger.info("Flowly auth graph schema created.");
            }

            management.commit();
        }
        catch (Exception ex) {
            management.rollback();
            logger.error("Unable to build Auth graph schema.", ex);
            throw ex;
        }

        // Create admin user vertex.
        if (schemaCreated) {
            createAdmin();
        }
    }

    private void createAdmin() throws Exception {
        try {
            Vertex adminVertex = graph.addVertex(T.label, Schema.V_USER,
                    Schema.V_P_USER_ID, ObjectKeys.ADMIN_USER_ID,
                    Schema.V_P_PASSWORD, PasswordHash.createHash(vault.getData(ObjectKeys.ADMIN_KEY)));
            createAdministratorsGroup(adminVertex);
            graph.tx().commit();

            logger.info("Administrators group and admin user created.");
        }
        catch (Exception ex) {
            graph.tx().rollback();
            logger.error("Unable to create Administrators group and admin user.", ex);
            throw ex;
        }
    }

    private void createAdministratorsGroup(Vertex admin) throws Exception {
        Vertex administrators = graph.addVertex(T.label, Schema.V_GROUP,
                Schema.V_P_GROUP_ID, ObjectKeys.ADMIN_GROUP_ID);

        administrators.addEdge(Schema.E_MEMBER, admin);
        admin.addEdge(Schema.E_MEMBER_OF, administrators);
    }
}

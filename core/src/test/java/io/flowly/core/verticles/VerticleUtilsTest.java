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

package io.flowly.core.verticles;

import io.flowly.core.BaseTestWithVertx;
import io.flowly.core.ObjectKeys;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

/**
 * @author <a>Uday Tatiraju</a>
 */
@RunWith(VertxUnitRunner.class)
public class VerticleUtilsTest extends BaseTestWithVertx {
    private Stack<VerticleDeployment> deployments;
    private Set<JsonObject> deployedVerticles;

    @Before
    public void setUp() {
        super.setUp();
        deployments = new Stack<>();
        deployedVerticles = new HashSet<>();

        VerticleDeployment deployment1 = new VerticleDeployment("verticle_one",
                BlankVerticle.class.getName(), new DeploymentOptions());
        deployments.push(deployment1);

        VerticleDeployment deployment2 = new VerticleDeployment("verticle_two",
                BlankVerticle.class.getName(), new DeploymentOptions());
        deployments.push(deployment2);
    }

    @Test
    public void deployVerticlesTest(TestContext context) {
        Async async = context.async();

        VerticleUtils.deployVerticles(deployments, deployedVerticles, vertx, handler -> {
            context.assertTrue(handler.succeeded(), "Verticle deployments should succeed.");
            context.assertEquals(2, deployedVerticles.size(), "Deployed verticles count not as expected.");
            async.complete();
        });
    }

    @Test
    public void undeployVerticlesTest(TestContext context) {
        Async async = context.async();
        deployVerticlesTest(context);

        VerticleUtils.undeployVerticles(deployedVerticles, vertx, handler -> {
            context.assertTrue(handler.succeeded(), "Verticle should be undeployed.");
            async.complete();
        });
    }

    @Test
    public void registerHandlersTest(TestContext context) {
        Async async1 = context.async();
        Async async2 = context.async();
        Async async3 = context.async();
        String address1 = "address1";
        String address2 = "address2";

        Queue<ConsumerRegistration> registrations = new LinkedList<>();
        registrations.add(new ConsumerRegistration(address1, handler1 -> {
            context.assertEquals("Hello", handler1.body(), "Message recieved is not as expected.");
            async1.complete();
        }));
        registrations.add(new ConsumerRegistration(address2, handler2 -> {
            context.assertEquals("Flowly", handler2.body(), "Message recieved is not as expected.");
            async2.complete();
        }));

        VerticleUtils.registerHandlers(vertx.eventBus(), LoggerFactory.getLogger(VerticleUtilsTest.class),
                registrations, handler -> {
                    context.assertTrue(handler.succeeded(), "Message handler registrations should not fail.");
                    async3.complete();

                    vertx.eventBus().send(address1, "Hello");
                    vertx.eventBus().send(address2, "Flowly");
        });
    }
}

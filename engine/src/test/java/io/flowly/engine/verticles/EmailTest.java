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

package io.flowly.engine.verticles;

import io.flowly.engine.EngineAddresses;
import io.flowly.engine.JsonKeys;
import io.flowly.engine.verticles.services.Email;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a>Uday Tatiraju</a>
 */
@RunWith(VertxUnitRunner.class)
public class EmailTest extends BaseVerticleTest {
    @Before
    public void before(TestContext context) {
        super.before();
        vertx.deployVerticle(new Email(), context.asyncAssertSuccess());
    }

    @Test
    public void testSendEmail(TestContext context) {
        List<String> emailTo = new ArrayList<>();
        emailTo.add("unit.test@test.com");

        JsonObject mail = new JsonObject();
        mail.put(JsonKeys.EMAIL_SUBJECT, "Email service test");
        mail.put(JsonKeys.EMAIL_BODY, "This is a test <b>email</b> with style.");
        mail.put(JsonKeys.EMAIL_TO, new JsonArray(emailTo));

        vertx.eventBus().send(EngineAddresses.SEND_EMAIL, mail, context.asyncAssertSuccess());
    }
}
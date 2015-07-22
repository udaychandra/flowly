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

package io.flowly.engine.verticles.services;

import io.flowly.engine.EngineAddresses;
import io.flowly.core.Failure;
import io.flowly.engine.JsonKeys;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mail.LoginOption;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;

/**
 * Defines an email service verticle that is available to all flowly apps.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class Email extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(Email.class);

    private MailClient mailClient;

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        // TODO: Replace with actual per app configuration.
        // TODO: Encrypt config details and add config per app?.
        MailConfig mailConfig = new MailConfig();
        mailConfig.setHostname("mailtrap.io");
        mailConfig.setPort(465);
        mailConfig.setUsername("34420b90fe7a0af53");
        mailConfig.setPassword("3ea5c28a13aa60");
        mailConfig.setLogin(LoginOption.REQUIRED);

        mailClient = MailClient.createNonShared(vertx, mailConfig);
        registerEmailHandler();

        startFuture.complete();
    }

    // TODO: design strategy to deploy system micro services like email, database and rest.
    private void registerEmailHandler() {
        vertx.eventBus().consumer(EngineAddresses.SEND_EMAIL, request -> {
            JsonObject mail = (JsonObject) request.body();
            MailMessage email = new MailMessage();

            try {
                // TODO: Should "from" variable be configured per app or per call?
                email.setFrom("test@flowly.io");

                // TODO: Test multiple send to issue.
                // If emailTo is a string, create an array.
                Object emailTo = mail.getValue(JsonKeys.EMAIL_TO);

                if (emailTo instanceof String) {
                    email.setTo((String) emailTo);
                }
                else {
                    email.setTo(mail.getJsonArray(JsonKeys.EMAIL_TO).getList());
                }

                email.setSubject(mail.getString(JsonKeys.EMAIL_SUBJECT));
                email.setHtml(mail.getString(JsonKeys.EMAIL_BODY));

                mailClient.sendMail(email, result -> {
                    if (result.succeeded()) {
                        if (logger.isInfoEnabled()) {
                            logger.info("Email: " + email.getSubject() + ", sent to: " + email.getTo());
                        }

                        JsonObject message = new JsonObject();
                        message.put(JsonKeys.RESULT, true);
                        request.reply(message);
                    }
                    else {
                        Failure failure = new Failure(6000, "Failed to send email.", result.cause());
                        logger.error(failure.getError(), failure.getCause());
                        request.fail(failure.getCode(), failure.getMessage());
                    }
                });
            }
            catch (Exception ex) {
                Failure failure = new Failure(6001, "Unable to parse email message.", ex);
                logger.error(failure.getError(), failure.getCause());
                request.fail(failure.getCode(), failure.getMessage());
            }
        });
    }
}

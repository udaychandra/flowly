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

package io.flowly.auth.router;

import io.flowly.auth.LocalAddresses;
import io.flowly.core.ObjectKeys;
import io.flowly.core.security.Group;
import io.flowly.core.security.Permission;
import io.flowly.core.security.User;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.JWTAuthHandler;

/**
 * @author <a>Uday Tatiraju</a>
 */
public class UserRouter extends CrudRouter {
    private static final Logger logger = LoggerFactory.getLogger(UserRouter.class);

    public UserRouter(Vertx vertx, JWTAuth authProvider, Router router) {
        super(vertx, authProvider, router, "/user/*");

        prepareLoginRoute(vertx, router);

        prepareSearchRoute("/user/search", LocalAddresses.SEARCH_USER, "Unable to search for users.");
        prepareRoute(HttpMethod.POST, "/user/create", LocalAddresses.CREATE_USER, "Unable to create user.", false);
        prepareRoute(HttpMethod.POST, "/user/update", LocalAddresses.UPDATE_USER, "Unable to update user.", false);
        prepareRoute(HttpMethod.GET, "/user/get", LocalAddresses.GET_USER, "Unable to get user.", true);
    }

    private void prepareLoginRoute(Vertx vertx, Router router) {
        router.route(HttpMethod.POST, "/login").handler(requestHandler -> {
            try {
                JsonObject user = requestHandler.getBodyAsJson();

                vertx.eventBus().<JsonObject>send(LocalAddresses.AUTHENTICATE_USER, user, reply -> {
                    writeSuccessResponse(requestHandler, getAuthenticatedUser(reply.result().body()).encode(), false);
                });
            }
            catch (Exception ex) {
                logger.error("Unable to authenticate user.", ex);
                writeErrorResponse(requestHandler, "Unable to authenticate user.");
            }
        });
    }

    private JsonObject getAuthenticatedUser(JsonObject user) {
        if (!user.getBoolean(User.AUTHENTICATED)) {
            return user;
        }

        JsonObject claims = new JsonObject().put("sub", user.getString(User.USER_ID)).
                put("permissions", user.remove(Permission.EFFECTIVE_PERMISSIONS));

        JsonArray directMemberships = (JsonArray) user.remove(User.DIRECT_MEMBERSHIPS);
        if (directMemberships != null) {
            claims.put(ObjectKeys.GURU, directMemberships.stream().anyMatch(m -> {
                JsonObject group = (JsonObject) m;
                String groupId = group.getString(Group.GROUP_ID);

                return groupId != null && groupId.equals(ObjectKeys.ADMIN_GROUP_ID);
            }));
        }

        return user.put("token", authProvider.generateToken(claims, new JWTOptions()));
    }
}

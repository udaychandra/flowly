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

package io.flowly.auth;

import io.flowly.core.ObjectKeys;
import io.flowly.core.security.Permission;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.AbstractUser;
import io.vertx.ext.auth.AuthProvider;

/**
 * Minor modifications to io.vertx.ext.auth.jwt.impl.JWTUser.
 *
 * @author <a>Paulo Lopes</a>
 * @author <a>Uday Tatiraju</a>
 */
public class ExtJwtUser extends AbstractUser {
    private static final Logger logger = LoggerFactory.getLogger(ExtJwtUser.class);

    private final JsonObject jwtToken;
    private final JsonArray permissions;

    public ExtJwtUser(JsonObject jwtToken, String permissionsClaimKey) {
        this.jwtToken = jwtToken;
        this.permissions = jwtToken.getJsonArray(permissionsClaimKey, null);
    }

    @Override
    public JsonObject principal() {
        return jwtToken;
    }

    @Override
    public void setAuthProvider(AuthProvider authProvider) {
        // No operation.
    }

    @Override
    public void doIsPermitted(String permission, Handler<AsyncResult<Boolean>> handler) {
        if (principal().getBoolean(ObjectKeys.GURU)) {
            handler.handle(Future.succeededFuture(true));
            return;
        }
        else if (permissions != null) {
            Permission permissionToTest = new Permission(permission);

            for (Object jwtPermission : permissions) {
                if (permissionToTest.isSubSet((JsonObject) jwtPermission)) {
                    handler.handle(Future.succeededFuture(true));
                    return;
                }
            }
        }

        logger.debug("User has no permission [" + permission + "]");
        handler.handle(Future.succeededFuture(false));
    }
}

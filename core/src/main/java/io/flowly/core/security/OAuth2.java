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

package io.flowly.core.security;

import io.vertx.core.http.HttpClientRequest;

import java.net.URLEncoder;
import java.util.Base64;

/**
 * OAuth 2 helper methods.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class OAuth2 {
    protected static final String UTF_8 = "UTF-8";
    private static final String COLON = ":";

    /**
     * Create Base64 encoded bearer token credentials for OAuth2.
     *
     * @param key application's or user's consumer key used to create the bearer token credentials.
     * @param secret application's or user's consumer secret used to create the bearer token credentials.
     * @return OAuth2 bearer token credentials.
     */
    public static String createBearerTokenCredentials(String key, String secret) {
        try {
            key = URLEncoder.encode(key, UTF_8);
            secret = URLEncoder.encode(secret, UTF_8);
            String bearerTokenCredentials = key + COLON + secret;

            return new String(Base64.getEncoder().encode(bearerTokenCredentials.getBytes()));
        }
        catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Include an Authorization header to the request with the value of Basic and bearer token credentials.
     * Add request body for OAuth2.
     *
     * @param request the https client request to which the authorization header is to be added.
     * @param key application's or user's consumer key used to create the bearer token credentials.
     * @param secret application's or user's consumer secret used to create the bearer token credentials.
     */
    public static void prepareOAuthBearerTokenRequest(HttpClientRequest request, String key, String secret) {
        prepareOAuthBearerTokenRequest(request, createBearerTokenCredentials(key, secret));
    }

    /**
     * Include an Authorization header to the request with the value of Basic and bearer token credentials.
     * Add request body for OAuth2.
     *
     * @param request the https client request to which the authorization header is to be added.
     * @param bearerTokenCredentials Base64 encoded bearer token credentials for OAuth2.
     */
    public static void prepareOAuthBearerTokenRequest(HttpClientRequest request, String bearerTokenCredentials) {
        String body = "grant_type=client_credentials";
        request.putHeader("Authorization", "Basic " + bearerTokenCredentials);
        request.putHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        request.putHeader("Content-Length", "" + body.length());
        request.write(body);
    }
}

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

/**
 * Defines the local event bus addresses.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class LocalAddresses {
    public static final String CREATE_USER = "io.flowly.auth:user.create";
    public static final String UPDATE_USER = "io.flowly.auth:user.update";
    public static final String SEARCH_USER = "io.flowly.auth:user.search";
    public static final String GET_USER = "io.flowly.auth:user.get";
    public static final String AUTHENTICATE_USER = "io.flowly.auth:user.authenticate";

    public static final String CREATE_GROUP = "io.flowly.auth:group.create";
    public static final String UPDATE_GROUP = "io.flowly.auth:group.update";
    public static final String SEARCH_GROUP = "io.flowly.auth:group.search";
    public static final String GET_GROUP = "io.flowly.auth:group.get";

    public static final String CREATE_RESOURCE = "io.flowly.auth:resource.create";
    public static final String UPDATE_RESOURCE = "io.flowly.auth:resource.update";
    public static final String SEARCH_RESOURCE = "io.flowly.auth:resource.search";
    public static final String GET_RESOURCE = "io.flowly.auth:resource.get";
}

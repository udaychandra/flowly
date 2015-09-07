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

/**
 * Defines string constants that are used to create flowly auth graph schema.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class Schema {
    // Define "user" vertex.
    public static final String V_USER = "user";
    public static final String V_P_USER_ID = "userId";
    public static final String V_P_FIRST_NAME = "firstName";
    public static final String V_P_LAST_NAME = "lastName";
    public static final String V_P_MIDDLE_NAME = "middleName";
    public static final String V_P_IS_INTERNAL = "isInternal";
    public static final String V_P_PASSWORD = "password";
    public static final String V_IDX_USER_ID = "userIdx";

    // Define "group" vertex.
    public static final String V_GROUP = "group";
    public static final String V_P_GROUP_ID = "groupId";
    public static final String V_IDX_GROUP_ID = "groupIdx";

    // Represents a user's full name or a group's name.
    public static final String V_P_NAME = "name";
    public static final String V_P_DESCRIPTION = "description";

    // Define "resource" vertex.
    public static final String V_RESOURCE = "resource";
    public static final String V_P_RESOURCE_ID = "resourceId";
    public static final String V_IDX_RESOURCE_ID = "resourceIdx";

    // Define "member" edge.
    public static final String E_MEMBER = "member";

    // Define "memberOf" edge.
    public static final String E_MEMBER_OF = "memberOf";

    // Define "hasPermission" edge
    public static final String E_HAS_PERMISSION = "hasPermission";
    public static final String E_P_RWX = "rwx";
}

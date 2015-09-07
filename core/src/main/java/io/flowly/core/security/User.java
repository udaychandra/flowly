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

import io.flowly.core.ObjectKeys;
import io.flowly.core.data.Entity;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Defines constants that represent user object keys.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class User extends Entity {
    public static final String USER_ID = "userId";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String MIDDLE_NAME = "middleName";
    public static final String FULL_NAME = "fullName";
    public static final String IS_INTERNAL = "isInternal";
    public static final String PASSWORD = "password";
    public static final String AUTHENTICATED = "authenticated";

    public static final String DIRECT_MEMBERSHIPS = "directMemberships";
    public static final String EFFECTIVE_MEMBERSHIPS = "effectiveMemberships";
    public static final String GROUPS_TO_ADD = "groupsToAdd";
    public static final String GROUPS_TO_REMOVE = "groupsToRemove";

    public User() {
        super();
    }

    public User(JsonObject user) {
        super(user);
    }

    public void setUserId(String userId) {
        put(USER_ID, userId);
    }

    public String getUserId() {
        return getString(USER_ID);
    }

    public void setFirstName(String firstName) {
        put(FIRST_NAME, firstName);
    }

    public String getFirstName() {
        return getString(FIRST_NAME);
    }

    public void setLastName(String lastName) {
        put(LAST_NAME, lastName);
    }

    public String getLastName() {
        return getString(LAST_NAME);
    }

    public void setMiddleName(String middleName) {
        put(MIDDLE_NAME, middleName);
    }

    public String getMiddleName() {
        return getString(MIDDLE_NAME);
    }

    public void setFullName(String fullName) {
        put(FULL_NAME, fullName);
    }

    public String getFullName() {
        return getString(FULL_NAME);
    }

    public void setInternal(Boolean isInternal) {
        put(IS_INTERNAL, isInternal);
    }

    public Boolean isInternal() {
        return getBoolean(IS_INTERNAL);
    }

    public void setPassword(String password) {
        put(PASSWORD, password);
    }

    public String getPassword() {
        return getString(PASSWORD);
    }

    public void clearPassword() {
        getMap().remove(PASSWORD);
    }

    public JsonArray getGroupsToAdd() {
        return getJsonArray(User.GROUPS_TO_ADD);
    }

    public JsonArray getGroupsToRemove() {
        return getJsonArray(User.GROUPS_TO_REMOVE);
    }

    public JsonArray getPermissionsToAdd() {
        return getJsonArray(Permission.PERMISSIONS_TO_ADD);
    }

    @Override
    public JsonArray validate() {
        return validate(false);
    }

    /**
     * Ensure that the user attributes are valid.
     *
     * @param ifKeySpecified flag indicating that values should only be validated if the keys are specified.
     * @return an empty list or a list of validation errors based on whether the validation passed or not.
     */
    public JsonArray validate(boolean ifKeySpecified) {
        JsonArray errors = new JsonArray();

        if (!ifKeySpecified || containsKey(USER_ID)) {
            validateStringProperty(errors, getUserId(), "User Id cannot be blank.");
        }

        if (!ifKeySpecified || containsKey(FIRST_NAME)) {
            validateStringProperty(errors, getFirstName(), "First name cannot be blank.");
        }

        if (!ifKeySpecified || containsKey(LAST_NAME)) {
            validateStringProperty(errors, getLastName(), "Last name cannot be blank.");
        }

        if (BooleanUtils.isTrue(isInternal()) && containsKey(PASSWORD)) {
            validateStringProperty(errors, getPassword(), "Password cannot be blank.");
        }

        return errors;
    }
}

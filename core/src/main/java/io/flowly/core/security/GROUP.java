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

/**
 * Defines constants that represent group object keys.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class Group extends Entity {
    public static final String GROUP_ID = "groupId";
    public static final String INHERITED = "inherited";

    public static final String USERS_TO_ADD = "usersToAdd";
    public static final String USERS_TO_REMOVE = "usersToRemove";

    public static final String DIRECT_MEMBERS = "directMembers";
    public static final String EFFECTIVE_MEMBERS = "effectiveMembers";
    public static final String ALL_USERS = "allUsers";

    public Group() {
        super();
    }

    public Group(JsonObject group) {
        super(group);
    }

    public void setId(Object id) {
        put(ObjectKeys.ID, id);
    }

    public Long getId() {
        return getLong(ObjectKeys.ID);
    }

    @Override
    public JsonArray validate() {
        return validate(false);
    }

    /**
     * Ensure that the group attributes are valid.
     *
     * @param ifKeySpecified flag indicating that values should only be validated if the keys are specified.
     * @return an empty list or a list of validation errors based on whether the validation passed or not.
     */
    public JsonArray validate(boolean ifKeySpecified) {
        JsonArray errors = new JsonArray();

        if (!ifKeySpecified || containsKey(GROUP_ID)) {
            validateStringProperty(errors, getGroupId(), "Group Id cannot be blank.");
        }

        return errors;
    }

    public void setGroupId(String groupId) {
        put(GROUP_ID, groupId);
    }

    public String getGroupId() {
        return getString(GROUP_ID);
    }

    public void setInherited(Boolean inherited) {
        put(INHERITED, inherited);
    }

    public Boolean isInherited() {
        return getBoolean(INHERITED);
    }
}

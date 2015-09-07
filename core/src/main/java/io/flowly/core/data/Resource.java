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

package io.flowly.core.data;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.BooleanUtils;

/**
 * Represents a resource in flowly.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class Resource extends Entity {
    public static final String RESOURCE_ID = "resourceId";

    public Resource() {
        super();
    }

    public Resource(JsonObject resource) {
        super(resource);
    }

    @Override
    public JsonArray validate() {
        return validate(false);
    }

    /**
     * Ensure that the resource attributes are valid.
     *
     * @param ifKeySpecified flag indicating that values should only be validated if the keys are specified.
     * @return an empty list or a list of validation errors based on whether the validation passed or not.
     */
    public JsonArray validate(boolean ifKeySpecified) {
        JsonArray errors = new JsonArray();

        if (!ifKeySpecified || containsKey(RESOURCE_ID)) {
            validateStringProperty(errors, getResourceId(), "Resource Id cannot be blank.");
        }

        return errors;
    }

    public void setResourceId(String resourceId) {
        put(RESOURCE_ID, resourceId);
    }

    public String getResourceId() {
        return getString(RESOURCE_ID);
    }
}

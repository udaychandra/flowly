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

import io.flowly.core.ObjectKeys;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;

/**
 * Base class that defines an entity.
 *
 * @author <a>Uday Tatiraju</a>
 */
public abstract class Entity extends JsonObject {
    public static final String DESCRIPTION = "description";

    public Entity() {
        super();
    }

    public Entity(JsonObject entity) {
        super(entity.getMap());
    }

    public void setId(Object id) {
        put(ObjectKeys.ID, id);
    }

    public Long getId() {
        return getLong(ObjectKeys.ID);
    }

    public void setDescription(String description) {
        put(DESCRIPTION, description);
    }

    public String getDescription() {
        return getString(DESCRIPTION);
    }

    /**
     * Ensure that attributes are valid.
     *
     * @return an empty list or a list of validation errors based on whether the validation passed or not.
     */
    public abstract JsonArray validate();

    protected void validateStringProperty(JsonArray errors, String value, String message) {
        if (StringUtils.isBlank(value)) {
            errors.add(message);
        }
    }
}

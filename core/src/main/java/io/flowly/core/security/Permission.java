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

import io.flowly.core.data.Entity;
import io.flowly.core.data.Resource;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Defines constants that represent permission object keys.
 * "rwx" key's allowed values are 0 to 7.
 * 0    ---
 * 1    --x (execute)
 * 2    -w- (write)
 * 3    -wx
 * 4    r-- (read)
 * 5    r-x
 * 6    rw-
 * 7    rwx
 *
 * @author <a>Uday Tatiraju</a>
 */
public class Permission extends Entity {
    public static final String RESOURCE_VERTEX_ID = "resourceVertexId";
    public static final String RWX = "rwx";

    public static final String PERMISSIONS_TO_ADD = "permissionsToAdd";
    public static final String PERMISSIONS_TO_REMOVE = "permissionsToRemove";
    public static final String PERMISSIONS_TO_UPDATE = "permissionsToUpdate";

    public static final String EFFECTIVE_PERMISSIONS = "effectivePermissions";
    public static final String DIRECT_PERMISSIONS = "directPermissions";

    static final int EXECUTE = 0x0001;
    static final int WRITE = 0x0002;
    static final int READ = 0x0004;

    public Permission() {
        super();
    }

    public Permission(JsonObject permission) {
        super(permission);
    }

    @Override
    public JsonArray validate() {
        return new JsonArray();
    }

    public Permission(Object resourceVertexId, String resourceId, int rwx) {
        put(RESOURCE_VERTEX_ID, resourceVertexId);
        setRWX(rwx);

        if (resourceId != null) {
            put(Resource.RESOURCE_ID, resourceId);
        }
    }

    /**
     * A string representation of a permission object.
     *
     * @param permission <resourceId>:<[R],[W],[X]>
     *                   Ex: api:R,W
     */
    public Permission(String permission) {
        String[] tokens = permission.split(":");
        setResourceId(tokens[0]);

        Set<String> permissions = new HashSet<>(Arrays.asList(tokens[1].split(",")));
        setRWX(permissions.stream().mapToInt(Permission::getPermissionValue).sum());
    }

    public void setResourceVertexId(Long resourceVertexId) {
        put(RESOURCE_VERTEX_ID, resourceVertexId);
    }

    public Long getResourceVertexId() {
        return getLong(RESOURCE_VERTEX_ID);
    }

    public void setResourceId(String resourceId) {
        put(Resource.RESOURCE_ID, resourceId);
    }

    public String getResourceId() {
        return getString(Resource.RESOURCE_ID);
    }

    public void setRWX(int rwx) {
        if (rwx < 0 || rwx > 7) {
            throw new IllegalStateException("Permission set should be between 0 and 7");
        }

        put(RWX, rwx);
    }

    public Integer getRWX() {
        return getInteger(RWX);
    }

    public boolean canExecute() {
        int rwx = getInteger(RWX, 0);
        return (rwx & EXECUTE) == 1;
    }

    public boolean canWrite() {
        int rwx = getInteger(RWX, 0);
        return (rwx & WRITE) == 2;
    }

    public boolean canRead() {
        int rwx = getInteger(RWX, 0);
        return (rwx & READ) == 4;
    }

    public boolean isSubSet(JsonObject superSet) {
        return isSubSet(new Permission(superSet));
    }

    public boolean isSubSet(Permission superSet) {
        if (!getResourceId().equals(superSet.getResourceId())) {
            return false;
        }
        boolean isSubSet = true;
        boolean atLeastOne = false;

        if (canRead()) {
            isSubSet &= superSet.canRead();
            atLeastOne = true;
        }

        if (canWrite()) {
            isSubSet &= superSet.canWrite();
            atLeastOne = true;
        }

        if (canExecute()) {
            isSubSet &= superSet.canExecute();
            atLeastOne = true;
        }

        return atLeastOne && isSubSet;
    }

    public static JsonObject merge(JsonObject... permissions) {
        int mergedRWX = 0;

        for (JsonObject permission : permissions) {
            int rwx = permission.getInteger(RWX);
            mergedRWX = mergedRWX | rwx;
        }

        return new Permission(permissions[0].getLong(RESOURCE_VERTEX_ID),
                permissions[0].getString(Resource.RESOURCE_ID), mergedRWX);
    }

    public static int getPermissionValue(String value) {
        switch (value.toUpperCase()) {
            case "R": return READ;
            case "W": return WRITE;
            case "X": return EXECUTE;
            default: return 0;
        }
    }
}

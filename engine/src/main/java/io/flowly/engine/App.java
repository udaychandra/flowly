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

package io.flowly.engine;

import io.flowly.engine.utils.PathUtils;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.util.Objects;

/**
 * Defines a flowly application metadata.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class App {
    private static final long DEFAULT_SHARED_LOCK_TIMEOUT = 5000;

    private String id;
    private String name;
    private String description;
    private String appFolder;
    private String appRootFolder;
    private Boolean locked;
    private long sharedLockTimeout = DEFAULT_SHARED_LOCK_TIMEOUT;

    public App() {}

    public App(String id, String appRootFolder) {
        setId(id);
        setAppRootFolder(appRootFolder);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAppFolder() {
        if (appFolder == null) {
            appFolder = PathUtils.createPath(getAppRootFolder(), id.replace(PathUtils.DOT, File.separator));
        }

        return appFolder;
    }

    public void setAppFolder(String appFolder) {
        this.appFolder = Objects.requireNonNull(appFolder);
    }

    public String getAppRootFolder() {
        return Objects.requireNonNull(appRootFolder);
    }

    public void setAppRootFolder(String appRootFolder) {
        this.appRootFolder = Objects.requireNonNull(appRootFolder);
    }

    public Boolean isLocked() { return locked != null ? locked : false; }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public long getSharedLockTimeout() {
        return sharedLockTimeout;
    }

    public void setSharedLockTimeout(long sharedLockTimeout) {
        this.sharedLockTimeout = sharedLockTimeout;
    }

    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put(JsonKeys.APP_ID, getId());
        jsonObject.put(JsonKeys.APPS_DIRECTORY, getAppRootFolder());
        jsonObject.put(JsonKeys.APP_LOCKED, isLocked());

        return jsonObject;
    }

    public static App toApp(JsonObject jsonObject) {
        App app = new App(jsonObject.getString(JsonKeys.APP_ID), jsonObject.getString(JsonKeys.APPS_DIRECTORY));
        app.setLocked(jsonObject.getBoolean(JsonKeys.APP_LOCKED, false));
        return app;
    }
}

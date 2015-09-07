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

package io.flowly.core;

/**
 * Holds a bunch of common string constants that are primarily used as
 * keys in a JSON object or a Map.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class ObjectKeys {
    public static final String ID = "id";
    public static final String SUBJECT_ID = "subjectId";
    public static final String ADMIN_USER_ID = "admin";
    public static final String ADMIN_GROUP_ID = "Administrators";
    public static final String APP_ID = "appId";
    public static final String FLOW_ID = "flowId";
    public static final String TASK_ID = "taskId";
    public static final String INSTANCE_ID = "instanceId";
    public static final String VIEW_ID = "viewId";
    public static final String DEPLOYMENT_ID = "deploymentId";

    public static final String STATUS = "status";
    public static final String TASKS = "tasks";

    public static final String PAGE_NUMBER = "pageNumber";
    public static final String PAGE_SIZE = "pageSize";
    public static final String COUNT = "count";

    public static final String VIEW_ROUTE = "viewRoute";

    // Titan graph config keys
    public static final String DB_STORAGE_DIRECTORY = "storage.directory";
    public static final String DB_STORAGE_BACKEND = "storage.backend";

    public static final String VAULT_PATH = "vault.path";
    public static final String VAULT_KEY = "vault.key";
    public static final String KEY_STORE = "keyStore";

    public static final String ADMIN_KEY = "admin.key";
    public static final String GURU = "guru";

    public static final String HTTP_PORT = "http.port";
    public static final String HTTPS_PORT = "https.port";
}

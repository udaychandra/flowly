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

import io.flowly.core.ObjectKeys;

/**
 * Defines additional string constants that are used as JSON keys.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class JsonKeys extends ObjectKeys {
    // Configuration keys that are used to deploy flowly verticles.
    public static final String APPS_DIRECTORY = "apps.directory";
    public static final String APP_LOCKED = "app.locked";
    public static final String PUBLISH_FLOW_LIFE_CYCLE_EVENTS = "flow.events.lifecycle.publish";
    public static final String SCAN_APPS_ON_KERNEL_START = "kernel.start.apps.scan";

    // Titan graph config keys
    public static final String DB_STORAGE_DIRECTORY = "storage.directory";
    public static final String DB_STORAGE_BACKEND = "storage.backend";

    // Flow keys
    public static final String INSTANCE = "instance";

    // Flow event keys
    public static final String FLOW_EVENT_TYPE = "flowEventType";
    public static final String FLOW_START_EVENT = "flowStartEvent";
    public static final String FLOW_COMPLETE_EVENT = "flowCompleteEvent";
    public static final String FLOW_FAIL_EVENT = "flowFailEvent";
    public static final String FLOW_WAIT_UI_EVENT = "flowWaitUIEvent";
    public static final String FLOW_START_UI_EVENT = "flowStartUIEvent";
    public static final String FLOW_SAVE_UI_EVENT = "flowSaveUIEvent";
    public static final String FLOW_COMPLETE_UI_EVENT = "flowCompleteUIEvent";

    // System micro service keys.
    public static final String EMAIL_TO = "emailTo";
    public static final String EMAIL_SUBJECT = "emailSubject";
    public static final String EMAIL_BODY = "emailBody";
    public static final String RESULT = "result";
}

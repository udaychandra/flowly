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

import io.flowly.core.verticles.ClusterAddresses;
import io.flowly.engine.compilers.Compiler;

/**
 * Defines the addresses of the event listeners that are defined on
 * flowly engine's local event bus.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class EngineAddresses extends ClusterAddresses {
    public static final String HOP_FLOW_INSTANCE = "io.flowly.engine:flow.instance.hop";
    public static final String HOP_INTO_FLOW_INSTANCE = "io.flowly.engine:flow.instance.hopInto";
    public static final String HOP_OUT_FLOW_INSTANCE = "-io.flowly.engine:flow.instance.hopOut";

    public static final String AWAIT_USER_INTERACTION = "io.flowly.engine:user.interaction.await";

    public static final String START_USER_INTERACTION_VIEW = "io.flowly.engine:user.interaction.view.start";

    public static final String FAIL_FLOW_INSTANCE = "io.flowly.engine:flow.instance.fail";

    public static final String SEND_EMAIL = "io.flowly.engine:email.send";

    public static final String FLOW_LIFECYCLE_EVENT = "io.flowly.engine:flow.event.lifecycle";

    // Repository addresses.
    public static final String REPO_FLOW_SAVE = "io.flowly.engine:repo.flow.save";
    public static final String REPO_FLOW_DELETE = "io.flowly.engine:repo.flow.delete";
    public static final String REPO_FLOW_NEXT_ROUTE = "io.flowly.engine:repo.flow.route.next";
    public static final String REPO_FLOW_CREATE_INSTANCE = "io.flowly.engine:repo.flow.create.instance";
    public static final String REPO_FLOW_COMPLETE_INSTANCE = "io.flowly.engine:repo.flow.instance.complete";
    public static final String REPO_FLOW_FAIL_INSTANCE = "io.flowly.engine:repo.flow.instance.fail";
    public static final String REPO_FLOW_SAVE_INSTANCE = "io.flowly.engine:repo.flow.instance.save";
    public static final String REPO_FLOW_CREATE_FLOW_OBJECT_INSTANCE = "io.flowly.engine:repo.flow.instance.flowObject.create";
    public static final String REPO_ASSIGN_TASK = "io.flowly.engine:repo.task.assign";
    public static final String REPO_UPDATE_TASK = "io.flowly.engine:repo.task.update";

    public static String getFlowObjectBusAddress(String appId, String flowObjectId) {
        return appId + Compiler.COLON + flowObjectId;
    }
}

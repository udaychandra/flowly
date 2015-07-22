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

package io.flowly.core.verticles;

/**
 * Defines the addresses of the event listeners that are available on flowly's clustered event bus.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class ClusterAddresses {
    public static final String DEPLOY_APP = "io.flowly.engine:app.deploy";

    public static final String UNDEPLOY_APP = "io.flowly.engine:app.undeploy";

    public static final String START_FLOW_INSTANCE = "io.flowly.engine:flow.instance.start";

    public static final String START_FLOW_INSTANCE_TASK = "io.flowly.engine:flow.instance.task.start";

    public static final String SAVE_FLOW_INSTANCE_TASK = "io.flowly.engine:flow.instance.task.save";

    public static final String COMPLETE_FLOW_INSTANCE_TASK = "io.flowly.engine:flow.instance.task.complete";

    public static final String GET_FLOW_INSTANCE_TASK = "io.flowly.engine:flow.instance.task.get";

    public static final String GET_USER_INBOX = "io.flowly.repo:user.inbox";

    public static final String GET_USER_FLOWS = "io.flowly.repo:user.flows";
}

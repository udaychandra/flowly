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

package io.flowly.engine.assets;

import io.flowly.engine.App;

import java.util.List;

/**
 * Represents a flow in an application.
 * Example flows - process, micro service, interactive service.
 *
 * @author <a>Uday Tatiraju</a>
 */
public interface Flow extends FlowObject {
    App getApp();
    void setApp(App app);

    List<Variable> getVariables();
    void addVariable(Variable variable);

    List<FlowObject> getFlowObjects();
    void addFlowObject(FlowObject flowObject);

    List<ConnectingObject> getConnectingObjects();
    void addConnectingObject(ConnectingObject connectingObject);
}
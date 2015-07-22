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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The abstract base class that can be extended to define specific flows.
 *
 * @author <a>Uday Tatiraju</a>
 */
public abstract class BasicFlow extends BasicFlowObject implements Flow {
    private App app;
    private List<Variable> variables = new ArrayList<>();
    private List<FlowObject> flowObjects = new ArrayList<>();
    private List<ConnectingObject> connectingObjects = new LinkedList<>();

    public BasicFlow() {

    }

    public BasicFlow(String id, String name, String description, String type, App app) {
        super(id, name, description, type);
        this.app = app;
    }

    @Override
    public App getApp() {
        return app;
    }

    @Override
    public void setApp(App app) {
        this.app = app;
    }

    @Override
    public List<Variable> getVariables() {
        return variables;
    }

    @Override
    public void addVariable(Variable variable) {
        this.variables.add(variable);
    }

    @Override
    public List<FlowObject> getFlowObjects() {
        return flowObjects;
    }

    @Override
    public void addFlowObject(FlowObject flowObject) {
        this.flowObjects.add(flowObject);
    }

    @Override
    public List<ConnectingObject> getConnectingObjects() {
        return connectingObjects;
    }

    @Override
    public void addConnectingObject(ConnectingObject connectingObject) {
        this.connectingObjects.add(connectingObject);
    }
}
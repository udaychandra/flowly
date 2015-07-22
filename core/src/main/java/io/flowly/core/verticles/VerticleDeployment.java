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

import io.vertx.core.DeploymentOptions;

import java.util.Objects;

/**
 * Represents the id, name and deployment options of a verticle - used to deploy verticle instances.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class VerticleDeployment {
    private String id;
    private String name;
    private DeploymentOptions deploymentOptions;

    public VerticleDeployment(String id, String name, DeploymentOptions deploymentOptions) {
        setId(id);
        this.name = name;
        this.deploymentOptions = deploymentOptions;
    }

    public String getId() {
        return id;
    }

    private void setId(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public String getName() {
        if (name == null) {
            return getId();
        }

        return name;
    }

    public DeploymentOptions getDeploymentOptions() {
        return deploymentOptions;
    }
}

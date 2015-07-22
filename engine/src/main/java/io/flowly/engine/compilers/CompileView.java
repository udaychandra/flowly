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

package io.flowly.engine.compilers;

import io.flowly.engine.EngineAddresses;
import io.flowly.engine.assets.Flow;
import io.flowly.engine.assets.View;

/**
 * Compiles a view into JavaScript.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class CompileView {
    public static void compile(View view, Flow flow, StringBuilder output) {
        startUserInteractionView(output, view, flow.getApp().getId());
    }

    /**
     * Outputs JavaScript code that corresponds to saving the current state of the flow instance and
     * updating the status of the flow object.
     *
     * @param output the string builder to which compiled JavaScript is written to.
     * @param view the view flow object used to construct the app specific event bus address.
     * @param appId the application id used to construct the event bus address.
     */
    private static void startUserInteractionView(StringBuilder output, View view, String appId) {
        Compiler.start(output, appId, view.getId());
        Compiler.send(output, null, EngineAddresses.START_USER_INTERACTION_VIEW);
        Compiler.end(output);
    }
}

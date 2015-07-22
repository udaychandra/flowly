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

import io.flowly.core.data.FlowInstance;
import io.flowly.core.data.FlowInstanceMetadata;
import io.flowly.core.data.FlowInstanceStep;
import io.flowly.engine.assets.Condition;
import io.flowly.engine.assets.DecisionGateway;
import io.flowly.engine.assets.Flow;

/**
 * Compiles a decision gateway into JavaScript.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class CompileDecisionGateway {
    public static void compile(DecisionGateway decisionGateway, Flow flow, StringBuilder output) {
        Compiler.start(output, flow.getApp().getId(), decisionGateway.getId());
        Compiler.loadInstance(output);

        output.append("    var connectingObjectIds = [];");
        output.append(Compiler.LINE_BREAK).append(Compiler.LINE_BREAK);

        for (int i=0; i<decisionGateway.getConditions().size() - 1; i++) {
            Condition condition = decisionGateway.getConditions().get(i);

            if (i == 0) {
                output.append(
                        "    if (").append(condition.getWhen()).append(") {");
            }
            else {
                output.append(
                        "    else if (").append(condition.getWhen()).append(") {");
            }

            output.append(Compiler.LINE_BREAK);
            output.append(
                        "        connectingObjectIds.push('").append(condition.getConnectTo()).append("');");
            output.append(Compiler.LINE_BREAK);
            output.append(
                        "    }");
        }

        // This will throw an error if no conditions are defined for the gateway.
        // We have to capture such errors while saving the file (back-end validation).
        Condition lastCondition = decisionGateway.getConditions().get(decisionGateway.getConditions().size() - 1);
        output.append(Compiler.LINE_BREAK);

        if (decisionGateway.getConditions().size() > 1) {
            output.append(
                        "    else {");
            output.append(Compiler.LINE_BREAK);
            output.append(
                    "        connectingObjectIds.push('").append(lastCondition.getConnectTo()).append("');");
            output.append(Compiler.LINE_BREAK);
            output.append(
                        "    }");
        }
        else {
            output.append(
                        "    connectingObjectIds.push('").append(lastCondition.getConnectTo()).append("');");
        }

        output.append(Compiler.LINE_BREAK).append(Compiler.LINE_BREAK);

        // Update flow instance's current step.
        output.append("    instance.").append(FlowInstance.META_DATA).append(Compiler.DOT).
                append(FlowInstanceMetadata.CURRENT_STEP);
        output.append(Compiler.DOT).append(FlowInstanceStep._FLOW_OBJECT_CONNECTING_OBJECT_IDS).
                append(" = connectingObjectIds;");

        output.append(Compiler.LINE_BREAK).append(Compiler.LINE_BREAK);
        Compiler.hop(output, null);
        Compiler.end(output);
    }
}

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
import io.flowly.engine.EngineAddresses;
import io.flowly.engine.JsonKeys;
import io.flowly.engine.assets.Flow;
import io.flowly.engine.assets.InteractiveService;
import io.flowly.engine.assets.MicroService;
import io.flowly.engine.assets.SubFlow;

/**
 * Compiles a sub flow (a flow object that defines a sub flow) into JavaScript.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class CompileSubFlow {
    public static void compile(SubFlow subFlow, Flow flow, StringBuilder output) {
        if (subFlow.getSubFlowType().equals(MicroService.class.getSimpleName())) {
            // If address is present, this is a system service call.
            String serviceAddress = subFlow.getAddress();

            if (serviceAddress != null && serviceAddress.length() > 0) {
                callService(output, subFlow, flow.getApp().getId());
            }
            else {
                callIntoSubFlow(output, subFlow, flow.getApp().getId());
            }
        }
        else if (subFlow.getSubFlowType().equals(InteractiveService.class.getSimpleName())) {
            awaitUserInteraction(output, subFlow, flow.getApp().getId());
        }
    }

    /**
     * Outputs JavaScript that corresponds to pausing the flow instance and waiting for user interaction.
     *
     * @param output the string builder to which compiled JavaScript is written to.
     * @param subFlow the sub flow object used to construct the app specific event bus address.
     * @param appId the application id used to construct the event bus address.
     */
    private static void awaitUserInteraction(StringBuilder output, SubFlow subFlow, String appId) {
        Compiler.start(output, appId, subFlow.getId());
        Compiler.loadInstance(output);

        if (Compiler.createInputData(output, subFlow.getDataMappings())) {
            output.append(Compiler.TAB_BREAK).append(JsonKeys.INSTANCE).append(Compiler.DOT).
                    append(FlowInstance.INPUT_DATA).append(" = inputData;");
            output.append(Compiler.LINE_BREAK);
            output.append(Compiler.LINE_BREAK);
        }

        Compiler.send(output, JsonKeys.INSTANCE, EngineAddresses.AWAIT_USER_INTERACTION);
        Compiler.end(output);

        // Register handler to move the flow forward after user interaction is complete.
        registerHopOutSubFlow(output, subFlow, appId);
    }

    /**
     * Outputs JavaScript that corresponds to making a call to a registered system micro service.
     * Based on the success/failure of the callback, the appropriate instance verticle handler is invoked.
     *
     * @param output the string builder to which compiled JavaScript is written to.
     * @param subFlow the sub flow object used to construct the app specific event bus address.
     * @param appId the application id used to construct the event bus address.
     */
    private static void callService(StringBuilder output, SubFlow subFlow, String appId) {
        Compiler.start(output, appId, subFlow.getId());
        Compiler.loadInstance(output);
        Compiler.createInputData(output, subFlow.getDataMappings());

        output.append("    eb.send('").append(subFlow.getAddress()).append("', inputData, ").append("function(reply, replyError) {");
        output.append(Compiler.LINE_BREAK);
        output.append("        if (replyError == null) {");
        output.append(Compiler.LINE_BREAK).append(Compiler.TAB_BREAK).append(Compiler.TAB_BREAK);
        loadAndClearOutputData(output, subFlow, "reply.body()");
        output.append(Compiler.TAB_BREAK).append(Compiler.TAB_BREAK);
        Compiler.hop(output, null);
        output.append("        }");
        output.append(Compiler.LINE_BREAK);
        output.append("        else {");
        output.append(Compiler.LINE_BREAK);
        output.append("            console.error(replyError);");
        output.append(Compiler.LINE_BREAK).append(Compiler.TAB_BREAK).append(Compiler.TAB_BREAK);
        Compiler.fail(output, null);
        output.append("        }");
        output.append(Compiler.LINE_BREAK).append(Compiler.TAB_BREAK);
        Compiler.end(output);

        Compiler.end(output);
    }

    /**
     * Outputs JavaScript that prepares the input data for a sub-flow and starts the sub-flow.
     *
     * @param output the string builder to which compiled JavaScript is written to.
     * @param subFlow the sub flow object used to construct the app specific event bus address.
     * @param appId the application id used to construct the event bus address.
     */
    private static void callIntoSubFlow(StringBuilder output, SubFlow subFlow, String appId) {
        Compiler.start(output, appId, subFlow.getId());
        Compiler.loadInstance(output);

        if (Compiler.createInputData(output, subFlow.getDataMappings())) {
            output.append(Compiler.TAB_BREAK).append(JsonKeys.INSTANCE).append(Compiler.DOT).
                    append(FlowInstance.INPUT_DATA).append(" = inputData;");
            output.append(Compiler.LINE_BREAK);
            output.append(Compiler.LINE_BREAK);
        }

        Compiler.hopInto(output, null);
        Compiler.end(output);
        registerHopOutSubFlow(output, subFlow, appId);
    }

    private static void registerHopOutSubFlow(StringBuilder output, SubFlow subFlow, String appId) {
        Compiler.start(output, appId, subFlow.getId() + EngineAddresses.HOP_OUT_FLOW_INSTANCE);
        Compiler.loadInstance(output);
        loadAndClearOutputData(output, subFlow, "instance.outputData");
        Compiler.hop(output, null);
        Compiler.end(output);
    }

    private static void loadAndClearOutputData(StringBuilder output, SubFlow subFlow, String outputData) {
        output.append("    var outputData = ").append(outputData).append(";");
        output.append(Compiler.LINE_BREAK);
        Compiler.mapOutputData(output, subFlow.getDataMappings());

        // Clear output data.
        output.append("    instance.outputData = null;");
        output.append(Compiler.LINE_BREAK);
    }
}

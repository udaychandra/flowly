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

import io.flowly.engine.assets.DataMapping;
import io.flowly.engine.assets.DecisionGateway;
import io.flowly.engine.assets.EndEvent;
import io.flowly.engine.assets.Flow;
import io.flowly.engine.assets.InlineScript;
import io.flowly.engine.assets.InteractiveService;
import io.flowly.engine.assets.MicroService;
import io.flowly.engine.assets.Process;
import io.flowly.engine.assets.StartEvent;
import io.flowly.engine.assets.SubFlow;
import io.flowly.engine.assets.Variable;
import io.flowly.engine.assets.View;

import java.util.List;

/**
 * Compile assets in a flowly application based on the Visitor pattern.
 *
 * @author <a>Uday Tatiraju</a>
 */
public interface Compiler {
    String LINE_BREAK = "\n";
    String TAB_BREAK = "    ";
    String DOT = ".";
    String SEMI_COLON = ";";
    String COLON = ":";

    void compile(Process process, Flow flow, StringBuilder output);
    void compile(StartEvent startEvent, Flow flow, StringBuilder output);
    void compile(EndEvent endEvent, Flow flow, StringBuilder output);
    void compile(InlineScript inlineScript, Flow flow, StringBuilder output);
    void compile(SubFlow subFlow, Flow flow, StringBuilder output);
    void compile(DecisionGateway decisionGateway, Flow flow, StringBuilder output);
    void compile(InteractiveService interactiveService, Flow flow, StringBuilder output);
    void compile(View view, Flow flow, StringBuilder output);
    void compile(MicroService microService, Flow flow, StringBuilder output);

    /**
     * Writes the compiled JavaScript to a file. The name of the file and
     * its path is pulled from the given flow.
     *
     * @param flow the flow whose compiled output is to be written to a file.
     * @param output the string builder which holds the compiled JavaScript of the flow.
     * @return the absolute path of the file to which the compiled JavaScript is written to.
     */
    String writeFlow(Flow flow, StringBuilder output);

    /**
     * Outputs JavaScript that corresponds to "app + flow object" specific event bus
     * address registration. It marks the beginning of a flow object.
     * Ex: eb.consumer('com.test.app1:1001', function(pMessage) {
     *
     * @param output the string builder to which the compiled JavaScript is written to.
     * @param appId the application id used to generate the event bus address of the flow object.
     * @param flowObjectId the flow object id used in event bus address creation.
     */
    static void start(StringBuilder output, String appId, String flowObjectId) {
        output.append("eb.consumer('");
        output.append(EngineAddresses.getFlowObjectBusAddress(appId, flowObjectId));
        output.append("', function(pMessage) {");
        output.append(LINE_BREAK);
    }

    /**
     * Outputs JavaScript that corresponds to sending a "move token" message on the event bus
     * asking the FlowInstance verticle to move the token forward.
     * Ex: eb.send('bpd.instance.hop', pMessage.body());
     *
     * @param output the string builder to which compiled JavaScript is written to.
     * @param hopData the JSON data to pass to the instance verticle.
     */
    static void hop(StringBuilder output, String hopData) {
        send(output, hopData, EngineAddresses.HOP_FLOW_INSTANCE);
    }

    /**
     * Outputs JavaScript that corresponds to sending a "start sub-flow token" message on the event bus
     * asking the FlowInstance verticle to dive into a sub-flow.
     * Ex: eb.send('bpd.instance.hopInto', pMessage.body());
     *
     * @param output the string builder to which compiled JavaScript is written to.
     * @param hopIntoData the JSON data to pass to the instance verticle.
     */
    static void hopInto(StringBuilder output, String hopIntoData) {
        send(output, hopIntoData, EngineAddresses.HOP_INTO_FLOW_INSTANCE);
    }

    /**
     * Outputs JavaScript that corresponds to sending a "failure" message on the event bus
     * asking the instance verticle to update the status of the flow object.
     * Ex: eb.send('com.test.app1:bpd.instance.fail', pMessage.body());
     *
     * @param output the string builder to which compiled JavaScript is written to.
     * @param hopData the JSON data to pass to the instance verticle.
     */
    static void fail(StringBuilder output, String hopData) {
        send(output, hopData, EngineAddresses.FAIL_FLOW_INSTANCE);
    }

    /**
     * Outputs JavaScript that corresponds to sending an arbitrary message on the event bus.
     *
     * @param output the string builder to which compiled JavaScript is written to.
     * @param hopData the JSON data to pass to the instance verticle.
     * @param address appended to the app id to generate the event bus address.
     */
    static void send(StringBuilder output, String hopData, String address) {
        if (hopData == null) {
            hopData = "pMessage.body()";
        }

        output.append(TAB_BREAK);
        output.append("eb.send('");
        output.append(address);
        output.append("', ").append(hopData).append(");");
        output.append(LINE_BREAK);
    }

    /**
     * Complements the {@link #start} method to end the send message call.
     *
     * @param output the string builder to which compiled JavaScript is written to.
     */
    static void end(StringBuilder output) {
        output.append("});");
        output.append(LINE_BREAK);
        output.append(LINE_BREAK);
    }

    /**
     * Uses the data mapping list of the flow object to load input data variable with current data.
     *
     * @param output the string builder to which compiled JavaScript is written to.
     * @param dataMappings the flow object's data mappings.
     */
    static boolean createInputData(StringBuilder output, List<DataMapping> dataMappings) {
        boolean hasInputData = false;

        output.append("    var inputData = {};");
        output.append(Compiler.LINE_BREAK);

        for (DataMapping dataMapping : dataMappings) {
            if (!dataMapping.getScope().equals(Variable.INPUT_SCOPE)) {
                continue;
            }

            output.append("    inputData.").append(dataMapping.getTo()).append(" = ").append(dataMapping.getFrom());
            output.append(Compiler.SEMI_COLON).append(Compiler.LINE_BREAK);
            hasInputData = true;
        }

        output.append(Compiler.LINE_BREAK);
        return hasInputData;
    }

    /**
     * Uses the variables list of the flow to load output data variable with current data.
     *
     * @param output the string builder to which compiled JavaScript is written to.
     * @param variables the flow's list of variables.
     */
    static boolean createOutputData(StringBuilder output, List<Variable> variables) {
        boolean hasOutputData = false;

        output.append("    var outputData = {};");
        output.append(Compiler.LINE_BREAK);

        for (Variable variable : variables) {
            if (!variable.getScope().equals(Variable.OUTPUT_SCOPE)) {
                continue;
            }

            output.append("    outputData.").append(variable.getName()).append(" = ").
                    append("data.").append(variable.getName());
            output.append(Compiler.SEMI_COLON).append(Compiler.LINE_BREAK);
            hasOutputData = true;
        }

        output.append(Compiler.LINE_BREAK);
        return hasOutputData;
    }

    /**
     * Uses the variables list of the flow to load current data with input data variable.
     *
     * @param output the string builder to which compiled JavaScript is written to.
     * @param variables the flow's list of variables.
     */
    static void mapInputData(StringBuilder output, List<Variable> variables) {
        for (Variable variable : variables) {
            if (!variable.getScope().equals(Variable.INPUT_SCOPE)) {
                continue;
            }

            output.append("data.").append(variable.getName()).append(" = ").
                    append("inputData.").append(variable.getName());
            output.append(Compiler.SEMI_COLON).append(Compiler.LINE_BREAK);
        }

        output.append(Compiler.LINE_BREAK);
    }

    /**
     * Uses the data mapping list of the flow object to load current data with output data variable.
     *
     * @param output the string builder to which compiled JavaScript is written to.
     * @param dataMappings the flow object's data mappings.
     */
    static void mapOutputData(StringBuilder output, List<DataMapping> dataMappings) {
        for (DataMapping dataMapping : dataMappings) {
            if (!dataMapping.getScope().equals(Variable.OUTPUT_SCOPE)) {
                continue;
            }

            output.append(dataMapping.getTo()).append(" = ").append("outputData.").append(dataMapping.getFrom());
            output.append(Compiler.SEMI_COLON).append(Compiler.LINE_BREAK);
        }

        output.append(Compiler.LINE_BREAK);
    }

    /**
     * Outputs JavaSacript that pulls a flow instance from the message body.
     *
     * @param output the string builder to which compiled JavaScript is written to.
     */
    static void loadInstance(StringBuilder output) {
        output.append(TAB_BREAK).append("var instance = pMessage.body();").append(LINE_BREAK);
        output.append(TAB_BREAK).append("var data = instance.data;").append(LINE_BREAK);
    }
}

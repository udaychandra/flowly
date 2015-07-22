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

package io.flowly.engine.data.manager;

/**
 * Defines string constants that are used to create flowly graph schema.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class Schema {
    // Define "subject" vertex.
    public static final String V_SUBJECT = "subject";
    public static final String V_P_SUBJECT_ID = "subjectId";

    // Define "isAssigned" edge.
    public static final String E_IS_ASSIGNED = "isAssigned";
    public static final String E_P_RECEIVED_STATUS = "receivedStatus";
    public static final String E_P_ASSIGNED_ON = "assignedOn";
    public static final String E_P_AT_RISK_DATE = "atRiskDate";
    public static final String E_P_DUE_DATE = "dueDate";

    // Define "process instance" vertex.
    public static final String V_PROCESS_INSTANCE = "processInstance";
    public static final String V_P_PROCESS_FLOW_ID = "processFlowId";

    // Define "flow object instance" vertex which is part of a flow.
    public static final String V_FLOW_OBJECT_INSTANCE = "flowObjectInstance";
    public static final String V_P_INSTANCE_ID = "instanceId";
    public static final String V_P_FLOW_OBJECT_ID = "flowObjectId";
    public static final String V_P_META_DATA = "metaData";
    public static final String V_P_DATA = "data";
    public static final String V_P_INPUT_DATA = "inputData";
    public static final String V_P_OUTPUT_DATA = "outputData";

    // Define flow object edges that represent the flow direction.
    public static final String E_FLOW_TO = "flowTo";
    public static final String E_FLOW_INTO = "flowInto";
    public static final String E_FLOW_OUT = "flowOut";

    // Define "flow metadata" vertex.
    public static final String V_FLOW_METADATA = "flowMetadata";
    public static final String V_P_FLOW_ID = "flowId";
    public static final String V_P_FLOW_TYPE = "flowType";
    public static final String V_P_APP_ID = "appId";

    // Define "flow route" vertex.
    public static final String V_FLOW_ROUTE = "flowRoute";
    public static final String V_P_CURRENT_FLOW_OBJECT_ID = "currentFlowObjectId";
    public static final String V_P_CONNECTING_OBJECT_ID = "connectingObjectId";
    public static final String V_P_NEXT_FLOW_OBJECT_ID = "nextFlowObjectId";
    public static final String V_P_ROUTE_FLOW_ID = "routeFlowId";

    // General property keys.
    public static final String P_STATUS = "status";
    public static final String P_SUB_FLOW_ID = "subFlowId";
    public static final String P_SUBJECT = "subject";
    public static final String P_CAUSE = "cause";
}

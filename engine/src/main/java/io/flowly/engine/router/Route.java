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

package io.flowly.engine.router;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a route in a given flow.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class Route {
    /**
     * Represents the combination of next flow object id and the flow object's sub flow id (if any).
     */
    public static class Next {
        private String flowObjectId;
        private String subFlowId;

        public Next(String flowObjectId, String subFlowId) {
            this.flowObjectId = flowObjectId;
            this.subFlowId = subFlowId;
        }

        public String getFlowObjectId() {
            return flowObjectId;
        }

        public String getSubFlowId() {
            return subFlowId;
        }
    }

    private String previousFlowObjectId;
    private boolean valid;
    private boolean end;
    private List<Next> nextList;

    public Route() {
        nextList = new ArrayList<>();
    }

    public String getPreviousFlowObjectId() {
        return previousFlowObjectId;
    }

    public void setPreviousFlowObjectId(String previousFlowObjectId) {
        this.previousFlowObjectId = previousFlowObjectId;
    }

    public List<Next> getNextList() {
        return nextList;
    }

    public Next getNext() {
        return nextList.isEmpty() ? null : nextList.get(0);
    }

    public void addNext(String nextFlowObjectId, String subFlowId) {
        if (nextFlowObjectId.equals("-1")) {
            setEnd(true);
        }
        else {
            nextList.add(new Next(nextFlowObjectId, subFlowId));
        }
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isEnd() {
        return end;
    }

    protected void setEnd(boolean end) {
        this.end = end;
    }

    public boolean isSplit() {
        return nextList.size() > 1;
    }
}

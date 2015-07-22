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

package io.flowly.core.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a>Uday Tatiraju</a>
 */
public class SamplePojo {
    private String string;
    private boolean truthy;
    private long number;
    private int digit;
    private List<SamplePojo> children;

    public SamplePojo() {
    }

    public SamplePojo(String string, boolean truthy, long number, int digit) {
        this.string = string;
        this.truthy = truthy;
        this.number = number;
        this.digit = digit;

        children = new ArrayList<>();
    }

    public String getString() {
        return string;
    }

    public boolean isTruthy() {
        return truthy;
    }

    public long getNumber() {
        return number;
    }

    public int getDigit() {
        return digit;
    }

    public List<SamplePojo> getChildren() {
        return children;
    }

    public void addChild(SamplePojo child) {
        this.children.add(child);
    }
}

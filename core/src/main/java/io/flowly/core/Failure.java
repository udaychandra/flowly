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

package io.flowly.core;

/**
 * Application specific exception that uses a code and message.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class Failure extends Exception {
    private int code;

    /**
     * Constructs a failure object with the specified code and message.
     *
     * @param code number used to identity an exception in a given application.
     * @param message the message that describes the exception.
     */
    public Failure(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Constructs a failure object with the specified code, message and stack trace.
     *
     * @param code number used to identity an exception in a given application.
     * @param message the message that describes the exception.
     * @param cause the stack trace of the exception.
     */
    public Failure(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * Retrieve the application error code - used to identify the exception/failure.
     *
     * @return number used to identity this exception.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the concatenated failure code and the detailed failure message.
     *
     * @return failure code and the detailed failure message.
     */
    public String getError() {
        return getCode() + ": " + getMessage();
    }
}

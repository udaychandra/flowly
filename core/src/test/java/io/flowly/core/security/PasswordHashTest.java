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

package io.flowly.core.security;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a>Uday Tatiraju</a>
 */
public class PasswordHashTest {
    @Test
    public void basicPasswordHashTest() throws Exception {
        String passwordPrefix = "p\r\nassw0Rd!";

        for (int i=0; i<25; i++) {
            String password = passwordPrefix + i;
            String hash = PasswordHash.createHash(password);

            Assert.assertTrue("Password hashes do not match.", PasswordHash.validatePassword(password, hash));
        }
    }
}

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
public class PermissionTest {
    @Test
    public void testReadPermission() {
        Permission permission = new Permission();

        for (int i=0; i<4; i++) {
            permission.setRWX(i);
            Assert.assertFalse("Permission should not have read flag set to 1.", permission.canRead());
        }

        for (int i=4; i<8; i++) {
            permission.setRWX(i);
            Assert.assertTrue("Permission should have read flag set to 1.", permission.canRead());
        }
    }

    @Test
    public void testWritePermission() {
        Permission permission = new Permission();

        for (int i=0; i<8; i++) {
            permission.setRWX(i);

            if (i==2 || i==3 || i==6 || i==7) {
                Assert.assertTrue("Permission should have write flag set to 1.", permission.canWrite());
            }
            else {
                Assert.assertFalse("Permission should not have write flag set to 1.", permission.canWrite());
            }
        }
    }

    @Test
    public void testExecutePermission() {
        Permission permission = new Permission();

        for (int i=0; i<8; i++) {
            permission.setRWX(i);

            if (i==1 || i==3 || i==5 || i==7) {
                Assert.assertTrue("Permission should have execute flag set to 1.", permission.canExecute());
            }
            else {
                Assert.assertFalse("Permission should not have execute flag set to 1.", permission.canExecute());
            }
        }
    }

    @Test
    public void testMergePermissions() {
        Permission p1 = new Permission(1L, "R1", 1);
        Permission p2 = new Permission(1L, "R1", 2);
        Assert.assertEquals("rwx value should be 3", 3, Permission.merge(p1, p2).getInteger(Permission.RWX).intValue());

        p1 = new Permission(1L, "R1", 2);
        p2 = new Permission(1L, "R1", 4);
        Assert.assertEquals("rwx value should be 6", 6, Permission.merge(p1, p2).getInteger(Permission.RWX).intValue());

        p1 = new Permission(1L, "R1", 2);
        p2 = new Permission(1L, "R1", 4);
        Permission p3 = new Permission(1L, "R1", 1);
        Assert.assertEquals("rwx value should be 7", 7, Permission.merge(p1, p2, p3).getInteger(Permission.RWX).intValue());
    }

    @Test
    public void testStringConstructor() {
        Permission permission = new Permission("R1:x");
        Assert.assertTrue("Permission should have executable flag set to 1.", permission.canExecute());
        Assert.assertEquals("R1", permission.getResourceId());

        permission = new Permission("R2:R,w");
        Assert.assertTrue("Permission should have read flag set to 1.", permission.canRead());
        Assert.assertTrue("Permission should have write flag set to 1.", permission.canWrite());
        Assert.assertEquals("R2", permission.getResourceId());
    }

    @Test
    public void testSubSet() {
        Permission subSet = new Permission("R1:R");
        Permission superSet = new Permission("R1:R,W,X");

        Assert.assertTrue("Permission should be a subset of the other permission.", subSet.isSubSet(superSet));

        subSet = new Permission("R1:R,W");
        superSet = new Permission("r1:r,w");
        Assert.assertFalse("Permission should not be a subset of the other permission.", subSet.isSubSet(superSet));
    }
}

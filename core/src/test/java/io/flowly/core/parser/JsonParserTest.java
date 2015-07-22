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

import io.flowly.core.BaseTestWithVertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

/**
 *@author <a>Uday Tatiraju</a>
 */
@RunWith(VertxUnitRunner.class)
public class JsonParserTest extends BaseTestWithVertx {
    private String sampleJsonFile = getClass().getResource(File.separator + "sample_json.json").getPath();

    @Test
    public void parseJsonTest(TestContext context) throws Exception {
        Async async = context.async();

        Parser parser = new JsonParser(vertx.fileSystem());
        parser.parse(sampleJsonFile, SamplePojo.class, handler -> {
            context.assertTrue(handler.succeeded());

            SamplePojo samplePojo = handler.result();
            context.assertNotNull(samplePojo, "Sample POJO should not be null.");
            context.assertTrue(samplePojo.isTruthy(), "Truthy value of sample POJO should be true");
            context.assertEquals("sample test", samplePojo.getString(), "String value of sample POJO not as expected.");
            context.assertTrue(samplePojo.getChildren().size() == 1, "Sample POJO should have one child.");
            context.assertFalse(samplePojo.getChildren().get(0).isTruthy(),
                    "Sample POJO's child truthy should be false");

            async.complete();
        });
    }

    @Test
    public void playground() {
        JsonObject object = new JsonObject();
        object.put("one", "one");
        object.put("number", 10);

        System.out.println(object.encode());
    }
}

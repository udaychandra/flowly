//package io.flowly.engine.verticles;
//
//import io.vertx.core.Vertx;
//import io.vertx.core.http.HttpClient;
//import io.vertx.core.http.HttpClientRequest;
//import io.vertx.core.http.HttpMethod;
//import io.vertx.core.logging.Logger;
//import io.vertx.core.logging.LoggerFactory;
//import io.vertx.ext.unit.Async;
//import io.vertx.ext.unit.TestContext;
//import io.vertx.ext.unit.junit.VertxUnitRunner;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//@RunWith(VertxUnitRunner.class)
//public class RestIntegrationVerticle {
//    private static final Logger logger = LoggerFactory.getLogger(RestIntegrationVerticle.class);
//
//    private Vertx vertx;
//
//    @Before
//    public void setup() {
//        vertx = Vertx.vertx();
//
//
//    }
//
//    @Test
//    public void testTwitterTrends(TestContext context) {
//        Async async = context.async();
//        HttpClient client = vertx.createHttpClient();
//
//        HttpClientRequest request = client.request(HttpMethod.GET, "api.openweathermap.org", "/data/2.5/weather?q=London,uk",
//                response -> {
//                    logger.info("Received response with status code " + response.statusCode());
//
//                    response.bodyHandler(b -> {
//                        logger.info("Response: " + b.toString());
//                        async.complete();
//                    });
//                });
//
//
//        request.exceptionHandler(h -> {
//            logger.error(h.getMessage(), h.getCause());
//            context.fail();
//        });
//
//        request.end();
//    }
//}

package io.flowly.engine.verticles;

import io.flowly.core.security.OAuth2;
import io.flowly.engine.BaseTestWithVertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class TwitterTest extends BaseTestWithVertx {
    @Test
    public void getOAuthTokenBearerTest(TestContext context) throws Exception {
        Async async = context.async();

        HttpClientOptions options = new HttpClientOptions().
                setSsl(true).
                setTrustAll(true).
                setDefaultPort(443);
        HttpClient client = vertx.createHttpClient(options);

        HttpClientRequest request = client.request(HttpMethod.POST, "api.twitter.com", "/oauth2/token",
                response -> {
                    context.assertEquals(200, response.statusCode(), "Status code should be 200.");

                    response.bodyHandler(b -> {
                        JsonObject jsonObject = new JsonObject(b.toString());
                        context.assertEquals("AAAAAAAAAAAAAAAAAAAAACgrgwAAAAAAHpr8ng9mcrNx71gw7kKj7zGYm6s%3Dx5Zd3KKCULCfVc7m7yqPrJcMuphyl5dNkK273Vv9mhdlXHh4Hi",
                                jsonObject.getString("access_token"), "OAuth token not as expected.");
                        client.close();
                        async.complete();
                    });
                });

        request.exceptionHandler(h -> {
            client.close();
            context.fail(h.getCause());
        });

        OAuth2.prepareOAuthBearerTokenRequest(request, "3sghKmLY98rkGdCD9wC3FuL6d",
                "4r5kmq0SAHDU6EN4Na18kIofrRDkqEMOdLPyJWK8bQwAGrvEYb");
        request.end();
    }

    @Test
    public void getTwitterTrendsTest(TestContext context) {
        Async async = context.async();

        HttpClientOptions options = new HttpClientOptions().
                setSsl(true).
                setTrustAll(true).
                setDefaultPort(443);
        HttpClient client = vertx.createHttpClient(options);

        HttpClientRequest request = client.request(HttpMethod.GET, "api.twitter.com", "/1.1/trends/place.json?id=23424848",
                response -> {
                    context.assertEquals(200, response.statusCode(), "Status code should be 200.");
                    response.bodyHandler(h -> {
                        System.out.println(h.toString());

                        client.close();
                        async.complete();
                    });


                });

        request.putHeader("Authorization",
                "Bearer " + "AAAAAAAAAAAAAAAAAAAAACgrgwAAAAAAHpr8ng9mcrNx71gw7kKj7zGYm6s%3Dx5Zd3KKCULCfVc7m7yqPrJcMuphyl5dNkK273Vv9mhdlXHh4Hi");

        request.exceptionHandler(h -> {
            client.close();
            context.fail(h.getCause());
        });
        request.end();
    }
}

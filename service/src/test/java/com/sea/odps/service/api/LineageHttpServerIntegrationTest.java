package com.sea.odps.service.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

/**
 * @author lun.ma
 * @version $Id: LineageHttpServerIntegrationTest.java, v 0.1 2025-11-19 18:27 lun.ma Exp $
 */
public class LineageHttpServerIntegrationTest {

    private Vertx vertx;
    private WebClient client;
    private int port = 19090; // 随机端口也可以

    @Before
    public void startServer() throws Exception {
        vertx = Vertx.vertx();

        LineageHttpServer server = new LineageHttpServer(new ConnectorFactory());
        DeploymentOptions options =
                new DeploymentOptions().setConfig(new JsonObject().put("port", port));

        CountDownLatch latch = new CountDownLatch(1);
        final Throwable[] deployError = new Throwable[1];
        vertx.deployVerticle(
                server,
                options,
                ar -> {
                    if (ar.failed()) {
                        deployError[0] = ar.cause();
                    }
                    latch.countDown();
                });
        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("服务器启动超时");
        }
        if (deployError[0] != null) {
            throw new IllegalStateException("服务器启动失败", deployError[0]);
        }

        client =
                WebClient.create(
                        vertx,
                        new WebClientOptions().setDefaultHost("127.0.0.1").setDefaultPort(port));
    }

    @After
    public void stopServer() throws Exception {
        if (vertx != null) {
            CountDownLatch latch = new CountDownLatch(1);
            vertx.close(ar -> latch.countDown());
            latch.await(5, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testHealthEndpoint() throws Exception {
        Response resp = get("/health");

        assertEquals(200, resp.status);
        JsonObject json = new JsonObject(resp.body);
        assertEquals("UP", json.getString("status"));
        assertEquals("Lineage", json.getString("service"));
    }

    @Test
    public void testExtractLineageMissingSql() throws Exception {
        JsonObject request =
                new JsonObject()
                        .put("connectorType", "ODPS")
                        .put("connectorConfig", new JsonObject());

        Response resp = post("/api/lineage/extract", request.encode());
        assertEquals(400, resp.status);
        JsonObject json = new JsonObject(resp.body);
        assertTrue(json.getBoolean("error"));
        assertEquals("SQL 语句不能为空", json.getString("message"));
    }

    private Response get(String path) throws Exception {
        return send(client.get(path).timeout(2000), null);
    }

    private Response post(String path, String body) throws Exception {
        return send(
                client.post(path).putHeader("Content-Type", "application/json").timeout(5000),
                body);
    }

    private Response send(HttpRequest<Buffer> request, String body) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        final Response[] holder = new Response[1];
        final Throwable[] errors = new Throwable[1];

        io.vertx.core.Handler<io.vertx.core.AsyncResult<HttpResponse<Buffer>>> handler =
                ar -> {
                    if (ar.succeeded()) {
                        HttpResponse<Buffer> resp = ar.result();
                        holder[0] = new Response(resp.statusCode(), resp.bodyAsString());
                    } else {
                        errors[0] = ar.cause();
                    }
                    latch.countDown();
                };

        if (body == null) {
            request.send(handler);
        } else {
            request.sendBuffer(Buffer.buffer(body, StandardCharsets.UTF_8.name()), handler);
        }

        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new IllegalStateException("请求超时");
        }
        if (errors[0] != null) {
            throw new IllegalStateException("请求失败", errors[0]);
        }
        return holder[0];
    }

    private static class Response {
        final int status;
        final String body;

        Response(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }
}

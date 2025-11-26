package com.sea.odps.service.api;

import java.util.HashMap;
import java.util.Map;

import com.sea.odps.service.api.model.ColumnLineageTreeBuilder;
import com.sea.odps.service.api.model.LineageResultModel;
import com.sea.odps.service.connector.MetadataConnector;
import com.sea.odps.service.connector.MetadataConnector.MetadataException;
import com.sea.odps.service.lineage.Lineage;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * 血缘关系 HTTP 服务器。
 *
 * <p>提供 RESTful API 接口，接收 SQL 和连接器配置，返回 JSON 格式的表血缘和字段血缘。
 */
public class LineageHttpServer extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(LineageHttpServer.class);

    private final ConnectorFactory connectorFactory;
    private HttpServer server;

    public LineageHttpServer(ConnectorFactory connectorFactory) {
        this.connectorFactory = connectorFactory;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        Router router = Router.router(vertx);

        // 访问日志
        router.route().handler(this::logRequest);

        // 全局异常处理器
        router.route().failureHandler(this::handleFailure);

        // 配置 BodyHandler
        BodyHandler bodyHandler =
                BodyHandler.create()
                        .setHandleFileUploads(false)
                        .setDeleteUploadedFilesOnEnd(true)
                        .setMergeFormAttributes(true);

        router.route().handler(bodyHandler);
        // 健康检查接口
        router.get("/health").handler(this::handleHealth);

        // 提取血缘关系接口
        router.post("/api/lineage/extract").handler(this::handleExtractLineage);

        // 获取表血缘接口
        router.post("/api/lineage/tables").handler(this::handleTableLineage);

        // 获取字段血缘接口
        router.post("/api/lineage/columns").handler(this::handleColumnLineage);

        // 404 处理器
        router.route().handler(this::handleNotFound);

        // 启动服务器
        int port = config().getInteger("port", 8080);
        String host = config().getString("host", "127.0.0.1");

        LOGGER.info("正在启动 HTTP 服务器，监听地址: " + host + ":" + port);

        HttpServerOptions options = new HttpServerOptions();
        options.setPort(port);
        options.setHost(host);

        server =
                vertx.createHttpServer(options)
                        .requestHandler(router)
                        .exceptionHandler(this::handleServerException)
                        .listen(
                                port,
                                host,
                                result -> {
                                    if (result.succeeded()) {
                                        LOGGER.info("✓ HTTP 服务器已启动，监听地址: " + host + ":" + port);
                                        System.out.println(
                                                "✓ HTTP 服务器已启动，监听地址: " + host + ":" + port);
                                        startPromise.complete();
                                    } else {
                                        LOGGER.error("HTTP 服务器启动失败", result.cause());
                                        startPromise.fail(result.cause());
                                    }
                                });
    }

    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        if (server != null) {
            server.close(stopPromise);
        } else {
            stopPromise.complete();
        }
    }

    /** 健康检查接口。 */
    private void handleHealth(RoutingContext context) {
        LOGGER.info(
                "Handling /health request from "
                        + (context.request().remoteAddress() != null
                                ? context.request().remoteAddress().toString()
                                : "unknown"));

        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "UP");
            response.put("service", "Lineage");

            String jsonResponse = Json.encodePrettily(response);
            LOGGER.info("Sending health check response: " + jsonResponse);

            context.response()
                    .putHeader("Content-Type", "application/json; charset=utf-8")
                    .setStatusCode(200)
                    .end(jsonResponse);

            LOGGER.info("Health check response sent successfully");
        } catch (Exception e) {
            LOGGER.error("处理健康检查请求时发生异常", e);
            try {
                sendError(context, 500, "健康检查失败: " + e.getMessage());
            } catch (Exception ex) {
                LOGGER.error("发送错误响应时发生异常", ex);
                // 如果连错误响应都发不出去，至少记录日志
                if (!context.response().ended()) {
                    context.response().setStatusCode(500).end();
                }
            }
        }
    }

    /** 提取完整血缘关系接口。 */
    private void handleExtractLineage(RoutingContext context) {
        try {
            LineageRequest request =
                    Json.decodeValue(context.body().asString(), LineageRequest.class);

            if (request.getSql() == null || request.getSql().trim().isEmpty()) {
                sendError(context, 400, "SQL 语句不能为空");
                return;
            }

            if (request.getConnectorType() == null) {
                sendError(context, 400, "连接器类型不能为空");
                return;
            }

            // 创建连接器
            MetadataConnector connector =
                    connectorFactory.createConnector(
                            request.getConnectorType(), request.getConnectorConfig());

            // 提取血缘关系
            Lineage service = new Lineage(connector);
            LineageResultModel result = service.extractLineage(request.getSql());

            // 返回 JSON 响应
            LineageResponse response = new LineageResponse();
            response.setSql(result.getSql());
            response.setTableLineages(result.getTableLineages());
            response.setColumnLineages(result.getColumnLineages());
            // 生成树形结构
            response.setColumnLineageTree(
                    ColumnLineageTreeBuilder.buildTree(result.getColumnLineages()));

            context.response()
                    .putHeader("Content-Type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(response));

        } catch (MetadataException e) {
            LOGGER.warn("元数据服务错误", e);
            sendError(context, 500, "元数据服务错误: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            sendError(context, 400, "参数错误: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("服务器处理错误", e);
            sendError(context, 500, "服务器错误: " + e.getMessage());
        }
    }

    /** 获取表血缘接口。 */
    private void handleTableLineage(RoutingContext context) {
        try {
            LineageRequest request =
                    Json.decodeValue(context.body().asString(), LineageRequest.class);

            if (request.getSql() == null || request.getSql().trim().isEmpty()) {
                sendError(context, 400, "SQL 语句不能为空");
                return;
            }

            if (request.getConnectorType() == null) {
                sendError(context, 400, "连接器类型不能为空");
                return;
            }

            // 创建连接器
            MetadataConnector connector =
                    connectorFactory.createConnector(
                            request.getConnectorType(), request.getConnectorConfig());

            // 提取血缘关系
            Lineage service = new Lineage(connector);
            LineageResultModel result = service.extractLineage(request.getSql());

            // 只返回表血缘
            Map<String, Object> response = new HashMap<>();
            response.put("sql", result.getSql());
            response.put("tableLineages", result.getTableLineages());

            context.response()
                    .putHeader("Content-Type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(response));

        } catch (MetadataException e) {
            LOGGER.warn("元数据服务错误", e);
            sendError(context, 500, "元数据服务错误: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            sendError(context, 400, "参数错误: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("服务器处理错误", e);
            sendError(context, 500, "服务器错误: " + e.getMessage());
        }
    }

    /** 获取字段血缘接口。 */
    private void handleColumnLineage(RoutingContext context) {
        try {
            LineageRequest request =
                    Json.decodeValue(context.body().asString(), LineageRequest.class);

            if (request.getSql() == null || request.getSql().trim().isEmpty()) {
                sendError(context, 400, "SQL 语句不能为空");
                return;
            }

            if (request.getConnectorType() == null) {
                sendError(context, 400, "连接器类型不能为空");
                return;
            }

            // 创建连接器
            MetadataConnector connector =
                    connectorFactory.createConnector(
                            request.getConnectorType(), request.getConnectorConfig());

            // 提取血缘关系
            Lineage service = new Lineage(connector);
            LineageResultModel result = service.extractLineage(request.getSql());

            // 只返回字段血缘
            Map<String, Object> response = new HashMap<>();
            response.put("sql", result.getSql());
            response.put("columnLineages", result.getColumnLineages());
            // 生成树形结构
            response.put(
                    "columnLineageTree",
                    ColumnLineageTreeBuilder.buildTree(result.getColumnLineages()));

            context.response()
                    .putHeader("Content-Type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(response));

        } catch (MetadataException e) {
            LOGGER.warn("元数据服务错误", e);
            sendError(context, 500, "元数据服务错误: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            sendError(context, 400, "参数错误: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("服务器处理错误", e);
            sendError(context, 500, "服务器错误: " + e.getMessage());
        }
    }

    /** 发送错误响应。 */
    private void sendError(RoutingContext context, int statusCode, String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        error.put("statusCode", statusCode);

        LOGGER.warn(
                String.format(
                        "API 调用失败 %s %s -> %d (%s)",
                        context.request().method(), context.request().uri(), statusCode, message));

        context.response()
                .setStatusCode(statusCode)
                .putHeader("Content-Type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(error));
    }

    /** 路由处理失败处理器 - 处理路由处理链中的异常和错误。 */
    private void handleFailure(RoutingContext context) {
        Throwable failure = context.failure();
        int statusCode = context.statusCode();
        String uri = context.request().uri();
        String method = context.request().method().name();

        LOGGER.error(String.format("处理异常: %s %s -> %d", method, uri, statusCode), failure);

        // 确保响应已发送
        if (!context.response().ended()) {
            if (statusCode >= 400) {
                sendError(context, statusCode, failure != null ? failure.getMessage() : "请求处理失败");
            } else {
                sendError(context, 500, failure != null ? failure.getMessage() : "服务器内部错误");
            }
        }
    }

    /** 处理未匹配的路由（404）。 */
    private void handleNotFound(RoutingContext context) {
        LOGGER.warn("未匹配的路由: " + context.request().method() + " " + context.request().uri());
        sendError(context, 404, "接口不存在: " + context.request().uri());
    }

    /** 处理 HTTP 服务器级别的异常（网络层异常） */
    private void handleServerException(Throwable e) {
        LOGGER.error("HTTP 服务器异常", e);
    }

    /** 访问日志 */
    private void logRequest(RoutingContext context) {
        long start = System.currentTimeMillis();
        String method = context.request().method().name();
        String uri = context.request().uri();
        String remote =
                context.request().remoteAddress() != null
                        ? context.request().remoteAddress().toString()
                        : "unknown";
        LOGGER.info(String.format("收到请求 %s %s 来自 %s", method, uri, remote));

        context.addBodyEndHandler(
                v -> {
                    long duration = System.currentTimeMillis() - start;
                    int status =
                            context.response().getStatusCode() == -1
                                    ? 200
                                    : context.response().getStatusCode();
                    LOGGER.info(
                            String.format(
                                    "完成请求 %s %s -> %d (%d ms)", method, uri, status, duration));
                });

        context.next();
    }
}

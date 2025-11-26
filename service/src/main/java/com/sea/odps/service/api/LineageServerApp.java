package com.sea.odps.service.api;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * 血缘关系 HTTP 服务器应用入口。
 *
 * <p>启动 HTTP 服务器，提供 RESTful API 接口用于提取 SQL 血缘关系。
 *
 * <p>使用示例：
 *
 * <pre>
 * // 默认端口 8080
 * java -cp ... com.sea.odps.metadata.lineage.api.LineageServerApp
 *
 * // 指定端口
 * java -cp ... com.sea.odps.metadata.lineage.api.LineageServerApp --port 9090
 * </pre>
 *
 * <p>API 接口：
 *
 * <ul>
 *   <li>GET /health - 健康检查
 *   <li>POST /api/lineage/extract - 提取完整血缘关系（表血缘 + 字段血缘）
 *   <li>POST /api/lineage/tables - 只提取表血缘
 *   <li>POST /api/lineage/columns - 只提取字段血缘
 * </ul>
 */
public class LineageServerApp {

    public static void main(String[] args) {
        // 解析命令行参数
        final int[] portHolder = {8080};
        for (int i = 0; i < args.length; i++) {
            if ("--port".equals(args[i]) && i + 1 < args.length) {
                try {
                    portHolder[0] = Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    System.err.println("❌ 无效的端口号: " + args[i + 1]);
                    System.exit(1);
                }
            }
        }
        final int port = portHolder[0];

        // 创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();

        // 创建连接器工厂
        ConnectorFactory connectorFactory = new ConnectorFactory();

        // 创建 HTTP 服务器
        LineageHttpServer server = new LineageHttpServer(connectorFactory);

        // 配置部署选项
        DeploymentOptions options = new DeploymentOptions();
        JsonObject config = new JsonObject();
        config.put("port", port);
        // 默认监听 127.0.0.1，可通过配置修改
        String host = System.getProperty("server.host", "127.0.0.1");
        config.put("host", host);
        options.setConfig(config);

        // 部署服务器
        vertx.deployVerticle(
                server,
                options,
                result -> {
                    if (result.succeeded()) {
                        System.out.println("✓ 血缘关系 HTTP 服务器启动成功");
                        System.out.println("  监听地址: " + host + ":" + port);
                        System.out.println("  客户端连接地址:");
                        System.out.println("    http://" + host + ":" + port);
                        System.out.println("  API 文档:");
                        System.out.println(
                                "    GET  http://" + host + ":" + port + "/health - 健康检查");
                        System.out.println(
                                "    POST http://"
                                        + host
                                        + ":"
                                        + port
                                        + "/api/lineage/extract - 提取完整血缘关系");
                        System.out.println(
                                "    POST http://"
                                        + host
                                        + ":"
                                        + port
                                        + "/api/lineage/tables - 只提取表血缘");
                        System.out.println(
                                "    POST http://"
                                        + host
                                        + ":"
                                        + port
                                        + "/api/lineage/columns - 只提取字段血缘");
                    } else {
                        System.err.println("❌ 服务器启动失败: " + result.cause().getMessage());
                        result.cause().printStackTrace();
                        System.exit(1);
                    }
                });

        // 添加关闭钩子
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    System.out.println("\n正在关闭服务器...");
                                    vertx.close();
                                }));
    }
}

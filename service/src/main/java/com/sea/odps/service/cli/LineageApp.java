package com.sea.odps.service.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import com.aliyun.odps.Odps;
import com.aliyun.odps.OdpsException;
import com.aliyun.odps.account.Account;
import com.aliyun.odps.account.AliyunAccount;
import com.aliyun.odps.account.StsAccount;

import com.sea.odps.service.connector.ExcelMetadataConnector;
import com.sea.odps.service.connector.ExcelMetadataConnector.ExcelReader;
import com.sea.odps.service.connector.MetadataConnector;
import com.sea.odps.service.connector.MetadataConnector.MetadataException;
import com.sea.odps.service.connector.MockMetadataConnector;
import com.sea.odps.service.connector.OdpsMetastoreConnector;

/**
 * SQL 血缘关系分析应用入口。
 *
 * <p>使用示例：
 *
 * <pre>
 * // 使用默认 Mock 连接器
 * java -cp ... com.sea.odps.metadata.cli.LineageApp
 *
 * // 使用 Excel 元数据文件
 * java -cp ... com.sea.odps.metadata.cli.LineageApp metadata.xlsx
 *
 * // 使用 ODPS 连接器（通过配置文件）
 * java -cp ... com.sea.odps.metadata.cli.LineageApp --odps-config odps.properties
 * </pre>
 */
public class LineageApp {

    public static void main(String[] args) {
        // 1. 初始化元数据连接器
        MetadataConnector connector;

        if (args.length > 0 && args[0].equals("--odps-config")) {
            // 使用 ODPS 连接器
            if (args.length < 2) {
                System.err.println("❌ 错误: --odps-config 需要指定配置文件路径");
                System.err.println("用法: java LineageApp --odps-config <config-file>");
                return;
            }
            String configFile = args[1];
            try {
                connector = createOdpsConnector(configFile);
                System.out.println("✓ 已连接 ODPS 元数据服务（配置文件: " + configFile + "）");
            } catch (Exception e) {
                System.err.println("❌ 无法连接 ODPS 元数据服务: " + configFile);
                System.err.println("错误: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        } else if (args.length > 0) {
            // 从命令行参数获取元数据文件路径（Excel）
            String metadataFile = args[0];
            try {
                connector = createExcelConnector(metadataFile);
                System.out.println("✓ 已加载元数据文件: " + metadataFile);
            } catch (Exception e) {
                System.err.println("❌ 无法加载元数据文件: " + metadataFile);
                System.err.println("错误: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        } else {
            // 使用默认的测试元数据文件
            String defaultFile = "src/test/resources/metadata_sample.xlsx";
            try {
                connector = createExcelConnector(defaultFile);
                System.out.println("✓ 已加载默认元数据文件: " + defaultFile);
            } catch (Exception e) {
                System.err.println("❌ 无法加载默认元数据文件: " + defaultFile);
                System.err.println("请通过命令行参数指定元数据文件路径");
                System.err.println("用法: java LineageApp <metadata-file-path>");
                System.err.println("      java LineageApp --odps-config <config-file>");
                System.err.println("或者使用 Mock 连接器进行演示");
                // 使用 Mock 连接器作为演示
                connector = createMockConnector();
                System.out.println("✓ 使用 Mock 连接器（演示模式）");
            }
        }

        // 2. 创建可视化器
        LineageVisualizer visualizer = new LineageVisualizer(connector);

        // 3. 交互式输入 SQL
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n" + repeat("=", 80));
        System.out.println("SQL 血缘关系分析工具");
        System.out.println(repeat("=", 80));
        System.out.println("输入 SQL 语句进行分析（输入 'exit' 或 'quit' 退出）");
        System.out.println();

        while (true) {
            System.out.print("SQL> ");
            String sql = scanner.nextLine().trim();

            if (sql.isEmpty()) {
                continue;
            }

            if (sql.equalsIgnoreCase("exit") || sql.equalsIgnoreCase("quit")) {
                System.out.println("再见！");
                break;
            }

            if (sql.equalsIgnoreCase("help")) {
                printHelp();
                continue;
            }

            try {
                // 可视化血缘关系
                visualizer.visualize(sql);

                // 询问是否查看树形图
                System.out.print("\n是否查看树形血缘关系？(y/n): ");
                String choice = scanner.nextLine().trim();
                if (choice.equalsIgnoreCase("y") || choice.equalsIgnoreCase("yes")) {
                    visualizer.visualizeAsTree(sql);
                }
            } catch (MetadataException e) {
                System.err.println("❌ 元数据服务错误: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("❌ 处理错误: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println();
        }

        scanner.close();
    }

    /** 创建 Excel 元数据连接器。 */
    private static MetadataConnector createExcelConnector(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }

        ExcelReader reader =
                ExcelReader.of(
                        new FileInputStream(file),
                        inputStream -> {
                            // 这里需要实际的 Excel 解析逻辑
                            // 为了简化，我们返回空列表，实际使用时需要实现 ExcelRowMapper
                            throw new UnsupportedOperationException(
                                    "Excel 解析需要实现 ExcelRowMapper，请参考 ExcelMetadataConnectorTest");
                        });

        return new ExcelMetadataConnector(file.getName(), reader);
    }

    /**
     * 创建 ODPS 元数据连接器。
     *
     * @param configFile 配置文件路径，包含以下属性： - odps.endpoint: ODPS 服务端点 - odps.defaultProject: 默认项目名称 -
     *     odps.accessKeyId: AccessKey ID - odps.accessKeySecret: AccessKey Secret -
     *     odps.securityToken: Security Token（可选，STS 账户需要） - odps.accountType: 账户类型，ALIYUN 或 STS（默认
     *     ALIYUN） - odps.projectWhitelist: 项目白名单（可选，逗号分隔）
     * @return ODPS 元数据连接器
     * @throws IOException 配置文件读取失败
     * @throws OdpsException ODPS 连接失败
     */
    private static MetadataConnector createOdpsConnector(String configFile)
            throws IOException, OdpsException {
        Properties properties = new Properties();
        File file = new File(configFile);
        if (!file.exists()) {
            throw new IOException("配置文件不存在: " + configFile);
        }

        try (InputStream in = new FileInputStream(file)) {
            properties.load(in);
        }

        // 允许环境变量覆盖配置
        mergeEnv(properties, "odps.endpoint", "ODPS_ENDPOINT");
        mergeEnv(properties, "odps.defaultProject", "ODPS_PROJECT");
        mergeEnv(properties, "odps.accessKeyId", "ODPS_ACCESS_KEY_ID");
        mergeEnv(properties, "odps.accessKeySecret", "ODPS_ACCESS_KEY_SECRET");
        mergeEnv(properties, "odps.securityToken", "ODPS_SECURITY_TOKEN");
        mergeEnv(properties, "odps.accountType", "ODPS_ACCOUNT_TYPE");

        // 读取配置
        String endpoint =
                properties.getProperty("odps.endpoint", "https://service.odps.aliyun.com/api");
        String project = properties.getProperty("odps.defaultProject", "");
        String ak = properties.getProperty("odps.accessKeyId", "");
        String sk = properties.getProperty("odps.accessKeySecret", "");
        String token = properties.getProperty("odps.securityToken", "");
        String accountType = properties.getProperty("odps.accountType", "ALIYUN");

        // 验证必需参数
        if (ak.isEmpty() || sk.isEmpty() || project.isEmpty()) {
            throw new IllegalArgumentException(
                    "配置文件缺少必需参数: odps.accessKeyId, odps.accessKeySecret, odps.defaultProject");
        }

        // 创建账户
        Account account;
        if ("STS".equalsIgnoreCase(accountType)) {
            if (token.isEmpty()) {
                throw new IllegalArgumentException("STS 账户类型需要 odps.securityToken 参数");
            }
            account = new StsAccount(ak, sk, token);
        } else {
            account = new AliyunAccount(ak, sk);
        }

        // 创建 Odps 实例
        Odps odps = new Odps(account);
        odps.setEndpoint(endpoint);
        odps.setDefaultProject(project);

        // 读取项目白名单（可选）
        List<String> projectWhitelist = Collections.emptyList();
        String whitelistStr = properties.getProperty("odps.projectWhitelist", "");
        if (!whitelistStr.isEmpty()) {
            String[] projects = whitelistStr.split(",");
            projectWhitelist = new ArrayList<>();
            for (String p : projects) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty()) {
                    projectWhitelist.add(trimmed);
                }
            }
        }

        return new OdpsMetastoreConnector(odps, projectWhitelist);
    }

    /** 从环境变量合并配置。 */
    private static void mergeEnv(Properties properties, String propKey, String envKey) {
        String env = System.getenv(envKey);
        if (env != null && !env.isEmpty()) {
            properties.setProperty(propKey, env);
        }
    }

    /** 创建 Mock 连接器（用于演示）。 */
    private static MetadataConnector createMockConnector() {
        return new MockMetadataConnector();
    }

    /** 重复字符串（Java 8 兼容）。 */
    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    private static void printHelp() {
        System.out.println();
        System.out.println("【帮助信息】");
        System.out.println("  - 输入 SQL 语句进行分析");
        System.out.println("  - 输入 'exit' 或 'quit' 退出程序");
        System.out.println("  - 输入 'help' 显示此帮助信息");
        System.out.println();
        System.out.println("【使用方式】");
        System.out.println("  - Excel 元数据: java LineageApp <metadata-file-path>");
        System.out.println("  - ODPS 连接器: java LineageApp --odps-config <config-file>");
        System.out.println();
        System.out.println("【示例 SQL】");
        System.out.println("  SELECT t1.id, t1.name, t2.amount");
        System.out.println("  FROM ods.user_table t1");
        System.out.println("  LEFT JOIN ods.order_table t2 ON t1.id = t2.user_id;");
        System.out.println();
    }
}

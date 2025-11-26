package com.sea.odps.service.connector;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aliyun.odps.Odps;
import com.aliyun.odps.OdpsException;
import com.aliyun.odps.account.Account;
import com.aliyun.odps.account.AliyunAccount;
import com.aliyun.odps.account.StsAccount;

/**
 * 依赖真实 ODPS 服务的集成测试。默认读取 {@code src/test/resources/odps/odps-integration.properties} 中的配置，当
 * AccessKey/Project 仍是占位符时自动跳过。
 */
public class OdpsMetastoreConnectorIT {

    private static final String DEFAULT_ENDPOINT = "https://service.odps.aliyun.com/api";
    private static Properties properties;

    private static List<String> whiteProjects = Collections.singletonList("cicdata_meta_dev");

    @BeforeClass
    public static void loadProperties() throws IOException {
        properties = new Properties();
        try (InputStream in =
                OdpsMetastoreConnectorIT.class
                        .getClassLoader()
                        .getResourceAsStream("odps/odps-integration.properties")) {
            if (in != null) {
                properties.load(in);
            }
        }
        // 允许环境变量覆盖
        mergeEnv("odps.endpoint", "ODPS_ENDPOINT");
        mergeEnv("odps.defaultProject", "ODPS_PROJECT");
        mergeEnv("odps.accessKeyId", "ODPS_ACCESS_KEY_ID");
        mergeEnv("odps.accessKeySecret", "ODPS_ACCESS_KEY_SECRET");
        mergeEnv("odps.securityToken", "ODPS_SECURITY_TOKEN");
        mergeEnv("odps.accountType", "ODPS_ACCOUNT_TYPE");
    }

    private static void mergeEnv(String propKey, String envKey) {
        String env = System.getenv(envKey);
        if (env != null && !env.isEmpty()) {
            properties.setProperty(propKey, env);
        }
    }

    private boolean isConfigured() {
        String ak = properties.getProperty("odps.accessKeyId", "YOUR_ACCESS_KEY_ID");
        String sk = properties.getProperty("odps.accessKeySecret", "YOUR_ACCESS_KEY_SECRET");
        String project = properties.getProperty("odps.defaultProject", "YOUR_PROJECT_NAME");
        return !ak.contains("YOUR_") && !sk.contains("YOUR_") && !project.contains("YOUR_");
    }

    @Test
    public void listDatabasesAgainstRealOdps() throws Exception {
        Assume.assumeTrue("ODPS 集成测试未配置，跳过", isConfigured());
        try {
            OdpsMetastoreConnector connector =
                    new OdpsMetastoreConnector(buildOdps(), whiteProjects);
            List<MetadataConnector.DatabaseMeta> databases = connector.listDatabases();
            assertFalse("预期至少能列出一个 Project", databases.isEmpty());
        } catch (Exception ex) {
            // 如果是 SSL/网络错误，跳过测试（通常是环境问题）
            if (isSslOrNetworkError(ex)) {
                Assume.assumeNoException("ODPS 服务不可用（SSL/网络错误），跳过测试", ex);
            }
            throw ex;
        }
    }

    @Test
    public void listTablesAgainstRealOdps() throws Exception {
        Assume.assumeTrue("ODPS 集成测试未配置，跳过", isConfigured());
        try {
            Odps odps = buildOdps();
            OdpsMetastoreConnector connector = new OdpsMetastoreConnector(odps, whiteProjects);
            String project =
                    Objects.requireNonNull(
                            odps.getDefaultProject(),
                            "default project must be set for integration test");
            List<MetadataConnector.TableMeta> tables = connector.listTables(project);
            assertFalse("预期项目中至少存在一张表", tables.isEmpty());
        } catch (Exception ex) {
            // 如果是 SSL/网络错误，跳过测试（通常是环境问题）
            if (isSslOrNetworkError(ex)) {
                Assume.assumeNoException("ODPS 服务不可用（SSL/网络错误），跳过测试", ex);
            }
            throw ex;
        }
    }

    private boolean isFullyConfigured() {
        String ak = properties.getProperty("odps.accessKeyId", "");
        String sk = properties.getProperty("odps.accessKeySecret", "");
        String project = properties.getProperty("odps.defaultProject", "");
        return !ak.isEmpty()
                && !sk.isEmpty()
                && !project.isEmpty()
                && !ak.contains("YOUR_")
                && !sk.contains("YOUR_")
                && !project.contains("YOUR_");
    }

    private boolean isSslOrNetworkError(Throwable ex) {
        if (ex == null) {
            return false;
        }
        // 检查异常类型
        String className = ex.getClass().getName();
        if (className.contains("SSL")
                || className.contains("CertPath")
                || className.contains("PKIX")
                || className.contains("Handshake")) {
            return true;
        }
        // 检查异常消息
        String msg = ex.getMessage();
        if (msg != null) {
            String lowerMsg = msg.toLowerCase();
            if (lowerMsg.contains("ssl")
                    || lowerMsg.contains("certificate")
                    || lowerMsg.contains("pkix")
                    || lowerMsg.contains("handshake")
                    || lowerMsg.contains("connection")
                    || lowerMsg.contains("network")
                    || lowerMsg.contains("certification path")) {
                return true;
            }
        }
        // 递归检查 cause
        return isSslOrNetworkError(ex.getCause());
    }

    private Odps buildOdps() throws OdpsException {
        String endpoint = properties.getProperty("odps.endpoint", DEFAULT_ENDPOINT);
        String project = properties.getProperty("odps.defaultProject", "");
        String ak = properties.getProperty("odps.accessKeyId", "");
        String sk = properties.getProperty("odps.accessKeySecret", "");
        String token = properties.getProperty("odps.securityToken", "");
        String accountType = properties.getProperty("odps.accountType", "ALIYUN");

        Account account;
        if ("STS".equalsIgnoreCase(accountType)) {
            account = new StsAccount(ak, sk, token);
        } else {
            account = new AliyunAccount(ak, sk);
        }

        Odps odps = new Odps(account);
        odps.setEndpoint(endpoint);
        odps.setDefaultProject(project);
        return odps;
    }
}

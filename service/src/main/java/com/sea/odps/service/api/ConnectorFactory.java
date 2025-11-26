package com.sea.odps.service.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.aliyun.odps.Odps;
import com.aliyun.odps.account.Account;
import com.aliyun.odps.account.AliyunAccount;
import com.aliyun.odps.account.StsAccount;

import com.sea.odps.service.connector.ExcelMetadataConnector;
import com.sea.odps.service.connector.ExcelMetadataConnector.ExcelReader;
import com.sea.odps.service.connector.MetadataConnector;
import com.sea.odps.service.connector.MetadataConnector.CatalogType;
import com.sea.odps.service.connector.MetadataConnector.MetadataException;
import com.sea.odps.service.connector.OdpsMetastoreConnector;

/** 连接器工厂，根据类型和配置创建对应的连接器。 */
public class ConnectorFactory {

    /**
     * 创建连接器。
     *
     * @param connectorType 连接器类型（ODPS、EXCEL 等）
     * @param config 连接器配置
     * @return 元数据连接器
     * @throws MetadataException 当连接器创建失败时抛出
     */
    public MetadataConnector createConnector(String connectorType, Map<String, Object> config)
            throws MetadataException {
        if (connectorType == null) {
            throw new IllegalArgumentException("连接器类型不能为空");
        }

        try {
            CatalogType catalogType = CatalogType.valueOf(connectorType.toUpperCase());

            switch (catalogType) {
                case ODPS:
                    return createOdpsConnector(config);
                case EXCEL:
                    return createExcelConnector(config);
                default:
                    throw new IllegalArgumentException("不支持的连接器类型: " + connectorType);
            }
        } catch (IllegalArgumentException e) {
            throw new MetadataException("不支持的连接器类型: " + connectorType, e);
        }
    }

    /**
     * spotless:off
     * 创建 ODPS 连接器。
     *
     * @param config 配置信息，包含：
     *   - endpoint: ODPS 服务端点
     *   - project: 默认项目名称
     *   - accessKeyId: AccessKey ID
     *   - accessKeySecret: AccessKey Secret
     *   - securityToken: Security Token（可选，STS 账户需要）
     *   -accountType: 账户类型，ALIYUN 或 STS（默认 ALIYUN）
     *   - projectWhitelist: 项目白名单（可选，逗号分隔）
     *  spotless:on
     */
    private MetadataConnector createOdpsConnector(Map<String, Object> config)
            throws MetadataException {
        if (config == null) {
            throw new IllegalArgumentException("ODPS 连接器配置不能为空");
        }

        String endpoint = getString(config, "endpoint");
        String project = getString(config, "project");
        String accessKeyId = getString(config, "accessKeyId");
        String accessKeySecret = getString(config, "accessKeySecret");
        String securityToken = getString(config, "securityToken");
        String accountType = getString(config, "accountType", "ALIYUN");
        String projectWhitelistStr = getString(config, "projectWhitelist");

        if (endpoint == null || project == null || accessKeyId == null || accessKeySecret == null) {
            throw new IllegalArgumentException(
                    "ODPS 连接器配置不完整，需要: endpoint, project, accessKeyId, accessKeySecret");
        }

        try {
            Account account;
            if ("STS".equalsIgnoreCase(accountType)) {
                if (securityToken == null) {
                    throw new IllegalArgumentException("STS 账户需要 securityToken");
                }
                account = new StsAccount(accessKeyId, accessKeySecret, securityToken);
            } else {
                account = new AliyunAccount(accessKeyId, accessKeySecret);
            }

            Odps odps = new Odps(account);
            odps.setEndpoint(endpoint);
            odps.setDefaultProject(project);

            List<String> projectWhitelist = Collections.emptyList();
            if (projectWhitelistStr != null && !projectWhitelistStr.trim().isEmpty()) {
                String[] projects = projectWhitelistStr.split(",");
                projectWhitelist = new ArrayList<>();
                for (String p : projects) {
                    projectWhitelist.add(p.trim());
                }
            }

            return new OdpsMetastoreConnector(odps, projectWhitelist);
        } catch (Exception e) {
            throw new MetadataException("创建 ODPS 连接器失败", e);
        }
    }

    /**
     * 创建 Excel 连接器。
     *
     * @param config 配置信息， 包含： - filePath: Excel 文件路径
     */
    private MetadataConnector createExcelConnector(Map<String, Object> config)
            throws MetadataException {
        if (config == null) {
            throw new IllegalArgumentException("Excel 连接器配置不能为空");
        }

        String filePath = getString(config, "filePath");
        if (filePath == null) {
            throw new IllegalArgumentException("Excel 连接器配置需要 filePath");
        }

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new IOException("Excel 文件不存在: " + filePath);
            }

            // 注意：这里需要实际的 Excel 解析逻辑
            // 为了简化，我们创建一个基本的 ExcelReader
            // 实际使用时需要实现 ExcelRowMapper
            ExcelReader reader =
                    ExcelReader.of(
                            new FileInputStream(file),
                            inputStream -> {
                                // 这里需要实际的 Excel 解析逻辑
                                throw new UnsupportedOperationException(
                                        "Excel 解析需要实现 ExcelRowMapper，请参考 ExcelMetadataConnectorTest");
                            });

            return new ExcelMetadataConnector(file.getName(), reader);
        } catch (IOException e) {
            throw new MetadataException("创建 Excel 连接器失败: " + filePath, e);
        }
    }

    /** 从配置中获取字符串值。 */
    private String getString(Map<String, Object> config, String key) {
        Object value = config.get(key);
        return value != null ? value.toString() : null;
    }

    /** 从配置中获取字符串值，如果不存在则返回默认值。 */
    private String getString(Map<String, Object> config, String key, String defaultValue) {
        String value = getString(config, key);
        return value != null ? value : defaultValue;
    }
}

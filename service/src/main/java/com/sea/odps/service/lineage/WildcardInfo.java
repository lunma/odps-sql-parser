package com.sea.odps.service.lineage;

/**
 * 通配符信息。
 *
 * <p>用于标识字段是否为通配符字段（如 SELECT * 或 SELECT t1.*），并记录关联的表别名。
 */
class WildcardInfo {
    final boolean isWildcard;
    final String tableAlias;

    WildcardInfo(boolean isWildcard, String tableAlias) {
        this.isWildcard = isWildcard;
        this.tableAlias = tableAlias;
    }
}

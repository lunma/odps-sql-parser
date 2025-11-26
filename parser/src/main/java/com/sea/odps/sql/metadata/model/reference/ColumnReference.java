package com.sea.odps.sql.metadata.model.reference;

import java.util.Optional;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** 字段引用信息。 */
@Getter
@ToString
@EqualsAndHashCode(of = {"owner", "name", "raw"})
public class ColumnReference {

    private final String owner;

    private final String name;

    private final String raw;

    /** 表引用（可选）。当 owner 是表别名或表名时，可以关联到对应的 TableReference。 在元数据提取阶段通过表映射查找来确定。 */
    private final TableReference tableReference;

    /**
     * 构造函数
     *
     * @param owner 所有者（表名或表别名）
     * @param name 列名
     * @param raw 原始表达式
     */
    public ColumnReference(final String owner, final String name, final String raw) {
        this(owner, name, raw, null);
    }

    /**
     * 构造函数（带表引用）。
     *
     * @param owner 所有者（表名或表别名）
     * @param name 列名
     * @param raw 原始表达式
     * @param tableReference 表引用（可选）
     */
    public ColumnReference(
            final String owner,
            final String name,
            final String raw,
            final TableReference tableReference) {
        this.owner = owner;
        this.name = name;
        this.raw = raw;
        this.tableReference = tableReference;
    }

    /**
     * 获取表引用（如果存在）。
     *
     * @return 表引用，如果不存在则返回空
     */
    public Optional<TableReference> getTableReference() {
        return Optional.ofNullable(tableReference);
    }
}

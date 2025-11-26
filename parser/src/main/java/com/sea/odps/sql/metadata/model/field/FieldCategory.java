package com.sea.odps.sql.metadata.model.field;

/** 字段类型枚举。 */
public enum FieldCategory {
    /** 直接字段引用，如 select col from ... */
    DIRECT,
    /** 表达式字段，如 select a + b from ... */
    EXPRESSION,
    /** 聚合字段，如 select count(*) from ... */
    AGGREGATE
}

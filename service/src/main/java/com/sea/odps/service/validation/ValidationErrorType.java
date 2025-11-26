package com.sea.odps.service.validation;

/** 校验错误类型。 */
public enum ValidationErrorType {
    /** 列名冲突：多个表有相同的列名，SELECT * 会产生歧义。 */
    COLUMN_CONFLICT,

    /** 表不存在：SQL 中引用的表在元数据服务中不存在。 */
    TABLE_NOT_FOUND,

    /** 列不存在：SQL 中引用的列在表中不存在。 */
    COLUMN_NOT_FOUND,

    /** JOIN 条件错误：JOIN 条件中的列不存在或类型不兼容。 */
    JOIN_CONDITION_ERROR,

    /** 类型不兼容：表达式中的类型不兼容。 */
    TYPE_INCOMPATIBLE
}

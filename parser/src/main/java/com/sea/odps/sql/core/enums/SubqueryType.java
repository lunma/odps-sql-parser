package com.sea.odps.sql.core.enums;

/** 子查询类型枚举，表示子查询在 SQL 中的使用位置。 */
public enum SubqueryType {

    /** 投影子查询（SELECT 子句中的子查询）。 */
    PROJECTION_SUBQUERY,

    /** 表子查询（FROM 子句中的子查询）。 */
    TABLE_SUBQUERY,

    /** 谓词子查询（WHERE/HAVING 子句中的子查询）。 */
    PREDICATE_SUBQUERY,

    /** INSERT SELECT 子查询（INSERT 语句中的 SELECT 子查询）。 */
    INSERT_SELECT_SUBQUERY,

    /** EXISTS 子查询（EXISTS 条件中的子查询）。 */
    EXISTS_SUBQUERY
}

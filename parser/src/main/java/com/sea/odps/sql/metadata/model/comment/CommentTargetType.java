package com.sea.odps.sql.metadata.model.comment;

/** 注释目标类型枚举，表示注释所关联的 SQL 元素类型。 */
public enum CommentTargetType {
    /** 注释整个 SQL 语句。 */
    STATEMENT,

    /** 注释表。 */
    TABLE,

    /** 注释列。 */
    COLUMN,

    /** 注释字段（SELECT 子句中的字段）。 */
    FIELD,

    /** 注释 JOIN 关系。 */
    JOIN,

    /** 注释 WHERE 条件。 */
    WHERE,

    /** 注释 GROUP BY。 */
    GROUP_BY,

    /** 注释 HAVING 条件。 */
    HAVING,

    /** 注释 ORDER BY。 */
    ORDER_BY,

    /** 注释 LIMIT。 */
    LIMIT,

    /** 注释 CTE。 */
    CTE,

    /** 注释 HINT。 */
    HINT,

    /** 注释 LATERAL VIEW。 */
    LATERAL_VIEW,

    /** 未知目标（无法确定注释的具体目标）。 */
    UNKNOWN
}

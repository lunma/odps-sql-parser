package com.sea.odps.sql.core.segment.dml.pagination;

import com.sea.odps.sql.core.segment.SQLSegment;

/** 分页值片段接口，表示 LIMIT 子句中的值（偏移量或行数限制）。 可以是数字字面量或表达式。 */
public interface PaginationValueSegment extends SQLSegment {

    /**
     * 判断边界是否开放（用于某些数据库的特殊语法）。
     *
     * @return 边界是否开放
     */
    boolean isBoundOpened();
}

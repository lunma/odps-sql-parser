package com.sea.odps.sql.core.segment.dml.pagination.limit;

import java.util.Optional;

import com.sea.odps.sql.core.segment.SQLSegment;
import com.sea.odps.sql.core.segment.dml.pagination.PaginationValueSegment;
import com.sea.odps.sql.core.segment.generic.CommentSegment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** LIMIT 子句片段，表示 SELECT 语句的 LIMIT 分页子句。 包含偏移量（可选）和行数限制。 */
@RequiredArgsConstructor
@Getter
public class LimitSegment implements SQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final PaginationValueSegment offset;

    private final PaginationValueSegment rowCount;

    @lombok.Setter private CommentSegment comment;

    /**
     * 获取偏移量（OFFSET 值）。
     *
     * @return 偏移量，如果不存在则返回空
     */
    public Optional<PaginationValueSegment> getOffset() {
        return Optional.ofNullable(offset);
    }

    /**
     * 获取行数限制（LIMIT 值）。
     *
     * @return 行数限制，如果不存在则返回空
     */
    public Optional<PaginationValueSegment> getRowCount() {
        return Optional.ofNullable(rowCount);
    }
}

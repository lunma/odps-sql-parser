package com.sea.odps.sql.core.segment.dml.order;

import java.util.Collection;

import com.sea.odps.sql.core.segment.SQLSegment;
import com.sea.odps.sql.core.segment.dml.order.item.OrderByItemSegment;
import com.sea.odps.sql.core.segment.generic.CommentSegment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** ORDER BY 子句片段，表示 SELECT 语句的 ORDER BY 排序子句。 */
@RequiredArgsConstructor
@Getter
public final class OrderBySegment implements SQLSegment {

    private final int startIndex;

    private final int stopIndex;

    /** ORDER BY 排序项列表。 */
    private final Collection<OrderByItemSegment> orderByItems;

    /** ORDER BY 子句的注释。 */
    @lombok.Setter private CommentSegment comment;
}

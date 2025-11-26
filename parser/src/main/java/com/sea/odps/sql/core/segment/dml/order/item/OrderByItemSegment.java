package com.sea.odps.sql.core.segment.dml.order.item;

import java.util.Optional;

import com.sea.odps.sql.core.enums.NullsOrderType;
import com.sea.odps.sql.core.enums.OrderDirection;
import com.sea.odps.sql.core.segment.SQLSegment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 排序项片段抽象类，表示 ORDER BY 或 GROUP BY 子句中的一个排序项。 包含排序方向（ASC/DESC）和 NULL 值排序方式。 */
@RequiredArgsConstructor
@Getter
public abstract class OrderByItemSegment implements SQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final OrderDirection orderDirection;

    private final NullsOrderType nullsOrderType;

    /**
     * 获取 NULL 值排序方式。
     *
     * @return NULL 值排序方式，如果未指定则返回空
     */
    public Optional<NullsOrderType> getNullsOrderType() {
        return Optional.ofNullable(nullsOrderType);
    }

    /**
     * 获取 NULL 值排序方式（根据数据库类型返回默认值）。
     *
     * @param databaseType 数据库类型
     * @return NULL 值排序方式
     */
    public NullsOrderType getNullsOrderType(final String databaseType) {
        if (null != nullsOrderType) {
            return nullsOrderType;
        }
        if ("PostgreSQL".equals(databaseType)
                || "openGauss".equals(databaseType)
                || "Oracle".equals(databaseType)) {
            return OrderDirection.ASC.equals(orderDirection)
                    ? NullsOrderType.LAST
                    : NullsOrderType.FIRST;
        }
        return OrderDirection.ASC.equals(orderDirection)
                ? NullsOrderType.FIRST
                : NullsOrderType.LAST;
    }
}

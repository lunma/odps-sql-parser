package com.sea.odps.sql.core.segment.generic;

import java.util.Optional;

import com.sea.odps.sql.core.segment.SQLSegment;
import com.sea.odps.sql.core.value.identifier.IdentifierValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/** 所有者片段，表示标识符的所有者部分（如 `schema.table` 中的 `schema`）。 支持嵌套所有者（如 `db.schema.table`）。 */
@RequiredArgsConstructor
@Getter
@Setter
public final class OwnerSegment implements SQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final IdentifierValue identifier;

    private OwnerSegment owner;

    /**
     * 获取所有者片段（用于嵌套所有者，如 `db.schema.table` 中的 `db`）。
     *
     * @return 所有者片段，如果不存在则返回空
     */
    public Optional<OwnerSegment> getOwner() {
        return Optional.ofNullable(owner);
    }
}

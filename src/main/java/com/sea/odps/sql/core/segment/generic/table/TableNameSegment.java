package com.sea.odps.sql.core.segment.generic.table;

import com.sea.odps.sql.core.segment.SQLSegment;
import com.sea.odps.sql.core.value.identifier.IdentifierValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 表名片段，表示表的名称标识符。 */
@RequiredArgsConstructor
@Getter
public final class TableNameSegment implements SQLSegment {

  private final int startIndex;

  private final int stopIndex;

  private final IdentifierValue identifier;
}

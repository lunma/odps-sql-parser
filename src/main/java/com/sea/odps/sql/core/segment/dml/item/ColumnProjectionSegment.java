package com.sea.odps.sql.core.segment.dml.item;

import java.util.Optional;

import com.sea.odps.sql.core.segment.generic.AliasAvailable;
import com.sea.odps.sql.core.segment.generic.AliasSegment;
import com.sea.odps.sql.core.segment.generic.CommentSegment;
import com.sea.odps.sql.core.segment.generic.column.ColumnSegment;

import lombok.Getter;
import lombok.Setter;

/** 列投影片段，表示 SELECT 子句中的列投影（如 `SELECT column1`）。 包含列引用和别名（可选）。 */
public final class ColumnProjectionSegment implements ProjectionSegment, AliasAvailable {

  @Getter private final ColumnSegment column;

  @Setter private AliasSegment alias;

  @Getter @Setter private CommentSegment comment;

  public ColumnProjectionSegment(final ColumnSegment columnSegment) {
    column = columnSegment;
  }

  @Override
  public Optional<String> getAlias() {
    return null == alias ? Optional.empty() : Optional.ofNullable(alias.getIdentifier().getValue());
  }

  @Override
  public int getStartIndex() {
    return column.getStartIndex();
  }

  @Override
  public int getStopIndex() {
    return null == alias ? column.getStopIndex() : alias.getStopIndex();
  }
}

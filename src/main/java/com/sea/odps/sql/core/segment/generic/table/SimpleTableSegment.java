package com.sea.odps.sql.core.segment.generic.table;

import java.util.Optional;

import com.sea.odps.sql.core.segment.generic.AliasSegment;
import com.sea.odps.sql.core.segment.generic.CommentSegment;
import com.sea.odps.sql.core.segment.generic.OwnerAvailable;
import com.sea.odps.sql.core.segment.generic.OwnerSegment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/** 简单表片段，表示一个简单的表引用（如 `table1` 或 `schema.table1`）。 包含表名、所有者（可选）和别名（可选）。 */
@RequiredArgsConstructor
@Getter
public final class SimpleTableSegment implements TableSegment, OwnerAvailable {

  private final TableNameSegment tableName;

  @Setter private OwnerSegment owner;

  @Setter private AliasSegment alias;

  @Setter private CommentSegment comment;

  @Override
  public int getStartIndex() {
    if (null == owner) {
      return tableName.getStartIndex();
    }
    return owner.getOwner().isPresent()
        ? owner.getOwner().get().getStartIndex()
        : owner.getStartIndex();
  }

  @Override
  public int getStopIndex() {
    return null == alias ? tableName.getStopIndex() : alias.getStopIndex();
  }

  @Override
  public Optional<OwnerSegment> getOwner() {
    return Optional.ofNullable(owner);
  }

  @Override
  public Optional<String> getAlias() {
    return null == alias ? Optional.empty() : Optional.ofNullable(alias.getIdentifier().getValue());
  }
}

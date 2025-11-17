package com.sea.odps.sql.core.segment.dml.item;

import java.util.Optional;

import com.sea.odps.sql.core.segment.generic.AliasAvailable;
import com.sea.odps.sql.core.segment.generic.AliasSegment;
import com.sea.odps.sql.core.segment.generic.CommentSegment;
import com.sea.odps.sql.core.segment.generic.expr.ExpressionSegment;

import lombok.Getter;

/** 表达式投影片段，表示 SELECT 子句中的表达式投影（如 `SELECT expr AS alias`）。 包含表达式和别名（可选）。 */
public final class ExpressionProjectionSegment implements ProjectionSegment, AliasAvailable {

  @Getter private final ExpressionSegment expression;

  private AliasSegment alias;

  @lombok.Getter @lombok.Setter private CommentSegment comment;

  public ExpressionProjectionSegment(final ExpressionSegment expression) {
    this.expression = expression;
  }

  @Override
  public Optional<String> getAlias() {
    return null == alias ? Optional.empty() : Optional.ofNullable(alias.getIdentifier().getValue());
  }

  @Override
  public void setAlias(final AliasSegment alias) {
    this.alias = alias;
  }

  @Override
  public int getStartIndex() {
    return expression.getStartIndex();
  }

  @Override
  public int getStopIndex() {
    return null == alias ? expression.getStopIndex() : alias.getStopIndex();
  }
}

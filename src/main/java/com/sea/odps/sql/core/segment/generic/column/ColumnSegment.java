package com.sea.odps.sql.core.segment.generic.column;

import java.util.Optional;

import com.sea.odps.sql.core.segment.generic.OwnerAvailable;
import com.sea.odps.sql.core.segment.generic.OwnerSegment;
import com.sea.odps.sql.core.segment.generic.expr.ExpressionSegment;
import com.sea.odps.sql.core.value.identifier.IdentifierValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/** 列片段，表示 SQL 中的列引用（如 `column1` 或 `table.column1`）。 支持所有者（表名）和别名。 */
@RequiredArgsConstructor
@Getter
@Setter
public class ColumnSegment implements ExpressionSegment, OwnerAvailable {
  private final int startIndex;

  private final int stopIndex;

  private final IdentifierValue identifier;

  private OwnerSegment owner;

  /**
   * 获取带引号的限定名称（保留原始引号字符）。
   * 例如：`field1`、`table1`、field1、table1、`table1`.`field1`、`table1`.field1、table1.`field1` 或
   * table1.field1
   *
   * @return 带引号的限定名称
   */
  public String getQualifiedName() {
    return null == owner
        ? identifier.getValueWithQuoteCharacters()
        : String.join(
            ".",
            owner.getIdentifier().getValueWithQuoteCharacters(),
            identifier.getValueWithQuoteCharacters());
  }

  /**
   * 获取表达式（不带引号）。
   *
   * @return 表达式字符串
   */
  public String getExpression() {
    return null == owner
        ? identifier.getValue()
        : String.join(".", owner.getIdentifier().getValue(), identifier.getValue());
  }

  @Override
  public Optional<OwnerSegment> getOwner() {
    return Optional.ofNullable(owner);
  }

  @Override
  public int hashCode() {
    StringBuilder columnString = new StringBuilder();
    if (null != owner) {
      columnString.append(owner.getIdentifier().getValue());
    }
    columnString.append(identifier.getValue());
    return columnString.toString().hashCode();
  }
}

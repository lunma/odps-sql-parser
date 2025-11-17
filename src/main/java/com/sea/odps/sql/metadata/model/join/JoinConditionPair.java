package com.sea.odps.sql.metadata.model.join;

import com.sea.odps.sql.metadata.model.reference.ColumnReference;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** JOIN 条件中左右列的配对信息。 */
@Getter
@ToString
@EqualsAndHashCode(of = {"left", "right", "operator"})
public class JoinConditionPair {

  private final ColumnReference left;

  private final ColumnReference right;

  private final String operator;

  public JoinConditionPair(
      final ColumnReference left, final ColumnReference right, final String operator) {
    this.left = left;
    this.right = right;
    this.operator = operator;
  }
}

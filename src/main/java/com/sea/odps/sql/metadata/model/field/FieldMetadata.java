package com.sea.odps.sql.metadata.model.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.sea.odps.sql.metadata.model.reference.ColumnReference;

import lombok.Getter;

/** 字段元信息。 */
@Getter
public class FieldMetadata {

  private final String alias;

  private final String expression;

  private final FieldCategory category;

  private final List<ColumnReference> sourceColumns;

  public FieldMetadata(
      final String alias,
      final String expression,
      final FieldCategory category,
      final List<ColumnReference> sourceColumns) {
    this.alias = alias;
    this.expression = expression;
    this.category = null == category ? FieldCategory.EXPRESSION : category;
    List<ColumnReference> columns = new ArrayList<>();
    if (null != sourceColumns) {
      for (ColumnReference each : sourceColumns) {
        if (null != each) {
          columns.add(each);
        }
      }
    }
    this.sourceColumns = Collections.unmodifiableList(columns);
  }

  public Optional<String> getAliasOptional() {
    return Optional.ofNullable(alias).filter(s -> !s.isEmpty());
  }

  @Override
  public String toString() {
    return "FieldMetadata{"
        + "alias='"
        + alias
        + '\''
        + ", expression='"
        + expression
        + '\''
        + ", category="
        + category
        + ", sourceColumns="
        + sourceColumns
        + '}';
  }
}

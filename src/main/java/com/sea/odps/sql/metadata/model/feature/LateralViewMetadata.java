package com.sea.odps.sql.metadata.model.feature;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

/** LATERAL VIEW 元数据。 */
@Getter
public final class LateralViewMetadata {

  /** 表值函数表达式文本。 */
  private final String function;

  /** 表别名。 */
  private final String tableAlias;

  /** 列别名列表。 */
  private final List<String> columnAliases;

  /** 是否使用 OUTER 关键字。 */
  private final boolean outer;

  /**
   * 构造函数。
   *
   * @param function 表值函数表达式文本
   * @param tableAlias 表别名
   * @param columnAliases 列别名列表
   * @param outer 是否使用 OUTER 关键字
   */
  public LateralViewMetadata(
      final String function,
      final String tableAlias,
      final List<String> columnAliases,
      final boolean outer) {
    this.function = function;
    this.tableAlias = tableAlias;
    this.columnAliases =
        columnAliases != null
            ? Collections.unmodifiableList(columnAliases)
            : Collections.emptyList();
    this.outer = outer;
  }
}

package com.sea.odps.sql.metadata.model.function;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 函数调用元数据，表示 SQL 中的一个函数调用。 包含函数名称、类型、参数、调用位置等信息。 */
@RequiredArgsConstructor
@Getter
public final class FunctionCallMetadata {

  /** 函数名称（如 SUM、COUNT、SUBSTRING 等）。 */
  private final String functionName;

  /** 函数类型（聚合、窗口、字符串等）。 */
  private final FunctionType functionType;

  /** 函数参数表达式文本列表。 */
  private final List<String> arguments;

  /** 参数数量。 */
  private final int argumentCount;

  /** 函数调用的完整表达式文本。 */
  private final String expression;

  /** 函数调用所在的子句位置（SELECT、WHERE、HAVING、ORDER BY 等）。 */
  private final String clauseLocation;

  /** 函数别名（如果有，如 `SUM(amount) AS total`）。 */
  private final String alias;

  /** 是否为窗口函数（带 OVER 子句）。 */
  private final boolean isWindowFunction;

  /** 是否为聚合函数。 */
  private final boolean isAggregate;

  /** 是否为 DISTINCT 聚合（如 COUNT(DISTINCT col)）。 */
  private final boolean isDistinct;

  /**
   * 构造函数。
   *
   * @param functionName 函数名称
   * @param functionType 函数类型
   * @param arguments 参数列表
   * @param expression 完整表达式文本
   * @param clauseLocation 调用位置
   * @param alias 别名
   * @param isWindowFunction 是否为窗口函数
   * @param isAggregate 是否为聚合函数
   * @param isDistinct 是否为 DISTINCT 聚合
   */
  public FunctionCallMetadata(
      final String functionName,
      final FunctionType functionType,
      final List<String> arguments,
      final String expression,
      final String clauseLocation,
      final String alias,
      final boolean isWindowFunction,
      final boolean isAggregate,
      final boolean isDistinct) {
    this.functionName = functionName;
    this.functionType = functionType;
    this.arguments =
        null == arguments ? Collections.emptyList() : Collections.unmodifiableList(arguments);
    this.argumentCount = null == arguments ? 0 : arguments.size();
    this.expression = expression;
    this.clauseLocation = clauseLocation;
    this.alias = alias;
    this.isWindowFunction = isWindowFunction;
    this.isAggregate = isAggregate;
    this.isDistinct = isDistinct;
  }

  /**
   * 获取函数别名（如果存在）。
   *
   * @return 别名 Optional
   */
  public Optional<String> getAliasOptional() {
    return Optional.ofNullable(alias);
  }

  /**
   * 获取函数参数列表。
   *
   * @return 参数列表（不可修改）
   */
  public List<String> getArguments() {
    return arguments;
  }
}

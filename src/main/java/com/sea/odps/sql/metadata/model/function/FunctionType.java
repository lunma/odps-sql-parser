package com.sea.odps.sql.metadata.model.function;

/** 函数类型枚举，用于分类 SQL 中的函数调用。 */
public enum FunctionType {

  /** 聚合函数：SUM、COUNT、AVG、MIN、MAX、GROUP_CONCAT 等 */
  AGGREGATE,

  /** 窗口函数：ROW_NUMBER、RANK、DENSE_RANK、SUM() OVER 等 */
  WINDOW,

  /** 字符串函数：SUBSTRING、CONCAT、UPPER、LOWER、TRIM、REPLACE 等 */
  STRING,

  /** 数学函数：ABS、ROUND、FLOOR、CEIL、POWER、SQRT 等 */
  MATH,

  /** 日期时间函数：DATE_FORMAT、YEAR、MONTH、DAY、NOW、CURRENT_DATE 等 */
  DATE_TIME,

  /** 类型转换函数：CAST、CONVERT、TO_DATE、TO_NUMBER 等 */
  CAST,

  /** 条件函数：IF、CASE、COALESCE、NULLIF、IFNULL 等 */
  CONDITIONAL,

  /** 其他函数：无法分类或未知类型的函数 */
  OTHER
}

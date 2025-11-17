package com.sea.odps.sql.core.enums;

/** 集合操作类型枚举，表示 SELECT 语句的集合操作类型。 */
public enum CombineType {

  /** UNION ALL（保留重复行）。 */
  UNION_ALL,

  /** UNION（去除重复行）。 */
  UNION,

  /** INTERSECT ALL（保留重复行）。 */
  INTERSECT_ALL,

  /** INTERSECT（去除重复行）。 */
  INTERSECT,

  /** EXCEPT ALL（保留重复行）。 */
  EXCEPT_ALL,

  /** EXCEPT（去除重复行）。 */
  EXCEPT,

  /** MINUS ALL（保留重复行，Oracle 语法）。 */
  MINUS_ALL,

  /** MINUS（去除重复行，Oracle 语法）。 */
  MINUS
}

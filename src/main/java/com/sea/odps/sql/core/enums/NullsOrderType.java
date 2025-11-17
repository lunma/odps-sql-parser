package com.sea.odps.sql.core.enums;

/** NULL 值排序方式枚举，表示 ORDER BY 子句中 NULL 值的排序方式。 */
public enum NullsOrderType {

  /** NULL 值排在前面（NULLS FIRST）。 */
  FIRST,

  /** NULL 值排在后面（NULLS LAST）。 */
  LAST
}

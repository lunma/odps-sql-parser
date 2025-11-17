package com.sea.odps.sql.visitor.odps;

import com.sea.odps.sql.core.segment.dml.pagination.limit.LimitSegment;
import com.sea.odps.sql.core.segment.dml.window.WindowSegment;
import com.sea.odps.sql.core.statement.dml.SelectStatement;

import lombok.Getter;
import lombok.Setter;

/** ODPS SQL SELECT 语句实现类，扩展了 SELECT 语句以支持 ODPS 特有的功能。 包含 LIMIT 和 WINDOW 子句。 */
@Getter
@Setter
public class OdpsSQLSelectStatement extends SelectStatement {
  /** LIMIT 子句片段。 */
  private LimitSegment limit;

  /** WINDOW 子句片段。 */
  private WindowSegment window;
}

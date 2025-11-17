package com.sea.odps.sql.core.statement;

import com.sea.odps.sql.visitor.core.ASTNode;

/** SQL 语句接口，表示解析后的 SQL 语句。 所有 SQL 语句类型（SELECT、INSERT、UPDATE、DELETE 等）都实现此接口。 */
public interface SQLStatement extends ASTNode {}

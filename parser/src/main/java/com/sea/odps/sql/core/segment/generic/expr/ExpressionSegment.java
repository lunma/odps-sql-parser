package com.sea.odps.sql.core.segment.generic.expr;

import com.sea.odps.sql.core.segment.SQLSegment;

/** 表达式片段接口，表示 SQL 中的表达式。 可以是字面量、列引用、函数调用、子查询、二元运算等。 */
public interface ExpressionSegment extends SQLSegment {}

package com.sea.odps.sql.core.segment.generic.table;

import com.sea.odps.sql.core.segment.generic.AliasAvailable;

/** 表片段接口，表示 SQL 中的表引用。 可以是简单表、子查询表、JOIN 表等。 */
public interface TableSegment extends AliasAvailable {}

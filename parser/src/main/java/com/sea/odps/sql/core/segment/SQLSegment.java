package com.sea.odps.sql.core.segment;

import com.sea.odps.sql.visitor.core.ASTNode;

/** SQL 片段接口，表示 SQL 语句中的一个语义片段。 所有 SQL 语义片段（表、列、表达式、子句等）都实现此接口。 */
public interface SQLSegment extends ASTNode {

    /**
     * 获取片段在原始 SQL 中的起始位置索引。
     *
     * @return 起始索引
     */
    int getStartIndex();

    /**
     * 获取片段在原始 SQL 中的结束位置索引。
     *
     * @return 结束索引
     */
    int getStopIndex();
}

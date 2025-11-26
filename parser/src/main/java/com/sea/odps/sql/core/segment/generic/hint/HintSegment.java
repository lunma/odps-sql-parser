package com.sea.odps.sql.core.segment.generic.hint;

import java.util.Collection;
import java.util.LinkedList;

import com.sea.odps.sql.core.segment.SQLSegment;
import com.sea.odps.sql.core.segment.generic.CommentSegment;

import lombok.Getter;

/** HINT 片段，用于表示 SQL 中的优化提示。 */
@Getter
public final class HintSegment implements SQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final Collection<HintItemSegment> hintItems;

    @lombok.Setter private CommentSegment comment;

    /**
     * 构造函数。
     *
     * @param startIndex 起始索引
     * @param stopIndex 结束索引
     * @param hintItems HINT 项集合
     */
    public HintSegment(
            final int startIndex,
            final int stopIndex,
            final Collection<HintItemSegment> hintItems) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.hintItems = new LinkedList<>(hintItems);
    }
}

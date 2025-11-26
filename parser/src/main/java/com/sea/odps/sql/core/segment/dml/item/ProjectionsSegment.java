package com.sea.odps.sql.core.segment.dml.item;

import java.util.Collection;
import java.util.LinkedList;

import com.sea.odps.sql.core.segment.SQLSegment;

import lombok.Getter;

/** 投影片段，包含 SELECT 子句中的所有投影项。 */
@Getter
public class ProjectionsSegment implements SQLSegment {

    private final int startIndex;

    private final int stopIndex;

    private final Collection<ProjectionSegment> projections = new LinkedList<>();

    private final boolean distinct;

    /**
     * 构造函数。
     *
     * @param startIndex 起始索引
     * @param stopIndex 结束索引
     * @param projections 投影项集合
     */
    public ProjectionsSegment(
            int startIndex, int stopIndex, Collection<ProjectionSegment> projections) {
        this(startIndex, stopIndex, projections, false);
    }

    /**
     * 构造函数。
     *
     * @param startIndex 起始索引
     * @param stopIndex 结束索引
     * @param projections 投影项集合
     * @param distinct 是否包含 DISTINCT 关键字
     */
    public ProjectionsSegment(
            int startIndex,
            int stopIndex,
            Collection<ProjectionSegment> projections,
            boolean distinct) {
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.projections.addAll(projections);
        this.distinct = distinct;
    }
}

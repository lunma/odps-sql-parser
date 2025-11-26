package com.sea.odps.sql.metadata.model.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

/** 窗口元数据，表示 WINDOW 子句中的窗口定义。 */
@Getter
public final class WindowMetadata {

    /** 窗口名称。 */
    private final String windowName;

    /** 分区列列表（PARTITION BY）。 */
    private final List<String> partitionColumns;

    /** 排序列列表（ORDER BY）。 */
    private final List<String> orderColumns;

    /** 窗口框架类型（ROWS、RANGE、GROUPS）。 */
    private final String frameType;

    /**
     * 构造函数。
     *
     * @param windowName 窗口名称
     * @param partitionColumns 分区列列表
     * @param orderColumns 排序列列表
     * @param frameType 窗口框架类型
     */
    public WindowMetadata(
            final String windowName,
            final List<String> partitionColumns,
            final List<String> orderColumns,
            final String frameType) {
        this.windowName = windowName;
        this.partitionColumns =
                null == partitionColumns
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(new ArrayList<>(partitionColumns));
        this.orderColumns =
                null == orderColumns
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(new ArrayList<>(orderColumns));
        this.frameType = frameType;
    }
}

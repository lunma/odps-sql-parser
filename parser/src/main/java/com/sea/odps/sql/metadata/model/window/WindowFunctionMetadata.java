package com.sea.odps.sql.metadata.model.window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

/** 窗口函数元数据，表示 SELECT 表达式中的窗口函数调用。 */
@Getter
public final class WindowFunctionMetadata {

    /** 函数名称（如 ROW_NUMBER、SUM、AVG 等）。 */
    private final String functionName;

    /** 函数参数表达式列表。 */
    private final List<String> arguments;

    /** 引用的窗口名称（如果使用命名窗口，如 `OVER w1`）。 */
    private final String windowNameRef;

    /** 分区列列表（如果使用内联窗口规范）。 */
    private final List<String> partitionColumns;

    /** 排序列列表（如果使用内联窗口规范）。 */
    private final List<String> orderColumns;

    /** 窗口框架类型（如果使用内联窗口规范）。 */
    private final String frameType;

    /** 原始表达式文本。 */
    private final String expression;

    /**
     * 构造函数。
     *
     * @param functionName 函数名称
     * @param arguments 函数参数表达式列表
     * @param windowNameRef 引用的窗口名称
     * @param partitionColumns 分区列列表
     * @param orderColumns 排序列列表
     * @param frameType 窗口框架类型
     * @param expression 原始表达式文本
     */
    public WindowFunctionMetadata(
            final String functionName,
            final List<String> arguments,
            final String windowNameRef,
            final List<String> partitionColumns,
            final List<String> orderColumns,
            final String frameType,
            final String expression) {
        this.functionName = functionName;
        this.arguments =
                null == arguments
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(new ArrayList<>(arguments));
        this.windowNameRef = windowNameRef;
        this.partitionColumns =
                null == partitionColumns
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(new ArrayList<>(partitionColumns));
        this.orderColumns =
                null == orderColumns
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(new ArrayList<>(orderColumns));
        this.frameType = frameType;
        this.expression = expression;
    }
}

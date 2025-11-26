package com.sea.odps.sql.metadata.model.comment;

import com.sea.odps.sql.metadata.model.field.FieldMetadata;
import com.sea.odps.sql.metadata.model.reference.ColumnReference;
import com.sea.odps.sql.metadata.model.reference.TableReference;

import lombok.Getter;

/** 注释元数据，包含注释文本、位置信息以及关联的 SQL 元素信息。 */
@Getter
public final class CommentMetadata {

    /** 注释文本。 */
    private final String text;

    /** 注释在 SQL 中的起始位置。 */
    private final int startIndex;

    /** 注释在 SQL 中的结束位置。 */
    private final int stopIndex;

    /** 注释目标类型（表、列、字段等）。 */
    private final CommentTargetType targetType;

    /** 关联的表引用（如果注释的是表）。 */
    private final TableReference tableReference;

    /** 关联的列引用（如果注释的是列）。 */
    private final ColumnReference columnReference;

    /** 关联的字段元数据（如果注释的是字段）。 */
    private final FieldMetadata fieldMetadata;

    /** 关联的元素名称（用于无法通过引用关联的情况，如 CTE、HINT 等）。 */
    private final String targetName;

    /**
     * 构造函数（基础版本，不关联具体元素）。
     *
     * @param text 注释文本
     * @param startIndex 起始位置
     * @param stopIndex 结束位置
     */
    public CommentMetadata(final String text, final int startIndex, final int stopIndex) {
        this(text, startIndex, stopIndex, CommentTargetType.UNKNOWN, null, null, null, null);
    }

    /**
     * 完整构造函数。
     *
     * @param text 注释文本
     * @param startIndex 起始位置
     * @param stopIndex 结束位置
     * @param targetType 注释目标类型
     * @param tableReference 关联的表引用
     * @param columnReference 关联的列引用
     * @param fieldMetadata 关联的字段元数据
     * @param targetName 关联的元素名称
     */
    public CommentMetadata(
            final String text,
            final int startIndex,
            final int stopIndex,
            final CommentTargetType targetType,
            final TableReference tableReference,
            final ColumnReference columnReference,
            final FieldMetadata fieldMetadata,
            final String targetName) {
        this.text = text;
        this.startIndex = startIndex;
        this.stopIndex = stopIndex;
        this.targetType = targetType != null ? targetType : CommentTargetType.UNKNOWN;
        this.tableReference = tableReference;
        this.columnReference = columnReference;
        this.fieldMetadata = fieldMetadata;
        this.targetName = targetName;
    }

    /**
     * 创建关联表的注释元数据。
     *
     * @param text 注释文本
     * @param startIndex 起始位置
     * @param stopIndex 结束位置
     * @param tableReference 表引用
     * @return 注释元数据
     */
    public static CommentMetadata forTable(
            final String text,
            final int startIndex,
            final int stopIndex,
            final TableReference tableReference) {
        return new CommentMetadata(
                text,
                startIndex,
                stopIndex,
                CommentTargetType.TABLE,
                tableReference,
                null,
                null,
                null);
    }

    /**
     * 创建关联列的注释元数据。
     *
     * @param text 注释文本
     * @param startIndex 起始位置
     * @param stopIndex 结束位置
     * @param columnReference 列引用
     * @return 注释元数据
     */
    public static CommentMetadata forColumn(
            final String text,
            final int startIndex,
            final int stopIndex,
            final ColumnReference columnReference) {
        return new CommentMetadata(
                text,
                startIndex,
                stopIndex,
                CommentTargetType.COLUMN,
                null,
                columnReference,
                null,
                null);
    }

    /**
     * 创建关联字段的注释元数据。
     *
     * @param text 注释文本
     * @param startIndex 起始位置
     * @param stopIndex 结束位置
     * @param fieldMetadata 字段元数据
     * @return 注释元数据
     */
    public static CommentMetadata forField(
            final String text,
            final int startIndex,
            final int stopIndex,
            final FieldMetadata fieldMetadata) {
        return new CommentMetadata(
                text,
                startIndex,
                stopIndex,
                CommentTargetType.FIELD,
                null,
                null,
                fieldMetadata,
                null);
    }

    /**
     * 创建关联指定目标的注释元数据。
     *
     * @param text 注释文本
     * @param startIndex 起始位置
     * @param stopIndex 结束位置
     * @param targetType 目标类型
     * @param targetName 目标名称
     * @return 注释元数据
     */
    public static CommentMetadata forTarget(
            final String text,
            final int startIndex,
            final int stopIndex,
            final CommentTargetType targetType,
            final String targetName) {
        return new CommentMetadata(
                text, startIndex, stopIndex, targetType, null, null, null, targetName);
    }
}

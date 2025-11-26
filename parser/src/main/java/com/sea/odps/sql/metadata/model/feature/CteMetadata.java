package com.sea.odps.sql.metadata.model.feature;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

/** CTE（公共表表达式）元数据。 */
@Getter
public final class CteMetadata {

    /** CTE 名称。 */
    private final String name;

    /** CTE 列名列表。 */
    private final List<String> columns;

    /**
     * 构造函数。
     *
     * @param name CTE 名称
     * @param columns CTE 列名列表
     */
    public CteMetadata(final String name, final List<String> columns) {
        this.name = name;
        this.columns =
                columns != null ? Collections.unmodifiableList(columns) : Collections.emptyList();
    }
}

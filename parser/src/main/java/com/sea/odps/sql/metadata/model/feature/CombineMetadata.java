package com.sea.odps.sql.metadata.model.feature;

import lombok.Getter;

/** 集合操作（UNION/INTERSECT/EXCEPT）元数据。 */
@Getter
public final class CombineMetadata {

    /** 集合操作类型（UNION、INTERSECT、EXCEPT 等）。 */
    private final String combineType;

    /** 是否包含 ALL 关键字。 */
    private final boolean all;

    /**
     * 构造函数。
     *
     * @param combineType 集合操作类型
     * @param all 是否包含 ALL 关键字
     */
    public CombineMetadata(final String combineType, final boolean all) {
        this.combineType = combineType;
        this.all = all;
    }
}

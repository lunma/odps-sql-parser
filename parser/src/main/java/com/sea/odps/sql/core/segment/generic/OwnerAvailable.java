package com.sea.odps.sql.core.segment.generic;

import java.util.Optional;

import com.sea.odps.sql.core.segment.SQLSegment;

/** 所有者可用接口，表示可以设置所有者的片段（如表、列等）。 */
public interface OwnerAvailable extends SQLSegment {

    /**
     * 获取所有者片段。
     *
     * @return 所有者片段，如果不存在则返回空
     */
    Optional<OwnerSegment> getOwner();

    /**
     * 设置所有者片段。
     *
     * @param owner 所有者片段
     */
    void setOwner(OwnerSegment owner);
}

package com.sea.odps.sql.core.segment.generic;

import java.util.Optional;

import com.sea.odps.sql.core.segment.SQLSegment;

/** 别名可用接口，表示可以设置别名的片段（如表、子查询等）。 */
public interface AliasAvailable extends SQLSegment {
  /**
   * 获取别名。
   *
   * @return 别名，如果不存在则返回空
   */
  Optional<String> getAlias();

  /**
   * 设置别名。
   *
   * @param alias 别名片段
   */
  void setAlias(AliasSegment alias);
}

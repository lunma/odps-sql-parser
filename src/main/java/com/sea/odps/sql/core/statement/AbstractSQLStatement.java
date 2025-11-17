package com.sea.odps.sql.core.statement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.sea.odps.sql.core.segment.generic.CommentSegment;

/** SQL 语句抽象基类，提供注释片段管理功能。 所有具体的 SQL 语句类都继承此类。 */
public abstract class AbstractSQLStatement {

  private final Collection<CommentSegment> commentSegments = new LinkedList<>();

  /**
   * 获取注释片段集合。
   *
   * @return 注释片段集合
   */
  public Collection<CommentSegment> getCommentSegments() {
    return Collections.unmodifiableCollection(commentSegments);
  }

  /**
   * 添加注释片段。
   *
   * @param commentSegment 注释片段
   */
  public void addCommentSegment(final CommentSegment commentSegment) {
    if (null != commentSegment) {
      commentSegments.add(commentSegment);
    }
  }
}

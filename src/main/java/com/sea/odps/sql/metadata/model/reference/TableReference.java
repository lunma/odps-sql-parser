package com.sea.odps.sql.metadata.model.reference;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** 表引用信息，包括库名 / 所有者、表名、别名等。 */
@Getter
@ToString
@EqualsAndHashCode(of = {"owner", "name", "alias", "subquery"})
public class TableReference {

  private final String owner;

  private final String name;

  private final String alias;

  private final boolean subquery;

  private final String raw;

  public TableReference(
      final String owner,
      final String name,
      final String alias,
      final boolean subquery,
      final String raw) {
    this.owner = owner;
    this.name = name;
    this.alias = alias;
    this.subquery = subquery;
    this.raw = raw;
  }

  /**
   * 获取表的限定名（owner.name）。
   *
   * @return 限定名
   */
  public String getQualifiedName() {
    if (null == name) {
      return null;
    }
    return null == owner || owner.isEmpty() ? name : owner + "." + name;
  }
}

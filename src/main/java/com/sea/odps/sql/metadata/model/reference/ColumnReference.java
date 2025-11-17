package com.sea.odps.sql.metadata.model.reference;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** 字段引用信息。 */
@Getter
@ToString
@EqualsAndHashCode(of = {"owner", "name", "raw"})
public class ColumnReference {

  private final String owner;

  private final String name;

  private final String raw;

  public ColumnReference(final String owner, final String name, final String raw) {
    this.owner = owner;
    this.name = name;
    this.raw = raw;
  }
}

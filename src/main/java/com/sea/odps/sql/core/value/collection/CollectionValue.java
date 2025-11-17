package com.sea.odps.sql.core.value.collection;

import java.util.Collection;
import java.util.LinkedList;

import com.sea.odps.sql.core.value.ValueASTNode;

/** 集合值类，表示包含集合的 AST 节点值。 */
public final class CollectionValue<T> implements ValueASTNode<Collection> {

  private final Collection<T> value = new LinkedList<>();

  @Override
  public Collection getValue() {
    return value;
  }

  public void combine(final CollectionValue<T> collectionValue) {
    value.addAll(collectionValue.value);
  }
}

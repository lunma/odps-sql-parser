package com.sea.odps.sql.core.value.collection;

import java.util.Collection;
import java.util.LinkedList;

import com.sea.odps.sql.core.value.ValueASTNode;

/**
 * 集合值类，表示包含集合的 AST 节点值。
 *
 * <p>此类用于表示 SQL 中需要集合类型值的场景，例如：
 *
 * <ul>
 *   <li><b>IN 子句</b>：{@code WHERE id IN (1, 2, 3)} - 需要表示值列表
 *   <li><b>VALUES 子句</b>：{@code VALUES (1,2), (3,4)} - 需要表示多行值
 *   <li><b>数组字面量</b>：如果 SQL 支持数组类型，可以用于表示数组值
 *   <li><b>函数参数</b>：某些函数可能接受多个参数值作为集合
 * </ul>
 *
 * <p>目前此类尚未在解析器中实际使用，但保留以备未来实现上述 SQL 特性时使用。
 *
 * @param <T> 集合中元素的类型
 */
public final class CollectionValue<T> implements ValueASTNode<Collection> {

    private final Collection<T> value = new LinkedList<>();

    @Override
    public Collection getValue() {
        return value;
    }

    /**
     * 合并另一个集合值到当前集合中。
     *
     * @param collectionValue 要合并的集合值
     */
    public void combine(final CollectionValue<T> collectionValue) {
        value.addAll(collectionValue.value);
    }
}

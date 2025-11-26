package com.sea.odps.sql.core.value;

import com.sea.odps.sql.visitor.core.ASTNode;

/** 值 AST 节点接口，表示包含值的 AST 节点。 用于表示字面量、标识符等有值的节点。 */
public interface ValueASTNode<T> extends ASTNode {

    /**
     * 获取节点的值。
     *
     * @return 节点的值
     */
    T getValue();
}

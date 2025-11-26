package com.sea.odps.sql.visitor.core;

/**
 * SQL 访问者接口，用于访问 SQL AST 节点。
 *
 * <p>此接口为不同类型的 SQL 访问者提供统一的接口定义，支持访问者模式的实现。 未来可以扩展为多种访问者实现，例如：
 *
 * <ul>
 *   <li>AST 构建访问者（如 {@link com.sea.odps.sql.visitor.OdpsAstBuilderVisitor}）
 *   <li>SQL 格式化访问者（用于格式化 SQL 语句）
 *   <li>SQL 优化访问者（用于 SQL 查询优化）
 *   <li>SQL 重写访问者（用于 SQL 语句重写）
 * </ul>
 *
 * <p>目前此接口为空接口，作为访问者模式的标记接口使用。 具体的访问者实现可以直接继承 ANTLR 生成的 {@code OdpsParserBaseVisitor}，
 * 或实现此接口以提供统一的访问者类型定义。
 */
public interface SQLVisitor {}

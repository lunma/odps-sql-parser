package com.sea.odps.sql.visitor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sea.odps.sql.autogen.OdpsParser;
import com.sea.odps.sql.autogen.OdpsParserBaseVisitor;
import com.sea.odps.sql.core.enums.CombineType;
import com.sea.odps.sql.core.enums.NullsOrderType;
import com.sea.odps.sql.core.enums.OrderDirection;
import com.sea.odps.sql.core.segment.dml.LateralViewSegment;
import com.sea.odps.sql.core.segment.dml.WithSegment;
import com.sea.odps.sql.core.segment.dml.combine.CombineSegment;
import com.sea.odps.sql.core.segment.dml.expr.WindowFunctionSegment;
import com.sea.odps.sql.core.segment.dml.helper.PostSelectResult;
import com.sea.odps.sql.core.segment.dml.helper.PreSelectResult;
import com.sea.odps.sql.core.segment.dml.helper.SelectRestResult;
import com.sea.odps.sql.core.segment.dml.item.ColumnProjectionSegment;
import com.sea.odps.sql.core.segment.dml.item.ExpressionProjectionSegment;
import com.sea.odps.sql.core.segment.dml.item.ProjectionSegment;
import com.sea.odps.sql.core.segment.dml.item.ProjectionsSegment;
import com.sea.odps.sql.core.segment.dml.order.GroupBySegment;
import com.sea.odps.sql.core.segment.dml.order.OrderBySegment;
import com.sea.odps.sql.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import com.sea.odps.sql.core.segment.dml.order.item.OrderByItemSegment;
import com.sea.odps.sql.core.segment.dml.pagination.ExpressionPaginationValueSegment;
import com.sea.odps.sql.core.segment.dml.pagination.NumberLiteralPaginationValueSegment;
import com.sea.odps.sql.core.segment.dml.pagination.PaginationValueSegment;
import com.sea.odps.sql.core.segment.dml.pagination.limit.LimitSegment;
import com.sea.odps.sql.core.segment.dml.predicate.HavingSegment;
import com.sea.odps.sql.core.segment.dml.predicate.WhereSegment;
import com.sea.odps.sql.core.segment.dml.window.PartitionBySegment;
import com.sea.odps.sql.core.segment.dml.window.WindowDefinitionSegment;
import com.sea.odps.sql.core.segment.dml.window.WindowFrameBoundarySegment;
import com.sea.odps.sql.core.segment.dml.window.WindowFrameSegment;
import com.sea.odps.sql.core.segment.dml.window.WindowOrderBySegment;
import com.sea.odps.sql.core.segment.dml.window.WindowSegment;
import com.sea.odps.sql.core.segment.dml.window.WindowSpecificationSegment;
import com.sea.odps.sql.core.segment.generic.AliasSegment;
import com.sea.odps.sql.core.segment.generic.CommentSegment;
import com.sea.odps.sql.core.segment.generic.OwnerSegment;
import com.sea.odps.sql.core.segment.generic.column.ColumnSegment;
import com.sea.odps.sql.core.segment.generic.expr.BinaryOperationExpression;
import com.sea.odps.sql.core.segment.generic.expr.ExpressionSegment;
import com.sea.odps.sql.core.segment.generic.expr.NotExpression;
import com.sea.odps.sql.core.segment.generic.expr.complex.CommonTableExpressionSegment;
import com.sea.odps.sql.core.segment.generic.expr.simple.LiteralExpressionSegment;
import com.sea.odps.sql.core.segment.generic.expr.subquery.SubquerySegment;
import com.sea.odps.sql.core.segment.generic.hint.HintItemSegment;
import com.sea.odps.sql.core.segment.generic.hint.HintSegment;
import com.sea.odps.sql.core.segment.generic.table.JoinTableSegment;
import com.sea.odps.sql.core.segment.generic.table.SimpleTableSegment;
import com.sea.odps.sql.core.segment.generic.table.SubqueryTableSegment;
import com.sea.odps.sql.core.segment.generic.table.TableNameSegment;
import com.sea.odps.sql.core.segment.generic.table.TableSegment;
import com.sea.odps.sql.core.statement.AbstractSQLStatement;
import com.sea.odps.sql.core.statement.dml.SelectStatement;
import com.sea.odps.sql.core.util.LogUtil;
import com.sea.odps.sql.core.value.identifier.IdentifierValue;
import com.sea.odps.sql.visitor.core.ASTNode;
import com.sea.odps.sql.visitor.odps.OdpsSQLSelectStatement;

/**
 * ODPS 语句 AST 构建访问器，负责将 ANTLR 语法树转换为统一的 SQL 语义片段。
 *
 * <h3>SELECT 语句处理流程示例</h3>
 *
 * <h4>示例 1：基础 SELECT 语句</h4>
 *
 * <p>以 SQL 语句为例：
 *
 * <pre>{@code
 * SELECT DISTINCT t1.id, t2.name
 * FROM ods.table_a t1
 * LEFT JOIN ods.table_b t2 ON t1.id = t2.id
 * WHERE t1.status = 'active'
 * GROUP BY t1.id
 * ORDER BY t1.id
 * LIMIT 10 OFFSET 5
 * }</pre>
 *
 * <p>处理流程如下：
 *
 * <ol>
 *   <li><b>入口层</b>：visitScript() → visitStatement() → visitExecStatement() → visitQueryStatement()
 *       → visitSelectQueryStatement() → visitSelectQueryExpression()
 *   <li><b>SELECT 子句处理</b>：
 *       <ul>
 *         <li>visitSelectQueryExpression() 创建 OdpsSQLSelectStatement 对象
 *         <li>visitSelectClause() 检测 DISTINCT 关键字（ctx.dist）
 *         <li>visitSelectList(ctx, distinct=true) → 遍历 visitSelectItem()
 *         <li>对每个 SELECT 项：visitExpression() → visitLogicalExpression() → ... →
 *             visitTableOrColumnRef() → createColumnSegment()
 *         <li>创建 ColumnProjectionSegment 或 ExpressionProjectionSegment
 *         <li>组装为 ProjectionsSegment(distinct=true)
 *       </ul>
 *   <li><b>FROM/JOIN 子句处理</b>：
 *       <ul>
 *         <li>visitSelectRest() → visitFromClause() → visitJoinSource()
 *         <li>visitTableSource() → visitTableName() 创建 SimpleTableSegment
 *         <li>createJoinTableSegment() 处理 JOIN，构建 JoinTableSegment
 *       </ul>
 *   <li><b>WHERE/GROUP BY/HAVING 子句处理</b>：
 *       <ul>
 *         <li>visitPreSelectClauses() → visitWhereClause() / createGroupBySegment() /
 *             createHavingSegment()
 *         <li>WHERE：visitExpression() 递归解析条件表达式
 *         <li>GROUP BY：遍历 groupByKey，创建 ExpressionOrderByItemSegment 集合
 *       </ul>
 *   <li><b>ORDER BY/LIMIT 子句处理</b>：
 *       <ul>
 *         <li>visitPostSelectClauses() → createOrderBySegment() / visitLimitClause()
 *         <li>ORDER BY：遍历排序项，解析排序方向和 NULLS 处理方式
 *         <li>LIMIT：visitLimitClause() 解析 offset 和 rowCount
 *             <ul>
 *               <li>检测格式：LIMIT offset, rowCount 或 LIMIT rowCount [OFFSET offset]
 *               <li>createPaginationValueSegment() 从 MathExpressionContext 提取数字值
 *               <li>创建 NumberLiteralPaginationValueSegment 对象
 *               <li>组装为 LimitSegment(offset, rowCount)
 *             </ul>
 *       </ul>
 *   <li><b>组装结果</b>：将所有 Segment 设置到 OdpsSQLSelectStatement，返回完整的 AST
 * </ol>
 *
 * <h4>示例 2：集合操作（UNION/INTERSECT/EXCEPT）</h4>
 *
 * <p>以包含集合操作的 SQL 语句为例：
 *
 * <pre>{@code
 * SELECT id, name FROM table_a
 * UNION ALL
 * SELECT id, name FROM table_b
 * INTERSECT
 * SELECT id, name FROM table_c
 * ORDER BY id
 * LIMIT 100
 * }</pre>
 *
 * <p>处理流程如下：
 *
 * <ol>
 *   <li><b>入口层</b>：visitScript() → visitStatement() → visitExecStatement() → visitQueryStatement()
 *       → visitSelectQueryStatement() → visitQueryExpression()
 *   <li><b>左侧查询处理</b>：
 *       <ul>
 *         <li>visitQueryExpression() 检测到 ctx.s（简单查询表达式）
 *         <li>visitSimpleQueryExpression() → visitSelectQueryExpression()
 *         <li>解析第一个 SELECT 语句，得到 left SelectStatement
 *       </ul>
 *   <li><b>集合操作处理</b>：
 *       <ul>
 *         <li>遍历 ctx.rhs（SetRHSContext 列表）
 *         <li>对每个 rhsContext：
 *             <ul>
 *               <li>parseCombineType(rhsContext.setOperator()) 解析操作类型
 *                   <ul>
 *                     <li>检测 ctx.union/ctx.intersect/ctx.minus
 *                     <li>检测 ctx.all 判断是否为 ALL 类型
 *                     <li>返回
 *                         CombineType（UNION/UNION_ALL/INTERSECT/INTERSECT_ALL/EXCEPT/EXCEPT_ALL/MINUS/MINUS_ALL）
 *                   </ul>
 *               <li>visit(rhsContext.operand) 访问右侧操作数，得到 right SelectStatement
 *               <li>创建 CombineSegment(current, combineType, right)
 *               <li>current.setCombine(combineSegment) 设置到当前语句
 *               <li>更新 current = right，继续处理下一个集合操作
 *             </ul>
 *       </ul>
 *   <li><b>后续子句处理</b>：
 *       <ul>
 *         <li>ORDER BY 和 LIMIT 子句在最后一个 SELECT 语句中处理
 *         <li>处理方式与示例 1 相同
 *       </ul>
 *   <li><b>组装结果</b>：返回包含 CombineSegment 的 SelectStatement
 * </ol>
 *
 * <h4>示例 3：嵌套查询表达式</h4>
 *
 * <p>以包含括号的查询表达式为例：
 *
 * <pre>{@code
 * (SELECT id FROM table_a UNION SELECT id FROM table_b)
 * INTERSECT
 * SELECT id FROM table_c
 * }</pre>
 *
 * <p>处理流程：
 *
 * <ul>
 *   <li>visitQueryExpression() 检测到 ctx.q（括号内的查询表达式）
 *   <li>递归调用 visit(ctx.q) 处理括号内的查询
 *   <li>括号内的查询可能包含集合操作，递归处理
 *   <li>处理完括号内的查询后，继续处理外层的集合操作
 * </ul>
 *
 * <p><b>表达式解析优先级</b>：从高到低依次为：逻辑表达式 → 等值表达式 → 数学表达式 → 一元表达式 → 字段表达式 → 原子表达式
 *
 * <p><b>支持的功能</b>：
 *
 * <ul>
 *   <li>DISTINCT 关键字：在 ProjectionsSegment 中标记 distinct 标志
 *   <li>LIMIT 完整解析：支持 LIMIT offset, rowCount 和 LIMIT rowCount [OFFSET offset] 两种格式
 *   <li>集合操作：支持 UNION、UNION ALL、INTERSECT、INTERSECT ALL、EXCEPT、EXCEPT ALL、MINUS、MINUS ALL
 *   <li>嵌套查询表达式：支持括号内的查询表达式与集合操作的组合
 * </ul>
 */
public class OdpsAstBuilderVisitor extends OdpsParserBaseVisitor<ASTNode> {

  private static final Logger log = LoggerFactory.getLogger(OdpsAstBuilderVisitor.class);

  /** COMMENT token 的通道号（隐藏通道）。 从 OdpsLexer 中获取，COMMENT_CHANNEL 的值是 3。 */
  private static final int COMMENT_CHANNEL = 3;

  /** Token stream，用于访问隐藏通道中的注释 token。 */
  private TokenStream tokenStream;

  /** 窗口名称到窗口定义的映射表，用于解析窗口函数中的窗口引用。 */
  private final java.util.Map<String, WindowDefinitionSegment> windowDefinitions =
      new java.util.HashMap<>();

  /** 构造函数。 */
  public OdpsAstBuilderVisitor() {}

  /**
   * 设置 token stream，用于提取隐藏通道中的注释。
   *
   * @param tokenStream token stream
   */
  public void setTokenStream(final TokenStream tokenStream) {
    this.tokenStream = tokenStream;
  }

  /**
   * 访问脚本节点，处理用户代码块或语句。
   *
   * @param ctx 脚本上下文
   * @return AST 节点，如果存在用户代码块则访问用户代码块，否则访问语句
   */
  @Override
  public ASTNode visitScript(final OdpsParser.ScriptContext ctx) {
    if (null != ctx.userCodeBlock) {
      return visit(ctx.userCodeBlock);
    }
    if (null != ctx.statement) {
      return visit(ctx.statement);
    }
    return null;
  }

  /**
   * 访问语句节点，目前仅支持执行语句（execStatement）。
   *
   * @param ctx 语句上下文
   * @return AST 节点
   * @throws UnsupportedOperationException 如果是不支持的语句类型
   */
  @Override
  public ASTNode visitStatement(final OdpsParser.StatementContext ctx) {
    if (null != ctx.execStatement()) {
      return visitExecStatement(ctx.execStatement());
    }
    throw new UnsupportedOperationException(
        "unsupported statement, only exec statement is supported temporarily.");
  }

  /**
   * 访问执行语句节点，目前仅支持查询语句（queryStatement）。
   *
   * @param ctx 执行语句上下文
   * @return AST 节点
   * @throws UnsupportedOperationException 如果是不支持的执行语句类型
   */
  @Override
  public ASTNode visitExecStatement(final OdpsParser.ExecStatementContext ctx) {
    if (null != ctx.queryStatement()) {
      return visit(ctx.queryStatement());
    }
    throw new UnsupportedOperationException(
        "unsupported exec statement, only query statement is supported temporarily.");
  }

  /**
   * 访问查询语句节点，支持 SELECT 查询、FROM 语句、INSERT 语句和 WITH 子句。 支持 WITH CTE 子句的处理。
   *
   * @param ctx 查询语句上下文
   * @return AST 节点，如果是 SELECT 查询则返回解析结果，其他类型返回 null
   */
  @Override
  public ASTNode visitQueryStatement(final OdpsParser.QueryStatementContext ctx) {
    SelectStatement result = null;
    // 处理 WITH 子句（如果存在）
    if (null != ctx.withClause()) {
      WithSegment withSegment = (WithSegment) visit(ctx.withClause());
      if (null != ctx.selectQueryStatement()) {
        result = (SelectStatement) visit(ctx.selectQueryStatement());
        if (null != result && null != withSegment) {
          result.setWith(withSegment);
        }
        return result;
      }
    }
    if (null != ctx.selectQueryStatement()) {
      return visit(ctx.selectQueryStatement());
    }
    if (null != ctx.fromStatement()) {
      logUnsupportedFeature(ctx.fromStatement(), "FROM语句");
    }
    if (null != ctx.insertStatement()) {
      logUnsupportedFeature(ctx.insertStatement(), "INSERT语句");
    }
    return null;
  }

  /**
   * 访问 SELECT 查询语句节点，处理 SELECT 查询表达式或通用查询表达式。 支持集合操作（UNION、INTERSECT、EXCEPT）。
   *
   * @param ctx SELECT 查询语句上下文
   * @return AST 节点
   */
  @Override
  public ASTNode visitSelectQueryStatement(final OdpsParser.SelectQueryStatementContext ctx) {
    SelectStatement left = getLeftSelectStatement(ctx);
    if (left == null) {
      return null;
    }

    // 处理集合操作（如果有）
    if (ctx.rhs != null && !ctx.rhs.isEmpty()) {
      processSetOperations(left, ctx.rhs);
    }

    return left;
  }

  /**
   * 访问 SELECT 查询表达式节点，这是 SELECT 语句的核心处理方法。 创建 OdpsSQLSelectStatement 对象，并组装 SELECT
   * 子句和后续子句（FROM、WHERE、GROUP BY 等）。
   *
   * @param ctx SELECT 查询表达式上下文
   * @return OdpsSQLSelectStatement 对象，包含完整的 SELECT 语句语义信息
   */
  @Override
  public ASTNode visitSelectQueryExpression(final OdpsParser.SelectQueryExpressionContext ctx) {
    OdpsSQLSelectStatement selectStatement = new OdpsSQLSelectStatement();
    if (null != ctx.selectClause()) {
      // 处理 HINT 子句（在 SELECT 子句中）
      if (null != ctx.selectClause().hintClause()) {
        HintSegment hintSegment = (HintSegment) visit(ctx.selectClause().hintClause());
        if (null != hintSegment) {
          selectStatement.setHint(hintSegment);
        }
      }
      ProjectionsSegment projectionSegment = (ProjectionsSegment) visit(ctx.selectClause());
      selectStatement.setProjections(projectionSegment);
    }
    if (null != ctx.selectRest()) {
      SelectRestResult restResult = (SelectRestResult) visit(ctx.selectRest());
      if (null != restResult) {
        selectStatement.setFrom(restResult.getTableSegment());
        restResult.getWhereSegment().ifPresent(selectStatement::setWhere);
        restResult.getGroupBySegment().ifPresent(selectStatement::setGroupBy);
        restResult.getHavingSegment().ifPresent(selectStatement::setHaving);
        restResult.getOrderBySegment().ifPresent(selectStatement::setOrderBy);
        restResult.getLimitSegment().ifPresent(selectStatement::setLimit);
        restResult.getWindowSegment().ifPresent(selectStatement::setWindow);
        restResult.getLateralViewSegment().ifPresent(selectStatement::setLateralView);
      }
    }
    // 提取注释信息
    extractComments(ctx, selectStatement);
    return selectStatement;
  }

  // ------------------------------------ select clause ------------------------------------

  /**
   * 访问 SELECT 子句节点，处理 SELECT 列表或转换子句。 支持 DISTINCT 关键字的解析。
   *
   * @param ctx SELECT 子句上下文
   * @return ProjectionsSegment 对象，包含所有投影项和 DISTINCT 标志
   */
  @Override
  public ASTNode visitSelectClause(final OdpsParser.SelectClauseContext ctx) {
    if (ctx.selectList() != null) {
      boolean distinct = ctx.KW_DISTINCT() != null;
      return visitSelectList(ctx.selectList(), distinct);
    }
    if (ctx.selectTrfmClause() != null) {
      logUnsupportedFeature(ctx.selectTrfmClause(), "SELECT转换子句");
    }
    if (ctx.trfmClause() != null) {
      logUnsupportedFeature(ctx.trfmClause(), "转换子句");
    }
    return null;
  }

  /**
   * 访问 SELECT 列表节点，遍历所有 SELECT 项并组装为投影片段集合。
   *
   * @param ctx SELECT 列表上下文
   * @return ProjectionsSegment 对象，包含所有投影项的集合
   */
  @Override
  public ASTNode visitSelectList(final OdpsParser.SelectListContext ctx) {
    return visitSelectList(ctx, false);
  }

  /**
   * 访问 SELECT 列表节点，遍历所有 SELECT 项并组装为投影片段集合。
   *
   * @param ctx SELECT 列表上下文
   * @param distinct 是否包含 DISTINCT 关键字
   * @return ProjectionsSegment 对象，包含所有投影项的集合
   */
  private ASTNode visitSelectList(final OdpsParser.SelectListContext ctx, final boolean distinct) {
    Collection<ProjectionSegment> projections = new LinkedList<>();
    for (OdpsParser.SelectItemContext each : ctx.selectItem()) {
      ProjectionSegment projection = (ProjectionSegment) visit(each);
      if (projection != null) {
        projections.add(projection);
      }
    }
    return new ProjectionsSegment(getStartIndex(ctx), getStopIndex(ctx), projections, distinct);
  }

  /**
   * 访问 SELECT 项节点，处理单个 SELECT 项（列、表达式或表的所有列）。 支持别名设置，根据表达式类型创建 ColumnProjectionSegment 或
   * ExpressionProjectionSegment。
   *
   * @param ctx SELECT 项上下文
   * @return ProjectionSegment 对象，可能是 ColumnProjectionSegment 或 ExpressionProjectionSegment
   */
  @Override
  public ASTNode visitSelectItem(final OdpsParser.SelectItemContext ctx) {
    ProjectionSegment result = null;
    if (null != ctx.tableAllColumns()) {
      ColumnSegment columnSegment = createColumnSegment(ctx.tableAllColumns());
      ColumnProjectionSegment projectionSegment = new ColumnProjectionSegment(columnSegment);
      if (null != ctx.aliasIdentifier) {
        projectionSegment.setAlias((AliasSegment) visit(ctx.aliasIdentifier));
      }
      result = projectionSegment;
    } else if (null != ctx.expression()) {
      ExpressionSegment expressionSegment = (ExpressionSegment) visit(ctx.expression());
      if (expressionSegment instanceof ColumnSegment) {
        result = new ColumnProjectionSegment((ColumnSegment) expressionSegment);
      } else {
        result = new ExpressionProjectionSegment(expressionSegment);
      }
      if (null != ctx.aliasIdentifier) {
        AliasSegment aliasSegment = (AliasSegment) visit(ctx.aliasIdentifier);
        if (result instanceof ColumnProjectionSegment) {
          ((ColumnProjectionSegment) result).setAlias(aliasSegment);
        } else if (result instanceof ExpressionProjectionSegment) {
          ((ExpressionProjectionSegment) result).setAlias(aliasSegment);
        }
      }
    }
    // 提取字段附近的注释
    if (null != result) {
      CommentSegment comment = extractCommentNearNode(ctx);
      if (null != comment) {
        if (result instanceof ColumnProjectionSegment) {
          ((ColumnProjectionSegment) result).setComment(comment);
        } else if (result instanceof ExpressionProjectionSegment) {
          ((ExpressionProjectionSegment) result).setComment(comment);
        }
      }
    }
    return result;
  }

  // ------------------------------------ expression ------------------------------------

  /**
   * 访问表达式节点，表达式解析的入口方法，委托给逻辑表达式处理。
   *
   * @param ctx 表达式上下文
   * @return ExpressionSegment 对象
   */
  @Override
  public ASTNode visitExpression(final OdpsParser.ExpressionContext ctx) {
    return visit(ctx.logicalExpression());
  }

  /**
   * 访问逻辑表达式节点，处理 NOT 表达式或直接返回字面量表达式。
   *
   * @param ctx 逻辑表达式上下文
   * @return ExpressionSegment 对象
   */
  @Override
  public ASTNode visitLogicalExpression(final OdpsParser.LogicalExpressionContext ctx) {
    if (null != ctx.notExpression()) {
      return visit(ctx.notExpression());
    }
    return createLiteralExpression(ctx);
  }

  /**
   * 访问 NOT 表达式节点，处理逻辑非操作或继续向下解析等值表达式。
   *
   * @param ctx NOT 表达式上下文
   * @return ExpressionSegment 对象，如果是 NOT 操作则返回 NotExpression
   */
  @Override
  public ASTNode visitNotExpression(final OdpsParser.NotExpressionContext ctx) {
    if (ctx.notExpression() != null) {
      ExpressionSegment expression = (ExpressionSegment) visit(ctx.notExpression());
      return new NotExpression(getStartIndex(ctx), getStopIndex(ctx), expression);
    }
    if (ctx.equalExpression() != null) {
      return visit(ctx.equalExpression());
    }
    return createLiteralExpression(ctx);
  }

  /**
   * 访问等值表达式节点，处理比较操作符（=、!=、<>、>、<、>=、<= 等）。 如果存在左右操作数和操作符，则创建 BinaryOperationExpression。
   *
   * @param ctx 等值表达式上下文
   * @return ExpressionSegment 对象，如果是比较操作则返回 BinaryOperationExpression
   */
  @Override
  public ASTNode visitEqualExpression(final OdpsParser.EqualExpressionContext ctx) {
    if (ctx.mathExpression() != null) {
      return visit(ctx.mathExpression());
    }
    if (ctx.lhs != null && ctx.rhs != null && ctx.op != null) {
      ExpressionSegment left = (ExpressionSegment) visit(ctx.lhs);
      ExpressionSegment right = (ExpressionSegment) visit(ctx.rhs);
      String operator = ctx.op.getText();
      if (ctx.not != null) {
        operator = ctx.not.getText() + " " + operator;
      }
      String text = getOriginalText(ctx);
      return new BinaryOperationExpression(
          getStartIndex(ctx), getStopIndex(ctx), left, right, operator, text);
    }
    if (!ctx.equalExpression().isEmpty()) {
      return visit(ctx.equalExpression(0));
    }
    return createLiteralExpression(ctx);
  }

  /**
   * 访问数学表达式节点，处理算术操作符（+、-、*、/、% 等）。
   *
   * @param ctx 数学表达式上下文
   * @return ExpressionSegment 对象，如果是算术操作则返回 BinaryOperationExpression
   */
  @Override
  public ASTNode visitMathExpression(final OdpsParser.MathExpressionContext ctx) {
    if (ctx.unarySuffixExpression() != null) {
      return visit(ctx.unarySuffixExpression());
    }
    ExpressionSegment left = (ExpressionSegment) visit(ctx.lhs);
    ExpressionSegment right = (ExpressionSegment) visit(ctx.rhs);
    String operator = ctx.op.getText();
    String text = getOriginalText(ctx);
    return new BinaryOperationExpression(
        getStartIndex(ctx), getStopIndex(ctx), left, right, operator, text);
  }

  /**
   * 访问一元后缀表达式节点，处理后缀操作（如数组索引、方法调用等）。
   *
   * @param ctx 一元后缀表达式上下文
   * @return ExpressionSegment 对象
   */
  @Override
  public ASTNode visitUnarySuffixExpression(final OdpsParser.UnarySuffixExpressionContext ctx) {
    if (null != ctx.unarySuffixExpression()) {
      return visit(ctx.unarySuffixExpression());
    }
    if (null != ctx.unaryPrefixExpression()) {
      return visit(ctx.unaryPrefixExpression());
    }
    return createLiteralExpression(ctx);
  }

  /**
   * 访问一元前缀表达式节点，处理前缀操作（如负号、正号等）。
   *
   * @param ctx 一元前缀表达式上下文
   * @return ExpressionSegment 对象
   */
  @Override
  public ASTNode visitUnaryPrefixExpression(final OdpsParser.UnaryPrefixExpressionContext ctx) {
    if (null != ctx.unaryPrefixExpression()) {
      return visit(ctx.unaryPrefixExpression());
    }
    if (null != ctx.fieldExpression()) {
      return visit(ctx.fieldExpression());
    }
    return createLiteralExpression(ctx);
  }

  /**
   * 访问字段表达式节点，处理字段访问（如 table.column、对象成员访问等）。 支持嵌套字段访问，如 a.b.c。
   *
   * @param ctx 字段表达式上下文
   * @return ExpressionSegment 对象，如果是字段访问则返回 ColumnSegment
   */
  @Override
  public ASTNode visitFieldExpression(final OdpsParser.FieldExpressionContext ctx) {
    if (null != ctx.fieldExpression()) {
      ASTNode operand = visit(ctx.fieldExpression());
      if (null != ctx.member && operand instanceof ColumnSegment) {
        ColumnSegment base = (ColumnSegment) operand;
        String ownerExpression = base.getExpression();
        String memberIdentifier = ctx.member.field.getText();
        String raw =
            null == ownerExpression ? memberIdentifier : ownerExpression + "." + memberIdentifier;
        return createColumnSegment(ctx, raw);
      }
      if (null != ctx.member || null != ctx.method || null != ctx.index) {
        return createLiteralExpression(ctx);
      }
      return operand;
    }
    if (null != ctx.atomExpression()) {
      return visit(ctx.atomExpression());
    }
    return createLiteralExpression(ctx);
  }

  /**
   * 访问原子表达式节点，处理最基本的表达式单元（表/列引用、常量、函数调用等）。
   *
   * @param ctx 原子表达式上下文
   * @return ExpressionSegment 对象
   */
  @Override
  public ASTNode visitAtomExpression(final OdpsParser.AtomExpressionContext ctx) {
    if (null != ctx.tableOrColumnRef()) {
      return visit(ctx.tableOrColumnRef());
    }
    if (null != ctx.constant()) {
      return createLiteralExpression(ctx.constant());
    }
    if (null != ctx.function()) {
      return visitFunction(ctx.function());
    }
    return createLiteralExpression(ctx);
  }

  /**
   * 访问表或列引用节点，创建列片段。
   *
   * @param ctx 表或列引用上下文
   * @return ColumnSegment 对象
   */
  @Override
  public ASTNode visitTableOrColumnRef(final OdpsParser.TableOrColumnRefContext ctx) {
    return createColumnSegment(ctx);
  }

  /**
   * 访问表的所有列节点，支持两种格式： - SELECT *（单独的星号） - SELECT table.*（表名.星号）
   *
   * @param ctx 表的所有列上下文
   * @return ColumnSegment 对象，列名为 "*"，如果存在表名则设置 OwnerSegment
   */
  @Override
  public ASTNode visitTableAllColumns(final OdpsParser.TableAllColumnsContext ctx) {
    // 检查是否有表名（table.* 格式）
    if (ctx.table != null) {
      // 有表名的情况：table.*
      String tableName = ctx.table.getText();
      int starStart = ctx.STAR().getSymbol().getStartIndex();
      int starStop = ctx.STAR().getSymbol().getStopIndex();
      ColumnSegment columnSegment =
          new ColumnSegment(starStart, starStop, new IdentifierValue("*"));
      OwnerSegment ownerSegment =
          new OwnerSegment(
              ctx.table.getStart().getStartIndex(),
              ctx.table.getStop().getStopIndex(),
              new IdentifierValue(tableName));
      columnSegment.setOwner(ownerSegment);
      return columnSegment;
    } else if (ctx.STAR() != null) {
      // 单独的 * 情况：SELECT *
      Token starToken = ctx.STAR().getSymbol();
      return new ColumnSegment(
          starToken.getStartIndex(), starToken.getStopIndex(), new IdentifierValue("*"));
    }
    // 兜底：使用通用方法
    return createColumnSegment(ctx);
  }

  // ------------------------------------ table ------------------------------------

  /**
   * 访问表名节点，创建简单表片段。 支持三级命名：数据库.模式.表名，解析并设置 OwnerSegment。
   *
   * @param ctx 表名上下文
   * @return SimpleTableSegment 对象
   */
  @Override
  public ASTNode visitTableName(final OdpsParser.TableNameContext ctx) {
    SimpleTableSegment result =
        new SimpleTableSegment(
            new TableNameSegment(
                ctx.tab.getStart().getStartIndex(),
                ctx.tab.getStop().getStopIndex(),
                new IdentifierValue(ctx.tab.getText())));
    if (ctx.db != null) {
      result.setOwner(
          new OwnerSegment(
              ctx.db.getStart().getStartIndex(),
              ctx.db.getStop().getStopIndex(),
              new IdentifierValue(ctx.db.getText())));
    }
    if (ctx.sch != null) {
      OwnerSegment databaseOwner = result.getOwner().orElse(null);
      OwnerSegment schemaSegment =
          new OwnerSegment(
              ctx.sch.getStart().getStartIndex(),
              ctx.sch.getStop().getStopIndex(),
              new IdentifierValue(ctx.sch.getText()));
      schemaSegment.setOwner(databaseOwner);
      result.setOwner(schemaSegment);
    }
    // 注意：表注释在 visitTableSource 中提取（在别名设置之后），这里不提取
    return result;
  }

  /**
   * 访问标识符节点，创建标识符值对象。
   *
   * @param ctx 标识符上下文
   * @return IdentifierValue 对象
   */
  @Override
  public ASTNode visitIdentifier(final OdpsParser.IdentifierContext ctx) {
    return new IdentifierValue(ctx.getText());
  }

  /**
   * 访问别名标识符节点，创建别名片段。
   *
   * @param ctx 别名标识符上下文
   * @return AliasSegment 对象
   */
  @Override
  public ASTNode visitAliasIdentifier(final OdpsParser.AliasIdentifierContext ctx) {
    return new AliasSegment(
        getStartIndex(ctx), getStopIndex(ctx), new IdentifierValue(ctx.getText()));
  }

  /**
   * 访问表别名节点，创建别名片段。
   *
   * @param ctx 表别名上下文
   * @return AliasSegment 对象
   */
  @Override
  public ASTNode visitTableAlias(final OdpsParser.TableAliasContext ctx) {
    return new AliasSegment(
        getStartIndex(ctx), getStopIndex(ctx), new IdentifierValue(ctx.getText()));
  }

  /**
   * 访问 SELECT 后续子句节点，处理 FROM、WHERE、GROUP BY、HAVING、ORDER BY、LIMIT、WINDOW 等子句。 将这些子句组装为
   * SelectRestResult 对象返回。
   *
   * @param ctx SELECT 后续子句上下文
   * @return SelectRestResult 对象，包含所有后续子句的片段
   */
  @Override
  public ASTNode visitSelectRest(final OdpsParser.SelectRestContext ctx) {
    TableSegment tableSegment = null;
    WhereSegment whereSegment = null;
    GroupBySegment groupBySegment = null;
    HavingSegment havingSegment = null;
    OrderBySegment orderBySegment = null;
    LimitSegment limitSegment = null;
    WindowSegment windowSegment = null;
    LateralViewSegment lateralViewSegment = null;
    if (ctx.fromClause() != null) {
      tableSegment = (TableSegment) visit(ctx.fromClause());
      // 检查 joinSource 中是否包含 LATERAL VIEW
      lateralViewSegment = extractLateralViewFromJoinSource(ctx.fromClause());
    }
    // 处理 LATERAL VIEW（在 FROM 子句之后，selectRest 中的 lateralView）
    if (null != ctx.lateralView()) {
      lateralViewSegment = (LateralViewSegment) visit(ctx.lateralView());
    }
    if (null != ctx.preSelectClauses()) {
      PreSelectResult preSelectResult = (PreSelectResult) visit(ctx.preSelectClauses());
      if (null != preSelectResult) {
        whereSegment = preSelectResult.getWhereSegment().orElse(null);
        groupBySegment = preSelectResult.getGroupBySegment().orElse(null);
        havingSegment = preSelectResult.getHavingSegment().orElse(null);
        windowSegment = preSelectResult.getWindowSegment().orElse(null);
      }
    }
    if (null != ctx.postSelectClauses()) {
      PostSelectResult postSelectResult = (PostSelectResult) visit(ctx.postSelectClauses());
      if (null != postSelectResult) {
        orderBySegment = postSelectResult.getOrderBySegment().orElse(null);
        limitSegment = postSelectResult.getLimitSegment().orElse(null);
      }
    }
    if (null == tableSegment
        && null == whereSegment
        && null == groupBySegment
        && null == havingSegment
        && null == orderBySegment
        && null == limitSegment
        && null == windowSegment
        && null == lateralViewSegment) {
      return null;
    }
    return new SelectRestResult(
        tableSegment,
        whereSegment,
        groupBySegment,
        havingSegment,
        orderBySegment,
        limitSegment,
        windowSegment,
        lateralViewSegment);
  }

  /**
   * 从 FROM 子句的 joinSource 中提取 LATERAL VIEW。 遍历 joinSource 的所有 joinRHS，查找包含 LATERAL VIEW 的项。
   *
   * @param fromClause FROM 子句上下文
   * @return LATERAL VIEW 片段，如果不存在则返回 null
   */
  private LateralViewSegment extractLateralViewFromJoinSource(
      final OdpsParser.FromClauseContext fromClause) {
    if (fromClause == null || fromClause.joinSource() == null) {
      return null;
    }
    OdpsParser.JoinSourceContext joinSource = fromClause.joinSource();
    if (joinSource.rhs == null || joinSource.rhs.isEmpty()) {
      return null;
    }
    // 遍历所有 joinRHS，查找第一个包含 LATERAL VIEW 的项
    for (OdpsParser.JoinRHSContext rhsContext : joinSource.rhs) {
      if (rhsContext.lv != null) {
        return (LateralViewSegment) visit(rhsContext.lv);
      }
    }
    return null;
  }

  /**
   * 访问 SELECT 前置子句节点，处理 WHERE、GROUP BY、HAVING、WINDOW 子句。 这些子句在 SELECT 之后但在 ORDER BY 之前。
   *
   * @param ctx SELECT 前置子句上下文
   * @return PreSelectResult 对象，包含 WHERE、GROUP BY、HAVING、WINDOW 片段
   */
  @Override
  public ASTNode visitPreSelectClauses(final OdpsParser.PreSelectClausesContext ctx) {
    WhereSegment whereSegment = null;
    GroupBySegment groupBySegment = null;
    HavingSegment havingSegment = null;
    WindowSegment windowSegment = null;
    if (null != ctx.whereClause()) {
      whereSegment = (WhereSegment) visit(ctx.whereClause());
    }
    if (null != ctx.g) {
      groupBySegment = createGroupBySegment(ctx.g);
    }
    if (null != ctx.h) {
      havingSegment = createHavingSegment(ctx.h);
    }
    if (null != ctx.win) {
      windowSegment = (WindowSegment) visit(ctx.win);
    }
    return new PreSelectResult(whereSegment, groupBySegment, havingSegment, windowSegment);
  }

  /**
   * 访问 SELECT 后置子句节点，处理 ORDER BY、LIMIT 子句。 这些子句在 SELECT 语句的最后部分。
   *
   * @param ctx SELECT 后置子句上下文
   * @return PostSelectResult 对象，包含 ORDER BY、LIMIT 片段
   */
  @Override
  public ASTNode visitPostSelectClauses(final OdpsParser.PostSelectClausesContext ctx) {
    OrderBySegment orderBySegment = null;
    LimitSegment limitSegment = null;
    if (null != ctx.orderByClause()) {
      orderBySegment = createOrderBySegment(ctx.orderByClause());
    }
    if (null != ctx.limitClause()) {
      limitSegment = ((PostSelectResult) visit(ctx.limitClause())).getLimitSegment().orElse(null);
    }
    if (null != ctx.clusterByClause()) {
      logUnsupportedFeature(ctx.clusterByClause(), "CLUSTER BY子句");
    }
    if (null != ctx.distributeByClause()) {
      logUnsupportedFeature(ctx.distributeByClause(), "DISTRIBUTE BY子句");
    }
    if (null != ctx.sort) {
      logUnsupportedFeature(ctx.sort, "SORT子句");
    }
    return new PostSelectResult(orderBySegment, limitSegment);
  }

  /**
   * 访问 LIMIT 子句节点，创建限制片段。 支持两种格式： - LIMIT offset, rowCount - LIMIT rowCount [OFFSET offset]
   *
   * @param ctx LIMIT 子句上下文
   * @return PostSelectResult 对象，包含 LimitSegment
   */
  @Override
  public ASTNode visitLimitClause(final OdpsParser.LimitClauseContext ctx) {
    PaginationValueSegment offset = null;
    PaginationValueSegment rowCount = null;

    // 格式1: LIMIT offset, rowCount
    if (ctx.COMMA() != null) {
      if (ctx.offset != null && ctx.exp != null) {
        offset = createPaginationValueSegment(ctx.offset);
        rowCount = createPaginationValueSegment(ctx.exp);
      }
    } else if (ctx.exp != null) {
      // 格式2: LIMIT rowCount [OFFSET offset]
      rowCount = createPaginationValueSegment(ctx.exp);
      if (ctx.offset != null) {
        offset = createPaginationValueSegment(ctx.offset);
      }
    }

    LimitSegment limitSegment =
        new LimitSegment(getStartIndex(ctx), getStopIndex(ctx), offset, rowCount);
    // 提取 LIMIT 子句附近的注释
    CommentSegment comment = extractCommentNearNode(ctx);
    if (comment != null) {
      limitSegment.setComment(comment);
    }
    return new PostSelectResult(null, limitSegment);
  }

  /**
   * 访问 WHERE 子句节点，创建 WHERE 片段。
   *
   * @param ctx WHERE 子句上下文
   * @return WhereSegment 对象，包含条件表达式
   */
  @Override
  public ASTNode visitWhereClause(final OdpsParser.WhereClauseContext ctx) {
    ExpressionSegment expressionSegment = (ExpressionSegment) visit(ctx.expression());
    WhereSegment whereSegment =
        new WhereSegment(getStartIndex(ctx), getStopIndex(ctx), expressionSegment);
    // 提取 WHERE 子句附近的注释
    CommentSegment comment = extractCommentNearNode(ctx);
    if (comment != null) {
      whereSegment.setComment(comment);
    }
    return whereSegment;
  }

  /**
   * 访问 HAVING 子句节点，创建 HAVING 片段。
   *
   * @param ctx HAVING 子句上下文
   * @return HavingSegment 对象，包含条件表达式
   */
  @Override
  public ASTNode visitHavingClause(final OdpsParser.HavingClauseContext ctx) {
    return createHavingSegment(ctx);
  }

  /**
   * 访问 FROM 子句节点，处理连接源（JOIN 源）。
   *
   * @param ctx FROM 子句上下文
   * @return TableSegment 对象，可能是 SimpleTableSegment 或 JoinTableSegment
   */
  @Override
  public ASTNode visitFromClause(final OdpsParser.FromClauseContext ctx) {
    if (null != ctx.joinSource()) {
      return visit(ctx.joinSource());
    }
    return null;
  }

  /**
   * 访问 FROM 源节点，处理表源、子查询源或连接源。
   *
   * @param ctx FROM 源上下文
   * @return TableSegment 对象
   */
  @Override
  public ASTNode visitFromSource(final OdpsParser.FromSourceContext ctx) {
    if (null != ctx.tableSource()) {
      return visit(ctx.tableSource());
    }
    if (null != ctx.subQuerySource()) {
      return visit(ctx.subQuerySource());
    }
    if (null != ctx.joinSource()) {
      return visit(ctx.joinSource());
    }
    logUnsupportedFeature(ctx, "未识别的FROM源类型");
    return null;
  }

  /**
   * 访问连接源节点，处理多个表的连接操作。 从左到右依次处理每个连接，构建嵌套的 JoinTableSegment。 注意：如果 joinRHS 包含 LATERAL
   * VIEW，则跳过处理（LATERAL VIEW 在 visitSelectRest 中单独处理）。
   *
   * @param ctx 连接源上下文
   * @return TableSegment 对象，如果是多表连接则返回 JoinTableSegment
   */
  @Override
  public ASTNode visitJoinSource(final OdpsParser.JoinSourceContext ctx) {
    TableSegment current = (TableSegment) visit(ctx.lhs);
    if (ctx.rhs == null || ctx.rhs.isEmpty()) {
      return current;
    }

    // 处理 JOIN：从左到右依次处理，但需要考虑优先级
    // INNER JOIN 和 CROSS JOIN 的优先级高于 LEFT/RIGHT/FULL JOIN
    // 注意：LATERAL VIEW 在 joinRHS 中会被跳过，在 visitSelectRest 中单独处理
    for (OdpsParser.JoinRHSContext rhsContext : ctx.rhs) {
      // 跳过 LATERAL VIEW，它会在 visitSelectRest 中单独处理
      if (rhsContext.lv != null) {
        continue;
      }
      current = processJoinRHS(current, rhsContext);
    }
    return current;
  }

  /**
   * 处理单个 JOIN RHS，根据 JOIN 优先级决定是嵌套还是追加。
   *
   * @param current 当前的 TableSegment
   * @param rhsContext JOIN 右侧上下文
   * @return 处理后的 TableSegment
   */
  private TableSegment processJoinRHS(
      final TableSegment current, final OdpsParser.JoinRHSContext rhsContext) {
    TableSegment right = (TableSegment) visit(rhsContext.joinTable);
    if (right == null) {
      return current;
    }

    String newJoinType = normalizeJoinType(rhsContext.joinType);

    // 检查是否需要嵌套 JOIN（OUTER JOIN 后跟 INNER/CROSS JOIN）
    if (shouldNestJoin(current, newJoinType)) {
      return nestJoinInRight(current, right, newJoinType, rhsContext);
    }

    // 正常创建 JOIN
    return createJoinTableSegment(current, rhsContext);
  }

  /**
   * 判断是否需要将新的 JOIN 嵌套在当前 JOIN 的右侧。 规则：如果当前是 OUTER JOIN，新的是 INNER/CROSS JOIN，且右侧是简单表，则需要嵌套。
   *
   * @param current 当前的 TableSegment
   * @param newJoinType 新的 JOIN 类型
   * @return 如果需要嵌套则返回 true
   */
  private boolean shouldNestJoin(final TableSegment current, final String newJoinType) {
    if (!(current instanceof JoinTableSegment)) {
      return false;
    }
    JoinTableSegment currentJoin = (JoinTableSegment) current;
    return isOuterJoin(currentJoin.getJoinType())
        && isInnerOrCrossJoin(newJoinType)
        && currentJoin.getRight() instanceof SimpleTableSegment;
  }

  /**
   * 将新的 JOIN 嵌套在当前 JOIN 的右侧。
   *
   * @param current 当前的 TableSegment（必须是 JoinTableSegment）
   * @param right 右侧表片段
   * @param newJoinType 新的 JOIN 类型
   * @param rhsContext JOIN 右侧上下文
   * @return 更新后的 TableSegment
   */
  private TableSegment nestJoinInRight(
      final TableSegment current,
      final TableSegment right,
      final String newJoinType,
      final OdpsParser.JoinRHSContext rhsContext) {
    JoinTableSegment currentJoin = (JoinTableSegment) current;
    JoinTableSegment nestedJoin =
        createNestedJoin(currentJoin.getRight(), right, newJoinType, rhsContext);
    currentJoin.setRight(nestedJoin);
    currentJoin.setStopIndex(nestedJoin.getStopIndex());
    return current;
  }

  /**
   * 创建嵌套的 JOIN 片段。
   *
   * @param left 左侧表片段
   * @param right 右侧表片段
   * @param joinType 连接类型
   * @param ctx JOIN 右侧上下文
   * @return JoinTableSegment 对象
   */
  private JoinTableSegment createNestedJoin(
      final TableSegment left,
      final TableSegment right,
      final String joinType,
      final OdpsParser.JoinRHSContext ctx) {
    JoinTableSegment nestedJoin = new JoinTableSegment();
    nestedJoin.setLeft(left);
    nestedJoin.setRight(right);
    nestedJoin.setStartIndex(left.getStartIndex());
    nestedJoin.setStopIndex(right.getStopIndex());
    nestedJoin.setJoinType(joinType);
    nestedJoin.setNatural(ctx.KW_NATURAL() != null);
    if (ctx.joinOn != null && !ctx.joinOn.isEmpty()) {
      nestedJoin.setCondition(buildJoinCondition(ctx.joinOn));
    }
    if (ctx.commonCols != null && !ctx.commonCols.isEmpty()) {
      List<ColumnSegment> usingColumns =
          ctx.commonCols.stream().map(this::createColumnSegment).collect(Collectors.toList());
      nestedJoin.setUsing(usingColumns);
    }
    return nestedJoin;
  }

  /**
   * 创建连接表片段，处理单个 JOIN 操作（LEFT JOIN、RIGHT JOIN、INNER JOIN 等）。 支持 ON 条件和 USING 子句。 注意：LATERAL VIEW
   * 不应该作为 JOIN 的右侧，但语法中可能包含这种情况。
   *
   * @param left 左侧表片段
   * @param ctx JOIN 右侧上下文
   * @return JoinTableSegment 对象，包含连接类型、条件和 USING 列
   */
  private TableSegment createJoinTableSegment(
      final TableSegment left, final OdpsParser.JoinRHSContext ctx) {
    // 处理 LATERAL VIEW 情况：LATERAL VIEW 不应该作为 JOIN 的右侧
    if (ctx.lv != null) {
      logUnsupportedFeature(ctx, "LATERAL VIEW 作为 JOIN 的右侧不被支持");
      return left;
    }

    // 检查 joinTable 是否为 null
    if (ctx.joinTable == null) {
      logUnsupportedFeature(ctx, "JOIN 右侧表为空");
      return left;
    }

    TableSegment right = (TableSegment) visit(ctx.joinTable);
    if (right == null) {
      logUnsupportedFeature(ctx, "JOIN 右侧表解析失败");
      return left;
    }

    return createNestedJoin(left, right, normalizeJoinType(ctx.joinType), ctx);
  }

  /**
   * 构建 JOIN 条件表达式，处理单个或多个 ON 条件。 如果多个条件，使用 "AND" 连接。
   *
   * @param joinOnContexts JOIN ON 条件上下文列表
   * @return ExpressionSegment 对象
   */
  private ExpressionSegment buildJoinCondition(
      final List<OdpsParser.ExpressionContext> joinOnContexts) {
    if (joinOnContexts == null || joinOnContexts.isEmpty()) {
      return null;
    }
    if (joinOnContexts.size() == 1) {
      return (ExpressionSegment) visit(joinOnContexts.get(0));
    }
    String rawText =
        joinOnContexts.stream().map(this::getOriginalText).collect(Collectors.joining(" AND "));
    ParserRuleContext first = joinOnContexts.get(0);
    ParserRuleContext last = joinOnContexts.get(joinOnContexts.size() - 1);
    return new LiteralExpressionSegment(
        first.getStart().getStartIndex(), last.getStop().getStopIndex(), rawText);
  }

  /**
   * 规范化连接类型，将连接类型转换为大写并标准化空白字符。
   *
   * @param ctx 连接类型上下文
   * @return 规范化后的连接类型字符串，如果为空则返回 "JOIN"
   */
  private String normalizeJoinType(final OdpsParser.JoinTokenContext ctx) {
    if (null == ctx) {
      return "JOIN";
    }
    String text = ctx.getText();
    if (text == null || text.isEmpty()) {
      return "JOIN";
    }
    return text.replaceAll("\\s+", " ").toUpperCase();
  }

  /**
   * 判断连接类型是否为 INNER 或 CROSS JOIN。
   *
   * @param joinType 连接类型字符串
   * @return 如果是 INNER 或 CROSS JOIN 则返回 true
   */
  private boolean isInnerOrCrossJoin(final String joinType) {
    if (joinType == null) {
      return false;
    }
    String upper = joinType.toUpperCase();
    return upper.contains("INNER") || upper.contains("CROSS");
  }

  /**
   * 判断连接类型是否为 OUTER JOIN（LEFT、RIGHT、FULL）。
   *
   * @param joinType 连接类型字符串
   * @return 如果是 OUTER JOIN 则返回 true
   */
  private boolean isOuterJoin(final String joinType) {
    if (joinType == null) {
      return false;
    }
    String upper = joinType.toUpperCase();
    return upper.contains("LEFT") || upper.contains("RIGHT") || upper.contains("FULL");
  }

  /**
   * 访问子查询源节点，处理 FROM 子句中的子查询。 支持子查询别名设置。
   *
   * @param ctx 子查询源上下文
   * @return SubqueryTableSegment 对象
   */
  @Override
  public ASTNode visitSubQuerySource(final OdpsParser.SubQuerySourceContext ctx) {
    SubquerySegment subquerySegment = null;
    if (ctx.subQueryExpression() != null) {
      SelectStatement selectStatement = (SelectStatement) visit(ctx.subQueryExpression());
      subquerySegment = new SubquerySegment(getStartIndex(ctx), getStopIndex(ctx), selectStatement);
    }
    if (subquerySegment == null) {
      return null;
    }
    SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(subquerySegment);
    if (null != ctx.tableAliasWithCols()) {
      OdpsParser.TableAliasWithColsContext aliasWithColsContext = ctx.tableAliasWithCols();
      if (null != aliasWithColsContext.table) {
        AliasSegment aliasSegment = createAliasSegment(aliasWithColsContext.table);
        subqueryTableSegment.setAlias(aliasSegment);
      }
    }
    return subqueryTableSegment;
  }

  /**
   * 访问子查询表达式节点，处理子查询语句。
   *
   * @param ctx 子查询表达式上下文
   * @return SelectStatement 对象
   */
  @Override
  public ASTNode visitSubQueryExpression(final OdpsParser.SubQueryExpressionContext ctx) {
    if (null != ctx.queryExpression()) {
      return visit(ctx.queryExpression());
    }
    return null;
  }

  /**
   * 访问表源节点，处理单个表及其别名。
   *
   * @param ctx 表源上下文
   * @return SimpleTableSegment 对象，如果存在别名则设置别名
   */
  @Override
  public ASTNode visitTableSource(final OdpsParser.TableSourceContext ctx) {
    SimpleTableSegment tableSegment = null;
    if (null != ctx.tableName()) {
      tableSegment = (SimpleTableSegment) visit(ctx.tableName());
    }
    if (null != ctx.alias && null != tableSegment) {
      tableSegment.setAlias((AliasSegment) visit(ctx.alias));
    }
    // 在表源级别提取注释（在别名设置之后），这样可以获取表名和别名之后的注释
    if (null != tableSegment) {
      CommentSegment comment = extractCommentNearNode(ctx);
      if (null != comment) {
        tableSegment.setComment(comment);
      }
    }
    return tableSegment;
  }

  /**
   * 访问非 SQL11 标识符节点，创建别名片段。
   *
   * @param ctx 非 SQL11 标识符上下文
   * @return AliasSegment 对象
   */
  @Override
  public ASTNode visitIdentifierWithoutSql11(final OdpsParser.IdentifierWithoutSql11Context ctx) {
    return new AliasSegment(
        getStartIndex(ctx), getStopIndex(ctx), new IdentifierValue(ctx.getText()));
  }

  // ------------------------------------ helper ------------------------------------

  /**
   * 获取上下文的起始索引。
   *
   * @param ctx 解析规则上下文
   * @return 起始索引
   */
  private int getStartIndex(final ParserRuleContext ctx) {
    return ctx.getStart().getStartIndex();
  }

  /**
   * 获取上下文的结束索引。
   *
   * @param ctx 解析规则上下文
   * @return 结束索引
   */
  private int getStopIndex(final ParserRuleContext ctx) {
    return ctx.getStop().getStopIndex();
  }

  /**
   * 获取原始文本，从输入流中提取指定上下文的原始 SQL 文本。
   *
   * @param ctx 解析规则上下文
   * @return 原始文本字符串
   */
  private String getOriginalText(final ParserRuleContext ctx) {
    Interval interval = new Interval(getStartIndex(ctx), getStopIndex(ctx));
    return ctx.getStart().getInputStream().getText(interval);
  }

  /**
   * 创建字面量表达式片段，用于处理无法进一步解析的表达式（如函数调用、复杂表达式等）。
   *
   * @param ctx 解析规则上下文
   * @return LiteralExpressionSegment 对象，包含原始文本
   */
  private LiteralExpressionSegment createLiteralExpression(final ParserRuleContext ctx) {
    return new LiteralExpressionSegment(
        getStartIndex(ctx), getStopIndex(ctx), getOriginalText(ctx));
  }

  /**
   * 创建别名片段。
   *
   * @param ctx 解析规则上下文，如果为 null 则返回 null
   * @return AliasSegment 对象，如果上下文为 null 则返回 null
   */
  private AliasSegment createAliasSegment(final ParserRuleContext ctx) {
    if (ctx == null) {
      return null;
    }
    return new AliasSegment(
        getStartIndex(ctx), getStopIndex(ctx), new IdentifierValue(ctx.getText()));
  }

  /**
   * 创建列片段，从上下文中提取原始文本并创建列片段。
   *
   * @param ctx 解析规则上下文
   * @return ColumnSegment 对象
   */
  private ColumnSegment createColumnSegment(final ParserRuleContext ctx) {
    return createColumnSegment(ctx, getOriginalText(ctx));
  }

  /**
   * 创建列片段，解析列名和所有者（如 table.column）。 支持处理带引号的标识符，正确识别最后一个未引用的点作为分隔符。
   *
   * @param ctx 解析规则上下文
   * @param rawText 原始文本
   * @return ColumnSegment 对象，如果存在所有者则设置 OwnerSegment
   */
  private ColumnSegment createColumnSegment(final ParserRuleContext ctx, final String rawText) {
    String text = rawText.trim();
    String ownerText = null;
    String columnText = text;
    int lastDot = findLastUnquotedDot(text);
    if (lastDot > -1) {
      ownerText = text.substring(0, lastDot);
      columnText = text.substring(lastDot + 1);
    }
    ColumnSegment result =
        new ColumnSegment(getStartIndex(ctx), getStopIndex(ctx), new IdentifierValue(columnText));
    if (ownerText != null && !ownerText.isEmpty()) {
      int ownerStart = getStartIndex(ctx);
      OwnerSegment ownerSegment =
          new OwnerSegment(
              ownerStart, ownerStart + ownerText.length() - 1, new IdentifierValue(ownerText));
      result.setOwner(ownerSegment);
    }
    return result;
  }

  /**
   * 查找最后一个未引用的点（.），用于分离列的所有者和列名。 正确处理单引号、双引号和反引号。
   *
   * @param text 文本字符串
   * @return 最后一个未引用点的索引，如果不存在则返回 -1
   */
  private int findLastUnquotedDot(final String text) {
    boolean inQuote = false;
    char quoteChar = 0;
    for (int i = text.length() - 1; i >= 0; i--) {
      char current = text.charAt(i);
      if ('\'' == current || '"' == current || '`' == current) {
        if (!inQuote) {
          inQuote = true;
          quoteChar = current;
          continue;
        }
        if (quoteChar == current) {
          inQuote = false;
          continue;
        }
      }
      if ('.' == current && !inQuote) {
        return i;
      }
    }
    return -1;
  }

  /**
   * 创建分页值片段，从数学表达式中提取数字值或表达式。 如果表达式是数字字面量，则创建 NumberLiteralPaginationValueSegment。 否则创建
   * ExpressionPaginationValueSegment 存储表达式。
   *
   * @param ctx 数学表达式上下文
   * @return PaginationValueSegment 对象，如果无法解析则返回 null
   */
  private PaginationValueSegment createPaginationValueSegment(
      final OdpsParser.MathExpressionContext ctx) {
    if (ctx == null) {
      return null;
    }
    String text = getOriginalText(ctx).trim();
    try {
      long value = Long.parseLong(text);
      return new NumberLiteralPaginationValueSegment(getStartIndex(ctx), getStopIndex(ctx), value);
    } catch (NumberFormatException e) {
      // 如果不是简单的数字，解析为表达式
      if (log.isDebugEnabled()) {
        log.debug("LIMIT 值不是简单数字，解析为表达式: {}", text);
      }
      ExpressionSegment expression = (ExpressionSegment) visitMathExpression(ctx);
      if (expression != null) {
        return new ExpressionPaginationValueSegment(
            getStartIndex(ctx), getStopIndex(ctx), expression);
      }
      return null;
    }
  }

  /**
   * 访问查询表达式节点，处理可能包含集合操作的查询表达式。 支持 UNION、INTERSECT、EXCEPT 等集合操作。
   *
   * @param ctx 查询表达式上下文
   * @return SelectStatement 对象，如果包含集合操作则设置 CombineSegment
   */
  @Override
  public ASTNode visitQueryExpression(final OdpsParser.QueryExpressionContext ctx) {
    SelectStatement left = getLeftSelectStatement(ctx);
    if (left == null) {
      return null;
    }

    // 处理集合操作（如果有）
    if (ctx.rhs != null && !ctx.rhs.isEmpty()) {
      processSetOperations(left, ctx.rhs);
    }

    return left;
  }

  /**
   * 获取左侧的 SELECT 语句。
   *
   * @param ctx 查询表达式上下文或 SELECT 查询语句上下文
   * @return SelectStatement 对象，如果无法获取则返回 null
   */
  private SelectStatement getLeftSelectStatement(final ParserRuleContext ctx) {
    if (ctx instanceof OdpsParser.QueryExpressionContext) {
      OdpsParser.QueryExpressionContext qeCtx = (OdpsParser.QueryExpressionContext) ctx;
      if (qeCtx.q != null) {
        return (SelectStatement) visit(qeCtx.q);
      }
      if (qeCtx.s != null) {
        return (SelectStatement) visit(qeCtx.s);
      }
    } else if (ctx instanceof OdpsParser.SelectQueryStatementContext) {
      OdpsParser.SelectQueryStatementContext sqsCtx = (OdpsParser.SelectQueryStatementContext) ctx;
      if (sqsCtx.q != null) {
        return (SelectStatement) visit(sqsCtx.q);
      }
      if (sqsCtx.s != null) {
        return (SelectStatement) visit(sqsCtx.s);
      }
    }
    return null;
  }

  /**
   * 处理集合操作。
   *
   * @param left 左侧 SELECT 语句
   * @param rhsList 集合操作右侧列表
   */
  private void processSetOperations(
      final SelectStatement left, final List<OdpsParser.SetRHSContext> rhsList) {
    SelectStatement current = left;
    for (OdpsParser.SetRHSContext rhsContext : rhsList) {
      if (rhsContext.setOperator() == null || rhsContext.operand == null) {
        continue;
      }
      CombineType combineType = parseCombineType(rhsContext.setOperator());
      SelectStatement right = (SelectStatement) visit(rhsContext.operand);
      if (right == null) {
        continue;
      }
      CombineSegment combineSegment =
          new CombineSegment(
              rhsContext.getStart().getStartIndex(),
              rhsContext.getStop().getStopIndex(),
              current,
              combineType,
              right);
      current.setCombine(combineSegment);
      current = right;
    }
  }

  /**
   * 访问简单查询表达式节点，处理 SELECT 查询表达式或 FROM 查询表达式。
   *
   * @param ctx 简单查询表达式上下文
   * @return SelectStatement 对象
   */
  @Override
  public ASTNode visitSimpleQueryExpression(final OdpsParser.SimpleQueryExpressionContext ctx) {
    if (null != ctx.s) {
      return visit(ctx.s);
    }
    if (null != ctx.f) {
      return visit(ctx.f);
    }
    return null;
  }

  /**
   * 访问集合操作右侧节点，处理集合操作符和操作数。 注意：此方法主要用于访问操作数，实际的 CombineSegment 创建在 visitQueryExpression 中完成。
   *
   * @param ctx 集合操作右侧上下文
   * @return 操作数对应的 SelectStatement 对象
   */
  @Override
  public ASTNode visitSetRHS(final OdpsParser.SetRHSContext ctx) {
    if (null == ctx.operand) {
      return null;
    }
    // 直接访问操作数，返回 SelectStatement
    return visit(ctx.operand);
  }

  /**
   * 访问集合操作符节点，解析操作类型（UNION、INTERSECT、EXCEPT）和是否包含 ALL。
   *
   * @param ctx 集合操作符上下文
   * @return CombineType 枚举值
   */
  @Override
  public ASTNode visitSetOperator(final OdpsParser.SetOperatorContext ctx) {
    // 这个方法主要用于解析，实际类型在 visitSetRHS 中使用
    return null;
  }

  /**
   * 解析集合操作类型。
   *
   * @param ctx 集合操作符上下文
   * @return CombineType 枚举值
   */
  private CombineType parseCombineType(final OdpsParser.SetOperatorContext ctx) {
    if (ctx == null) {
      return CombineType.UNION;
    }

    boolean isAll = ctx.KW_ALL() != null;

    if (ctx.KW_UNION() != null) {
      return isAll ? CombineType.UNION_ALL : CombineType.UNION;
    }
    if (ctx.KW_INTERSECT() != null) {
      return isAll ? CombineType.INTERSECT_ALL : CombineType.INTERSECT;
    }
    if (ctx.KW_MINUS() != null || ctx.KW_EXCEPT() != null) {
      // 检查是 EXCEPT 还是 MINUS
      if (ctx.KW_EXCEPT() != null) {
        return isAll ? CombineType.EXCEPT_ALL : CombineType.EXCEPT;
      }
      // 默认使用 MINUS
      return isAll ? CombineType.MINUS_ALL : CombineType.MINUS;
    }
    // 默认返回 UNION
    return CombineType.UNION;
  }

  /**
   * 创建 HAVING 片段。
   *
   * @param ctx HAVING 子句上下文
   * @return HavingSegment 对象，如果上下文或条件为空则返回 null
   */
  private HavingSegment createHavingSegment(final OdpsParser.HavingClauseContext ctx) {
    if (ctx == null || ctx.havingCondition() == null) {
      return null;
    }
    ExpressionSegment expressionSegment =
        (ExpressionSegment) visit(ctx.havingCondition().expression());
    HavingSegment havingSegment =
        new HavingSegment(getStartIndex(ctx), getStopIndex(ctx), expressionSegment);
    CommentSegment comment = extractCommentNearNode(ctx);
    if (comment != null) {
      havingSegment.setComment(comment);
    }
    return havingSegment;
  }

  /**
   * 创建 GROUP BY 片段，遍历所有分组键并创建 OrderByItemSegment 集合。
   *
   * @param ctx GROUP BY 子句上下文
   * @return GroupBySegment 对象，如果上下文为空或没有分组项则返回 null
   */
  private GroupBySegment createGroupBySegment(final OdpsParser.GroupByClauseContext ctx) {
    if (ctx == null) {
      return null;
    }
    Collection<OrderByItemSegment> items = new LinkedList<>();
    for (OdpsParser.GroupByKeyContext keyContext : ctx.groupByKey()) {
      OrderByItemSegment item = createGroupByItem(keyContext);
      if (item != null) {
        items.add(item);
      }
    }
    if (items.isEmpty()) {
      return null;
    }
    GroupBySegment groupBySegment =
        new GroupBySegment(getStartIndex(ctx), getStopIndex(ctx), items);
    CommentSegment comment = extractCommentNearNode(ctx);
    if (comment != null) {
      groupBySegment.setComment(comment);
    }
    return groupBySegment;
  }

  /**
   * 创建 GROUP BY 项，将分组键转换为 OrderByItemSegment。 GROUP BY 项默认使用 ASC 排序方向。
   *
   * @param ctx GROUP BY 键上下文
   * @return OrderByItemSegment 对象，如果上下文为空则返回 null
   */
  private OrderByItemSegment createGroupByItem(final OdpsParser.GroupByKeyContext ctx) {
    if (ctx == null) {
      return null;
    }
    ExpressionSegment expressionSegment =
        ctx.exp != null
            ? (ExpressionSegment) visit(ctx.exp)
            : new LiteralExpressionSegment(
                getStartIndex(ctx), getStopIndex(ctx), getOriginalText(ctx));
    return new ExpressionOrderByItemSegment(
        getStartIndex(ctx), getStopIndex(ctx), expressionSegment, OrderDirection.ASC, null);
  }

  /**
   * 创建 ORDER BY 片段，遍历所有排序项并创建 OrderByItemSegment 集合。
   *
   * @param ctx ORDER BY 子句上下文
   * @return OrderBySegment 对象，如果上下文为空或没有排序项则返回 null
   */
  private OrderBySegment createOrderBySegment(final OdpsParser.OrderByClauseContext ctx) {
    if (ctx == null) {
      return null;
    }
    Collection<OrderByItemSegment> items = new LinkedList<>();
    for (OdpsParser.ColumnRefOrderContext each : ctx.exp) {
      OrderByItemSegment item = createOrderByItem(each);
      if (item != null) {
        items.add(item);
      }
    }
    if (items.isEmpty()) {
      return null;
    }
    OrderBySegment orderBySegment =
        new OrderBySegment(getStartIndex(ctx), getStopIndex(ctx), items);
    CommentSegment comment = extractCommentNearNode(ctx);
    if (comment != null) {
      orderBySegment.setComment(comment);
    }
    return orderBySegment;
  }

  /**
   * 创建 ORDER BY 项，解析排序表达式、排序方向（ASC/DESC）和 NULLS 处理方式（FIRST/LAST）。
   *
   * @param ctx 列引用排序上下文
   * @return OrderByItemSegment 对象，如果上下文为空则返回 null
   */
  private OrderByItemSegment createOrderByItem(final OdpsParser.ColumnRefOrderContext ctx) {
    if (ctx == null) {
      return null;
    }
    ExpressionSegment expressionSegment = (ExpressionSegment) visit(ctx.expression());
    OrderDirection direction = ctx.KW_DESC() != null ? OrderDirection.DESC : OrderDirection.ASC;
    NullsOrderType nullsOrderType =
        ctx.KW_FIRST() != null
            ? NullsOrderType.FIRST
            : (ctx.KW_LAST() != null ? NullsOrderType.LAST : null);
    return new ExpressionOrderByItemSegment(
        getStartIndex(ctx), getStopIndex(ctx), expressionSegment, direction, nullsOrderType);
  }

  // ------------------------------------ WITH CTE ------------------------------------

  /**
   * 访问 WITH 子句节点，处理公共表表达式（CTE）。
   *
   * @param ctx WITH 子句上下文
   * @return WithSegment 对象，包含所有 CTE 定义
   */
  @Override
  public ASTNode visitWithClause(final OdpsParser.WithClauseContext ctx) {
    Collection<CommonTableExpressionSegment> cteSegments = new LinkedList<>();
    if (null != ctx.branches && !ctx.branches.isEmpty()) {
      for (OdpsParser.CteStatementContext cteContext : ctx.branches) {
        CommonTableExpressionSegment cteSegment = (CommonTableExpressionSegment) visit(cteContext);
        if (null != cteSegment) {
          cteSegments.add(cteSegment);
        }
      }
    }
    WithSegment withSegment = new WithSegment(getStartIndex(ctx), getStopIndex(ctx), cteSegments);
    // 提取 WITH 子句附近的注释
    CommentSegment comment = extractCommentNearNode(ctx);
    if (comment != null) {
      withSegment.setComment(comment);
    }
    return withSegment;
  }

  /**
   * 访问 CTE 语句节点，处理单个公共表表达式定义。
   *
   * @param ctx CTE 语句上下文
   * @return CommonTableExpressionSegment 对象
   */
  @Override
  public ASTNode visitCteStatement(final OdpsParser.CteStatementContext ctx) {
    if (null == ctx.id) {
      return null;
    }
    IdentifierValue identifier = new IdentifierValue(ctx.id.getText());
    SubquerySegment subquery = null;

    // 处理 CTE 的查询表达式
    if (ctx.queryExp != null) {
      SelectStatement selectStatement = (SelectStatement) visit(ctx.queryExp);
      if (selectStatement != null) {
        subquery =
            new SubquerySegment(
                getStartIndex(ctx.queryExp), getStopIndex(ctx.queryExp), selectStatement);
      }
    } else if (ctx.exp != null) {
      // 如果 CTE 是表达式而不是查询，记录日志
      // 注意：这里可能需要根据实际需求调整，因为 CTE 通常应该是查询
      log.debug("CTE 使用表达式而非查询: {}", getOriginalText(ctx.exp));
    }

    // 处理列定义（如果存在）
    if (null != ctx.cols) {
      // 从 columnNameCommentList 中提取列名
      // 注意：这里需要根据实际的 ColumnNameCommentListContext 结构来调整
      // 当前简化处理，仅记录日志
      log.debug("CTE 列定义: {}", getOriginalText(ctx.cols));
    }

    if (null == subquery) {
      return null;
    }

    CommonTableExpressionSegment cteSegment =
        new CommonTableExpressionSegment(
            getStartIndex(ctx), getStopIndex(ctx), identifier, subquery);
    // 添加列定义（如果存在）
    // 注意：columns 字段在 CommonTableExpressionSegment 中是可变的集合
    // 可以通过 getColumns() 方法添加
    return cteSegment;
  }

  // ------------------------------------ HINT ------------------------------------

  /**
   * 访问 HINT 子句节点，处理优化提示。
   *
   * @param ctx HINT 子句上下文
   * @return HintSegment 对象，包含所有 HINT 项
   */
  @Override
  public ASTNode visitHintClause(final OdpsParser.HintClauseContext ctx) {
    if (null == ctx.hintList()) {
      return null;
    }
    // 直接访问 hintList 中的 hintItem，构建 HintSegment
    Collection<HintItemSegment> hintItems = new LinkedList<>();
    if (null != ctx.hintList().hintItem() && !ctx.hintList().hintItem().isEmpty()) {
      for (OdpsParser.HintItemContext hintItemContext : ctx.hintList().hintItem()) {
        ASTNode itemResult = visit(hintItemContext);
        if (itemResult instanceof HintItemSegment) {
          hintItems.add((HintItemSegment) itemResult);
        }
      }
    }
    if (hintItems.isEmpty()) {
      return null;
    }
    HintSegment hintSegment = new HintSegment(getStartIndex(ctx), getStopIndex(ctx), hintItems);
    // 提取 HINT 子句附近的注释
    CommentSegment comment = extractCommentNearNode(ctx);
    if (comment != null) {
      hintSegment.setComment(comment);
    }
    return hintSegment;
  }

  /**
   * 访问 HINT 列表节点，处理多个 HINT 项。 注意：此方法由 visitHintClause 直接处理，这里返回 null 或简单的占位符。
   *
   * @param ctx HINT 列表上下文
   * @return null（实际处理在 visitHintClause 中完成）
   */
  @Override
  public ASTNode visitHintList(final OdpsParser.HintListContext ctx) {
    // 这个方法通常不会被直接调用，因为 visitHintClause 会直接处理 hintList
    // 但为了完整性，我们仍然实现它
    Collection<HintItemSegment> hintItems = new LinkedList<>();
    if (null != ctx.hintItem() && !ctx.hintItem().isEmpty()) {
      for (OdpsParser.HintItemContext hintItemContext : ctx.hintItem()) {
        ASTNode result = visit(hintItemContext);
        if (result instanceof HintItemSegment) {
          hintItems.add((HintItemSegment) result);
        }
      }
    }
    if (hintItems.isEmpty()) {
      return null;
    }
    return new HintSegment(getStartIndex(ctx), getStopIndex(ctx), hintItems);
  }

  /**
   * 访问 HINT 项节点，处理单个优化提示项。
   *
   * @param ctx HINT 项上下文
   * @return HintItemSegment 对象
   */
  @Override
  public ASTNode visitHintItem(final OdpsParser.HintItemContext ctx) {
    // 提取 HINT 的原始文本
    String hintText = getOriginalText(ctx);
    return new HintItemSegment(getStartIndex(ctx), getStopIndex(ctx), hintText);
  }

  // ------------------------------------ WINDOW ------------------------------------

  /**
   * 访问 WINDOW 子句节点，处理窗口函数定义。
   *
   * @param ctx WINDOW 子句上下文
   * @return WindowSegment 对象
   */
  @Override
  public ASTNode visitWindow_clause(final OdpsParser.Window_clauseContext ctx) {
    List<WindowDefinitionSegment> windowDefList = new LinkedList<>();

    // 遍历所有窗口定义
    if (ctx.winDef != null) {
      for (OdpsParser.Window_defnContext windowDefnCtx : ctx.winDef) {
        WindowDefinitionSegment windowDef =
            (WindowDefinitionSegment) visitWindow_defn(windowDefnCtx);
        if (windowDef != null) {
          windowDefList.add(windowDef);
        }
      }
    }

    // 填充窗口定义映射表
    for (WindowDefinitionSegment windowDef : windowDefList) {
      if (windowDef.getWindowName() != null) {
        this.windowDefinitions.put(windowDef.getWindowName().getValue(), windowDef);
      }
    }

    WindowSegment windowSegment =
        new WindowSegment(getStartIndex(ctx), getStopIndex(ctx), windowDefList);
    // 提取 WINDOW 子句附近的注释
    CommentSegment comment = extractCommentNearNode(ctx);
    if (comment != null) {
      // WindowSegment 当前不支持注释，可以后续扩展
    }
    return windowSegment;
  }

  /**
   * 访问窗口定义节点，处理单个窗口定义（如 `w1 AS (PARTITION BY dept ORDER BY salary)`）。
   *
   * @param ctx 窗口定义上下文
   * @return WindowDefinitionSegment 对象
   */
  public ASTNode visitWindow_defn(final OdpsParser.Window_defnContext ctx) {
    if (ctx == null) {
      return null;
    }

    // 解析窗口名称
    IdentifierValue windowName = null;
    if (ctx.name != null) {
      windowName = (IdentifierValue) visit(ctx.name);
    }

    // 解析窗口规范
    WindowSpecificationSegment specification = null;
    if (ctx.spec != null) {
      specification = (WindowSpecificationSegment) visitWindow_specification(ctx.spec);
    }

    return new WindowDefinitionSegment(
        getStartIndex(ctx), getStopIndex(ctx), windowName, specification);
  }

  /**
   * 访问窗口规范节点，处理窗口规范（命名窗口引用或内联窗口规范）。
   *
   * @param ctx 窗口规范上下文
   * @return WindowSpecificationSegment 对象
   */
  public ASTNode visitWindow_specification(final OdpsParser.Window_specificationContext ctx) {
    if (ctx == null) {
      return null;
    }

    // 如果是命名窗口引用（如 `w1`）
    if (ctx.id != null && ctx.p == null && ctx.w == null) {
      IdentifierValue windowNameRef = (IdentifierValue) visit(ctx.id);
      // 查找对应的窗口定义
      WindowDefinitionSegment windowDef = windowDefinitions.get(windowNameRef.getValue());
      if (windowDef != null && windowDef.getSpecification() != null) {
        // 返回窗口定义的规范
        return windowDef.getSpecification();
      }
      // 如果找不到窗口定义，返回引用
      return new WindowSpecificationSegment(
          getStartIndex(ctx), getStopIndex(ctx), windowNameRef, null, null, null);
    }

    // 如果是内联窗口规范（如 `(PARTITION BY dept ORDER BY salary)`）
    IdentifierValue windowNameRef = null;
    if (ctx.id != null) {
      windowNameRef = (IdentifierValue) visit(ctx.id);
    }

    PartitionBySegment partitionBy = null;
    WindowOrderBySegment orderBy = null;
    WindowFrameSegment windowFrame = null;

    // 解析 partitioningSpec（包含 PARTITION BY 和 ORDER BY）
    if (ctx.p != null) {
      OdpsParser.PartitioningSpecContext partitioningSpec = ctx.p;

      // 解析 PARTITION BY 子句
      if (partitioningSpec.p != null) {
        partitionBy = createPartitionBySegment(partitioningSpec.p);
      }

      // 解析 ORDER BY 子句
      if (partitioningSpec.o != null) {
        orderBy = createWindowOrderBySegment(partitioningSpec.o);
      }
    }

    // 解析窗口框架
    if (ctx.w != null) {
      windowFrame = (WindowFrameSegment) visitWindow_frame(ctx.w);
    }

    return new WindowSpecificationSegment(
        getStartIndex(ctx), getStopIndex(ctx), windowNameRef, partitionBy, orderBy, windowFrame);
  }

  /**
   * 访问窗口框架节点，处理窗口框架（如 `ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING`）。
   *
   * @param ctx 窗口框架上下文
   * @return WindowFrameSegment 对象
   */
  public ASTNode visitWindow_frame(final OdpsParser.Window_frameContext ctx) {
    if (ctx == null) {
      return null;
    }

    String frameType = null;
    if (ctx.frameType != null) {
      frameType = ctx.frameType.getText();
    }

    WindowFrameBoundarySegment startBoundary = null;
    WindowFrameBoundarySegment endBoundary = null;

    // 处理 BETWEEN...AND 格式
    if (ctx.s != null && ctx.end != null) {
      startBoundary = (WindowFrameBoundarySegment) visitWindow_frame_boundary(ctx.s);
      endBoundary = (WindowFrameBoundarySegment) visitWindow_frame_boundary(ctx.end);
    } else if (ctx.b != null) {
      // 处理单边界格式
      startBoundary = (WindowFrameBoundarySegment) visitWindow_frame_boundary(ctx.b);
    }

    String exclusion = null;
    if (ctx.ex != null) {
      exclusion = getOriginalText(ctx.ex);
    }

    return new WindowFrameSegment(
        getStartIndex(ctx), getStopIndex(ctx), frameType, startBoundary, endBoundary, exclusion);
  }

  /**
   * 访问窗口框架边界节点，处理窗口框架边界（如 `UNBOUNDED PRECEDING`、`1 PRECEDING`、`CURRENT ROW` 等）。
   *
   * @param ctx 窗口框架边界上下文
   * @return WindowFrameBoundarySegment 对象
   */
  public ASTNode visitWindow_frame_boundary(final OdpsParser.Window_frame_boundaryContext ctx) {
    if (ctx == null) {
      return null;
    }

    String boundaryType = null;
    ExpressionSegment offsetExpression = null;

    if (ctx.d != null && ctx.value == null) {
      // UNBOUNDED PRECEDING 或 UNBOUNDED FOLLOWING
      boundaryType = "UNBOUNDED_" + ctx.d.getText();
    } else if (ctx.value != null) {
      // value PRECEDING 或 value FOLLOWING
      offsetExpression = (ExpressionSegment) visitMathExpression(ctx.value);
      if (ctx.d != null) {
        boundaryType = ctx.d.getText(); // PRECEDING 或 FOLLOWING
      }
    } else {
      // CURRENT ROW
      boundaryType = "CURRENT_ROW";
    }

    return new WindowFrameBoundarySegment(
        getStartIndex(ctx), getStopIndex(ctx), boundaryType, offsetExpression);
  }

  /**
   * 创建分区规范片段，从 PARTITION BY 子句中提取分区表达式。
   *
   * @param ctx PARTITION BY 子句上下文
   * @return PartitionBySegment 对象
   */
  private PartitionBySegment createPartitionBySegment(
      final OdpsParser.PartitionByClauseContext ctx) {
    if (ctx == null) {
      return null;
    }
    List<ExpressionSegment> expressions = new LinkedList<>();

    // 处理带括号的表达式列表
    if (ctx.expsParen != null) {
      for (OdpsParser.ExpressionContext expCtx :
          ctx.expsParen.expressionsNotInParenthese().expression()) {
        ExpressionSegment expr = (ExpressionSegment) visit(expCtx);
        if (expr != null) {
          expressions.add(expr);
        }
      }
    }
    // 处理不带括号的表达式列表
    else if (ctx.expsNoParen != null) {
      for (OdpsParser.ExpressionContext expCtx : ctx.expsNoParen.expression()) {
        ExpressionSegment expr = (ExpressionSegment) visit(expCtx);
        if (expr != null) {
          expressions.add(expr);
        }
      }
    }

    return new PartitionBySegment(getStartIndex(ctx), getStopIndex(ctx), expressions);
  }

  /**
   * 创建窗口排序规范片段，从 ORDER BY 子句中提取排序项。
   *
   * @param ctx ORDER BY 子句上下文
   * @return WindowOrderBySegment 对象
   */
  private WindowOrderBySegment createWindowOrderBySegment(
      final OdpsParser.OrderByClauseContext ctx) {
    if (ctx == null) {
      return null;
    }
    List<OrderByItemSegment> items = new LinkedList<>();

    if (ctx.exp != null) {
      for (OdpsParser.ColumnRefOrderContext orderCtx : ctx.exp) {
        OrderByItemSegment item = createOrderByItem(orderCtx);
        if (item != null) {
          items.add(item);
        }
      }
    }

    return new WindowOrderBySegment(getStartIndex(ctx), getStopIndex(ctx), items);
  }

  // ------------------------------------ FUNCTION ------------------------------------

  /**
   * 访问函数节点，处理函数调用，识别窗口函数（带 OVER 子句的函数）。
   *
   * @param ctx 函数上下文
   * @return ExpressionSegment 对象，如果是窗口函数则返回 WindowFunctionSegment，否则返回 LiteralExpressionSegment
   */
  public ASTNode visitFunction(final OdpsParser.FunctionContext ctx) {
    if (ctx == null) {
      return null;
    }

    String originalText = getOriginalText(ctx);

    // 检查是否有 OVER 子句
    if (ctx.ws != null) {
      // 这是一个窗口函数
      String functionName = null;
      if (ctx.name != null) {
        functionName = getOriginalText(ctx.name);
      }

      // 解析函数参数
      List<ExpressionSegment> arguments = new LinkedList<>();
      if (ctx.arg != null) {
        for (OdpsParser.FunctionArgumentContext argCtx : ctx.arg) {
          ExpressionSegment arg = null;
          if (argCtx.s != null) {
            arg = (ExpressionSegment) visit(argCtx.s);
          } else if (argCtx.f != null) {
            // 函数引用作为参数，解析为字面量表达式
            arg = createLiteralExpression(argCtx.f);
          } else if (argCtx.l != null) {
            // lambda 表达式暂不支持，解析为字面量表达式
            arg = createLiteralExpression(argCtx.l);
          }
          if (arg != null) {
            arguments.add(arg);
          }
        }
      }

      // 解析窗口规范
      WindowSpecificationSegment windowSpec =
          (WindowSpecificationSegment) visitWindow_specification(ctx.ws);

      return new WindowFunctionSegment(
          getStartIndex(ctx), getStopIndex(ctx), functionName, arguments, windowSpec, originalText);
    }

    // 普通函数，返回字面量表达式
    return createLiteralExpression(ctx);
  }

  // ------------------------------------ LATERAL VIEW ------------------------------------

  /**
   * 访问 LATERAL VIEW 节点，处理横向视图（用于展开数组或 Map 类型的列）。
   *
   * @param ctx LATERAL VIEW 上下文
   * @return LateralViewSegment 对象
   */
  @Override
  public ASTNode visitLateralView(final OdpsParser.LateralViewContext ctx) {
    if (null == ctx.function()) {
      return null;
    }
    ExpressionSegment function = (ExpressionSegment) visit(ctx.function());
    if (null == function) {
      return null;
    }
    AliasSegment tableAlias = null;
    if (null != ctx.tableAlias()) {
      tableAlias = (AliasSegment) visit(ctx.tableAlias());
    }
    Collection<IdentifierValue> columnAliases = new LinkedList<>();
    if (ctx.identifier() != null && !ctx.identifier().isEmpty()) {
      for (OdpsParser.IdentifierContext identifierContext : ctx.identifier()) {
        columnAliases.add(new IdentifierValue(identifierContext.getText()));
      }
    }
    boolean outer = ctx.KW_OUTER() != null;
    LateralViewSegment lateralViewSegment =
        new LateralViewSegment(
            getStartIndex(ctx), getStopIndex(ctx), function, tableAlias, columnAliases, outer);
    // 提取 LATERAL VIEW 附近的注释
    CommentSegment comment = extractCommentNearNode(ctx);
    if (comment != null) {
      lateralViewSegment.setComment(comment);
    }
    return lateralViewSegment;
  }

  // ------------------------------------ COMMENT ------------------------------------

  /**
   * 从解析上下文提取注释信息并添加到语句中。 注释 token 位于隐藏通道（COMMENT_CHANNEL）中，需要通过 token stream 访问。
   *
   * <p><b>实现说明</b>：
   *
   * <ul>
   *   <li>此方法会提取整个 SELECT 语句范围内的所有注释
   *   <li>这些注释包括 Segment 级别的注释和语句级别的注释
   *   <li>在元数据提取阶段会进行去重，只处理真正的语句级别注释（未被 Segment 提取的注释）
   *   <li>Segment 级别的注释已在各个 visit 方法中提取并存储到对应的 Segment 中
   * </ul>
   *
   * <p><b>设计意图</b>：此方法用于提取语句级别的注释（不在任何特定 Segment 附近的注释）， 但为了确保不遗漏任何注释，当前实现会提取所有注释，去重逻辑在元数据提取阶段完成。
   *
   * @param ctx 解析规则上下文
   * @param statement SQL 语句对象
   */
  private void extractComments(final ParserRuleContext ctx, final AbstractSQLStatement statement) {
    if (ctx == null || statement == null || !(tokenStream instanceof CommonTokenStream)) {
      return;
    }

    CommonTokenStream commonTokenStream = (CommonTokenStream) tokenStream;
    int startTokenIndex = ctx.getStart().getTokenIndex();
    int stopTokenIndex = ctx.getStop().getTokenIndex();

    commonTokenStream.fill();
    List<Token> allTokens = commonTokenStream.getTokens();
    if (allTokens == null) {
      return;
    }

    for (Token token : allTokens) {
      if (token == null || token.getChannel() != COMMENT_CHANNEL) {
        continue;
      }
      int tokenIndex = token.getTokenIndex();
      if (tokenIndex < startTokenIndex || tokenIndex > stopTokenIndex) {
        continue;
      }
      String commentText = token.getText();
      if (commentText != null && !commentText.trim().isEmpty()) {
        statement.addCommentSegment(
            new CommentSegment(commentText, token.getStartIndex(), token.getStopIndex()));
      }
    }
  }

  /**
   * 记录未支持的功能。 用于记录解析过程中遇到但尚未实现的功能。
   *
   * @param ctx 解析规则上下文
   * @param featureName 功能名称
   */
  private void logUnsupportedFeature(final ParserRuleContext ctx, final String featureName) {
    if (null == ctx || !log.isDebugEnabled()) {
      return;
    }
    try {
      String sqlSnippet = getOriginalText(ctx);
      // 限制SQL片段长度，避免日志过长
      if (sqlSnippet != null && sqlSnippet.length() > 100) {
        sqlSnippet = sqlSnippet.substring(0, 100) + "...";
      }
      log.debug("未支持的功能 [{}]: {} | SQL片段: {}", LogUtil.methodName(), featureName, sqlSnippet);
    } catch (Exception e) {
      // 如果获取原始文本失败，至少记录功能名称
      log.debug("未支持的功能 [{}]: {}", LogUtil.methodName(), featureName);
    }
  }

  /**
   * 提取节点附近的注释。 优先获取节点右侧的注释（注释通常在元素之后），如果没有则获取左侧的注释。
   *
   * @param ctx 解析规则上下文
   * @return 注释片段，如果未找到则返回 null
   */
  private CommentSegment extractCommentNearNode(final ParserRuleContext ctx) {
    if (ctx == null || !(tokenStream instanceof CommonTokenStream)) {
      return null;
    }

    CommonTokenStream commonTokenStream = (CommonTokenStream) tokenStream;

    // 优先获取右侧的注释（注释通常在元素之后）
    CommentSegment comment =
        extractCommentFromTokens(
            commonTokenStream.getHiddenTokensToRight(
                ctx.getStop().getTokenIndex(), COMMENT_CHANNEL),
            true);
    if (comment != null) {
      return comment;
    }

    // 如果没有右侧注释，尝试获取左侧注释（注释在元素之前）
    return extractCommentFromTokens(
        commonTokenStream.getHiddenTokensToLeft(ctx.getStart().getTokenIndex(), COMMENT_CHANNEL),
        false);
  }

  /**
   * 从 token 列表中提取注释。
   *
   * @param tokens token 列表
   * @param useFirst 是否使用第一个 token（true）或最后一个 token（false）
   * @return 注释片段，如果未找到则返回 null
   */
  private CommentSegment extractCommentFromTokens(
      final List<Token> tokens, final boolean useFirst) {
    if (tokens == null || tokens.isEmpty()) {
      return null;
    }
    Token commentToken = useFirst ? tokens.get(0) : tokens.get(tokens.size() - 1);
    if (commentToken == null) {
      return null;
    }
    String commentText = commentToken.getText();
    if (commentText == null || commentText.trim().isEmpty()) {
      return null;
    }
    return new CommentSegment(
        commentText, commentToken.getStartIndex(), commentToken.getStopIndex());
  }
}

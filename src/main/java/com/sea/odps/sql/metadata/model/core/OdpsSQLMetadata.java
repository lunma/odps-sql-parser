package com.sea.odps.sql.metadata.model.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sea.odps.sql.metadata.model.clause.GroupByMetadata;
import com.sea.odps.sql.metadata.model.clause.HavingConditionMetadata;
import com.sea.odps.sql.metadata.model.clause.LimitMetadata;
import com.sea.odps.sql.metadata.model.clause.OrderByMetadata;
import com.sea.odps.sql.metadata.model.clause.WhereConditionMetadata;
import com.sea.odps.sql.metadata.model.comment.CommentMetadata;
import com.sea.odps.sql.metadata.model.feature.CombineMetadata;
import com.sea.odps.sql.metadata.model.feature.CteMetadata;
import com.sea.odps.sql.metadata.model.feature.HintMetadata;
import com.sea.odps.sql.metadata.model.feature.LateralViewMetadata;
import com.sea.odps.sql.metadata.model.field.FieldMetadata;
import com.sea.odps.sql.metadata.model.function.FunctionCallMetadata;
import com.sea.odps.sql.metadata.model.join.JoinRelation;
import com.sea.odps.sql.metadata.model.reference.TableReference;
import com.sea.odps.sql.metadata.model.window.WindowFunctionMetadata;
import com.sea.odps.sql.metadata.model.window.WindowMetadata;

/**
 * ODPS SQL 元数据类，包含从 SQL 语句中提取的所有结构化元数据信息。 包括表信息、JOIN 关系、字段信息、子句元数据、特性元数据（CTE、HINT、LATERAL
 * VIEW、集合操作）、 注释信息、窗口定义、窗口函数和函数调用列表等。
 */
public class OdpsSQLMetadata {

  private final List<TableReference> tables = new ArrayList<>();

  private final List<JoinRelation> joins = new ArrayList<>();

  private final List<FieldMetadata> fields = new ArrayList<>();

  private final Map<String, TableReference> aliasIndex = new LinkedHashMap<>();

  private final Map<String, TableReference> nameIndex = new LinkedHashMap<>();

  private WhereConditionMetadata whereCondition;

  private GroupByMetadata groupBy;

  private HavingConditionMetadata havingCondition;

  private OrderByMetadata orderBy;

  private LimitMetadata limit;

  private final List<CteMetadata> ctes = new ArrayList<>();

  private HintMetadata hint;

  private LateralViewMetadata lateralView;

  private final List<CombineMetadata> combines = new ArrayList<>();

  private Boolean distinct;

  private final List<CommentMetadata> comments = new ArrayList<>();

  private final List<WindowMetadata> windows = new ArrayList<>();

  private final List<WindowFunctionMetadata> windowFunctions = new ArrayList<>();

  private final List<FunctionCallMetadata> functionCalls = new ArrayList<>();

  /**
   * 注册表。
   *
   * @param tableReference 表信息
   * @return 注册后的实例（可能是已有实例）
   */
  public TableReference addTable(final TableReference tableReference) {
    if (null == tableReference) {
      return null;
    }
    for (TableReference each : tables) {
      if (each.equals(tableReference)) {
        registerIndex(each);
        return each;
      }
    }
    tables.add(tableReference);
    registerIndex(tableReference);
    return tableReference;
  }

  private void registerIndex(final TableReference tableReference) {
    if (null != tableReference.getAlias() && !aliasIndex.containsKey(tableReference.getAlias())) {
      aliasIndex.put(tableReference.getAlias(), tableReference);
    }
    String qualifiedName = tableReference.getQualifiedName();
    if (null != qualifiedName && !nameIndex.containsKey(qualifiedName)) {
      nameIndex.put(qualifiedName, tableReference);
    }
  }

  public void addJoin(final JoinRelation joinRelation) {
    if (null != joinRelation) {
      joins.add(joinRelation);
    }
  }

  public void addField(final FieldMetadata fieldMetadata) {
    if (null != fieldMetadata) {
      fields.add(fieldMetadata);
    }
  }

  public Collection<TableReference> getTables() {
    return Collections.unmodifiableList(tables);
  }

  public Collection<JoinRelation> getJoins() {
    return Collections.unmodifiableList(joins);
  }

  public Collection<FieldMetadata> getFields() {
    return Collections.unmodifiableList(fields);
  }

  public Optional<TableReference> findByAlias(final String alias) {
    if (null == alias || alias.isEmpty()) {
      return Optional.empty();
    }
    return Optional.ofNullable(aliasIndex.get(alias));
  }

  public Optional<TableReference> findByQualifiedName(final String qualifiedName) {
    if (null == qualifiedName || qualifiedName.isEmpty()) {
      return Optional.empty();
    }
    return Optional.ofNullable(nameIndex.get(qualifiedName));
  }

  public void setWhereCondition(final WhereConditionMetadata whereCondition) {
    this.whereCondition = whereCondition;
  }

  public Optional<WhereConditionMetadata> getWhereCondition() {
    return Optional.ofNullable(whereCondition);
  }

  public void setGroupBy(final GroupByMetadata groupBy) {
    this.groupBy = groupBy;
  }

  public Optional<GroupByMetadata> getGroupBy() {
    return Optional.ofNullable(groupBy);
  }

  public void setHavingCondition(final HavingConditionMetadata havingCondition) {
    this.havingCondition = havingCondition;
  }

  public Optional<HavingConditionMetadata> getHavingCondition() {
    return Optional.ofNullable(havingCondition);
  }

  public void setOrderBy(final OrderByMetadata orderBy) {
    this.orderBy = orderBy;
  }

  public Optional<OrderByMetadata> getOrderBy() {
    return Optional.ofNullable(orderBy);
  }

  public void setLimit(final LimitMetadata limit) {
    this.limit = limit;
  }

  public Optional<LimitMetadata> getLimit() {
    return Optional.ofNullable(limit);
  }

  public void addCte(final CteMetadata cte) {
    if (null != cte) {
      ctes.add(cte);
    }
  }

  public Collection<CteMetadata> getCtes() {
    return Collections.unmodifiableList(ctes);
  }

  public void setHint(final HintMetadata hint) {
    this.hint = hint;
  }

  public Optional<HintMetadata> getHint() {
    return Optional.ofNullable(hint);
  }

  public void setLateralView(final LateralViewMetadata lateralView) {
    this.lateralView = lateralView;
  }

  public Optional<LateralViewMetadata> getLateralView() {
    return Optional.ofNullable(lateralView);
  }

  public void addCombine(final CombineMetadata combine) {
    if (null != combine) {
      combines.add(combine);
    }
  }

  public Collection<CombineMetadata> getCombines() {
    return Collections.unmodifiableList(combines);
  }

  public void setDistinct(final Boolean distinct) {
    this.distinct = distinct;
  }

  public Optional<Boolean> getDistinct() {
    return Optional.ofNullable(distinct);
  }

  public void addComment(final CommentMetadata comment) {
    if (null != comment) {
      comments.add(comment);
    }
  }

  public Collection<CommentMetadata> getComments() {
    return Collections.unmodifiableList(comments);
  }

  public void addWindow(final WindowMetadata window) {
    if (null != window) {
      windows.add(window);
    }
  }

  public Collection<WindowMetadata> getWindows() {
    return Collections.unmodifiableList(windows);
  }

  public void addWindowFunction(final WindowFunctionMetadata windowFunction) {
    if (null != windowFunction) {
      windowFunctions.add(windowFunction);
    }
  }

  public Collection<WindowFunctionMetadata> getWindowFunctions() {
    return Collections.unmodifiableList(windowFunctions);
  }

  public void addFunctionCall(final FunctionCallMetadata functionCall) {
    if (null != functionCall) {
      functionCalls.add(functionCall);
    }
  }

  public Collection<FunctionCallMetadata> getFunctionCalls() {
    return Collections.unmodifiableList(functionCalls);
  }
}

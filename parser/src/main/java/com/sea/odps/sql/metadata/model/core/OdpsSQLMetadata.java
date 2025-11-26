package com.sea.odps.sql.metadata.model.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    // 缓存字段：按作用域分组的字段映射（延迟初始化）
    private Map<String, List<FieldMetadata>> fieldsByScopeCache;

    // 缓存字段：子查询表映射（延迟初始化）
    private Map<String, TableReference> subqueryTableMapCache;

    // 缓存字段：外层查询表别名集合（延迟初始化）
    private Set<String> outerQueryTableAliasesCache;

    // 子查询与其内部表的映射：键为子查询别名，值为该子查询内部的表别名集合
    private final Map<String, Set<String>> subqueryInnerTablesMap = new LinkedHashMap<>();

    // 缓存字段：子查询与其内部表的映射的不可变视图（延迟初始化）
    private Map<String, Set<String>> subqueryInnerTablesMapCache;

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
        // 清除缓存，因为表列表已更改
        subqueryTableMapCache = null;
        outerQueryTableAliasesCache = null;
        return tableReference;
    }

    /**
     * 记录子查询内部的表。
     *
     * <p>当提取子查询内部的表时，调用此方法记录该表属于哪个子查询。
     *
     * @param subqueryAlias 子查询别名
     * @param tableAlias 表别名（如果表有别名）
     * @param tableQualifiedName 表的限定名（用于没有别名的情况）
     */
    public void addSubqueryInnerTable(
            String subqueryAlias, String tableAlias, String tableQualifiedName) {
        if (subqueryAlias == null || subqueryAlias.isEmpty()) {
            return;
        }
        subqueryInnerTablesMap.computeIfAbsent(subqueryAlias, k -> new LinkedHashSet<>());
        Set<String> innerTables = subqueryInnerTablesMap.get(subqueryAlias);
        if (tableAlias != null && !tableAlias.isEmpty()) {
            innerTables.add(tableAlias);
        } else if (tableQualifiedName != null && !tableQualifiedName.isEmpty()) {
            innerTables.add(tableQualifiedName);
        }
        // 清除缓存，因为子查询内部表映射已更改
        subqueryInnerTablesMapCache = null;
    }

    private void registerIndex(final TableReference tableReference) {
        if (null != tableReference.getAlias()
                && !aliasIndex.containsKey(tableReference.getAlias())) {
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
            // 清除缓存，因为 JOIN 关系已更改
            outerQueryTableAliasesCache = null;
        }
    }

    public void addField(final FieldMetadata fieldMetadata) {
        if (null != fieldMetadata) {
            fields.add(fieldMetadata);
            // 清除缓存，因为字段列表已更改
            fieldsByScopeCache = null;
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

    /**
     * 获取按作用域分组的字段映射。
     *
     * <p>返回一个不可变的映射，键为作用域别名（子查询别名），值为该作用域下的字段列表。 顶层查询的字段（scopeAlias 为 null）不会包含在此映射中。
     *
     * @return 按作用域分组的字段映射
     */
    public Map<String, List<FieldMetadata>> getFieldsByScope() {
        if (fieldsByScopeCache == null) {
            Map<String, List<FieldMetadata>> result = new LinkedHashMap<>();
            for (FieldMetadata field : fields) {
                String scopeAlias = field.getScopeAlias();
                if (scopeAlias != null && !scopeAlias.isEmpty()) {
                    result.computeIfAbsent(scopeAlias, k -> new ArrayList<>()).add(field);
                }
            }
            // 将内部列表也设为不可变
            Map<String, List<FieldMetadata>> immutableResult = new LinkedHashMap<>();
            for (Map.Entry<String, List<FieldMetadata>> entry : result.entrySet()) {
                immutableResult.put(entry.getKey(), Collections.unmodifiableList(entry.getValue()));
            }
            fieldsByScopeCache = Collections.unmodifiableMap(immutableResult);
        }
        return fieldsByScopeCache;
    }

    /**
     * 获取子查询表映射。
     *
     * <p>返回一个不可变的映射，键为子查询别名，值为对应的表引用。 只包含有别名的子查询表。
     *
     * @return 子查询表映射
     */
    public Map<String, TableReference> getSubqueryTableMap() {
        if (subqueryTableMapCache == null) {
            Map<String, TableReference> result = new LinkedHashMap<>();
            for (TableReference tableRef : tables) {
                if (tableRef.isSubquery() && tableRef.getAlias() != null) {
                    result.put(tableRef.getAlias(), tableRef);
                }
            }
            subqueryTableMapCache = Collections.unmodifiableMap(result);
        }
        return subqueryTableMapCache;
    }

    /**
     * 获取外层查询表别名集合。
     *
     * <p>返回一个不可变的集合，包含所有属于外层查询（非子查询内部）的表别名。 判断逻辑：
     *
     * <ul>
     *   <li>优先从 JOIN 关系中提取表别名（JOIN 中的表通常是外层查询的表）
     *   <li>如果 JOIN 关系为空，则从所有非子查询的表中提取别名
     * </ul>
     *
     * @return 外层查询表别名集合
     */
    public Set<String> getOuterQueryTableAliases() {
        if (outerQueryTableAliasesCache == null) {
            Set<String> result = new LinkedHashSet<>();

            // 从 JOIN 关系中提取外层查询的表别名
            for (JoinRelation join : joins) {
                TableReference leftRef = join.getLeft();
                TableReference rightRef = join.getRight();
                if (leftRef != null && !leftRef.isSubquery() && leftRef.getAlias() != null) {
                    result.add(leftRef.getAlias());
                }
                if (rightRef != null && !rightRef.isSubquery() && rightRef.getAlias() != null) {
                    result.add(rightRef.getAlias());
                }
            }

            // 如果 JOIN 关系为空，从所有非子查询的表中提取外层查询的表
            if (result.isEmpty()) {
                for (TableReference tableRef : tables) {
                    if (!tableRef.isSubquery() && tableRef.getAlias() != null) {
                        result.add(tableRef.getAlias());
                    }
                }
            }

            outerQueryTableAliasesCache = Collections.unmodifiableSet(result);
        }
        return outerQueryTableAliasesCache;
    }

    /**
     * 获取指定子查询内部的表别名集合。
     *
     * <p>返回一个不可变的集合，包含属于指定子查询的所有内部表的别名。
     *
     * @param subqueryAlias 子查询别名
     * @return 子查询内部的表别名集合，如果子查询不存在或没有内部表则返回空集合
     */
    public Set<String> getSubqueryInnerTableAliases(String subqueryAlias) {
        if (subqueryAlias == null || subqueryAlias.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> innerTables = subqueryInnerTablesMap.get(subqueryAlias);
        return innerTables != null
                ? Collections.unmodifiableSet(innerTables)
                : Collections.emptySet();
    }

    /**
     * 获取所有子查询与其内部表的映射。
     *
     * <p>返回一个不可变的映射，键为子查询别名，值为该子查询内部的表别名集合。
     *
     * @return 子查询与其内部表的映射
     */
    public Map<String, Set<String>> getSubqueryInnerTablesMap() {
        if (subqueryInnerTablesMapCache == null) {
            Map<String, Set<String>> result = new LinkedHashMap<>();
            for (Map.Entry<String, Set<String>> entry : subqueryInnerTablesMap.entrySet()) {
                result.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
            }
            subqueryInnerTablesMapCache = Collections.unmodifiableMap(result);
        }
        return subqueryInnerTablesMapCache;
    }
}

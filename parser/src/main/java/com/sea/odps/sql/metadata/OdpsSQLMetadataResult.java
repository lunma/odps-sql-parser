package com.sea.odps.sql.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata;
import com.sea.odps.sql.metadata.model.field.FieldMetadata;
import com.sea.odps.sql.metadata.model.join.JoinConditionPair;
import com.sea.odps.sql.metadata.model.join.JoinRelation;
import com.sea.odps.sql.metadata.model.reference.ColumnReference;
import com.sea.odps.sql.metadata.model.reference.TableReference;

/** 校验结果。 */
public class OdpsSQLMetadataResult {

    private final boolean supported;

    private final boolean valid;

    private final List<OdpsSQLMetadataError> errors;

    private final OdpsSQLMetadata metadata;

    private OdpsSQLMetadataResult(
            final boolean supported,
            final boolean valid,
            final List<OdpsSQLMetadataError> errors,
            final OdpsSQLMetadata metadata) {
        this.supported = supported;
        this.valid = valid;
        this.errors =
                null == errors
                        ? Collections.emptyList()
                        : Collections.unmodifiableList(new ArrayList<>(errors));
        this.metadata = metadata;
    }

    public static OdpsSQLMetadataResult success(final OdpsSQLMetadata metadata) {
        return new OdpsSQLMetadataResult(true, true, Collections.emptyList(), metadata);
    }

    public static OdpsSQLMetadataResult withErrors(final List<OdpsSQLMetadataError> errors) {
        return new OdpsSQLMetadataResult(true, false, errors, null);
    }

    public static OdpsSQLMetadataResult unsupported(final List<OdpsSQLMetadataError> errors) {
        return new OdpsSQLMetadataResult(false, false, errors, null);
    }

    public boolean isSupported() {
        return supported;
    }

    public boolean isValid() {
        return valid;
    }

    public List<OdpsSQLMetadataError> getErrors() {
        return errors;
    }

    public Optional<OdpsSQLMetadata> getMetadata() {
        return Optional.ofNullable(metadata);
    }

    /**
     * 构建用户友好的 SQL 校验摘要信息。
     *
     * @return 摘要文本
     */
    public String formatSummary() {
        if (!supported) {
            return buildErrorSummary("不支持的 SQL 类型", errors);
        }
        if (!valid) {
            return buildErrorSummary("SQL 存在语法或语义问题", errors);
        }
        if (null == metadata) {
            return "未提取到 SQL 元数据信息";
        }
        StringBuilder sb = new StringBuilder();
        appendTables(sb, metadata.getTables());
        appendJoins(sb, metadata.getJoins());
        appendFields(sb, metadata.getFields());
        return sb.length() == 0 ? "未发现可用的 SQL 元数据" : sb.toString().trim();
    }

    private String buildErrorSummary(
            final String header, final List<OdpsSQLMetadataError> validationErrors) {
        if (null == validationErrors || validationErrors.isEmpty()) {
            return header;
        }
        StringBuilder sb = new StringBuilder(header).append('\n');
        validationErrors.forEach(
                error ->
                        sb.append("- [")
                                .append(error.getLine())
                                .append(':')
                                .append(error.getCharPositionInLine())
                                .append("] ")
                                .append(error.getMessage())
                                .append('\n'));
        return sb.toString().trim();
    }

    private void appendTables(final StringBuilder sb, final Iterable<TableReference> tables) {
        if (tables == null) {
            return;
        }
        List<String> lines = new ArrayList<>();
        for (TableReference table : tables) {
            if (null == table) {
                continue;
            }
            StringBuilder line = new StringBuilder(formatTableReference(table));
            if (table.isSubquery()) {
                line.append(" (子查询)");
            }
            lines.add(line.toString());
        }
        if (!lines.isEmpty()) {
            sb.append("表:\n");
            lines.forEach(line -> sb.append("- ").append(line).append('\n'));
        }
    }

    private void appendJoins(final StringBuilder sb, final Iterable<JoinRelation> joins) {
        if (joins == null) {
            return;
        }
        List<String> lines = new ArrayList<>();
        for (JoinRelation join : joins) {
            if (null == join) {
                continue;
            }
            StringBuilder line = new StringBuilder();
            line.append(Optional.ofNullable(join.getJoinType()).orElse("JOIN"));
            line.append(' ');
            line.append(formatTableReference(join.getLeft()));
            line.append(" ⇔ ");
            line.append(formatTableReference(join.getRight()));
            if (!join.getColumnPairs().isEmpty()) {
                String conditions =
                        join.getColumnPairs().stream()
                                .map(this::formatJoinConditionPair)
                                .collect(Collectors.joining(", "));
                line.append(" 条件: ").append(conditions);
            } else if (!join.getUsingColumns().isEmpty()) {
                String usingCols =
                        join.getUsingColumns().stream()
                                .map(this::formatColumnReference)
                                .collect(Collectors.joining(", "));
                line.append(" USING ").append(usingCols);
            }
            lines.add(line.toString());
        }
        if (!lines.isEmpty()) {
            sb.append("连接关系:\n");
            lines.forEach(line -> sb.append("- ").append(line).append('\n'));
        }
    }

    private void appendFields(final StringBuilder sb, final Iterable<FieldMetadata> fields) {
        if (fields == null) {
            return;
        }
        List<String> lines = new ArrayList<>();
        for (FieldMetadata field : fields) {
            if (null == field) {
                continue;
            }
            String displayName =
                    field.getAliasOptional()
                            .orElseGet(
                                    () ->
                                            Optional.ofNullable(field.getExpression())
                                                    .orElse("<未命名字段>"));
            StringBuilder line = new StringBuilder(displayName);
            field.getAliasOptional()
                    .filter(
                            alias ->
                                    null != field.getExpression()
                                            && !field.getExpression().equals(alias))
                    .ifPresent(alias -> line.append(" <= ").append(field.getExpression()));
            if (!field.getSourceColumns().isEmpty()) {
                String sources =
                        field.getSourceColumns().stream()
                                .map(this::formatColumnReference)
                                .collect(Collectors.joining(", "));
                line.append(" 来源: ").append(sources);
            }
            line.append(" 类型: ").append(field.getCategory().name());
            lines.add(line.toString());
        }
        if (!lines.isEmpty()) {
            sb.append("字段:\n");
            lines.forEach(line -> sb.append("- ").append(line).append('\n'));
        }
    }

    private String formatJoinConditionPair(final JoinConditionPair pair) {
        if (null == pair) {
            return "<未知条件>";
        }
        String left = formatColumnReference(pair.getLeft());
        String right = formatColumnReference(pair.getRight());
        String operator = Optional.ofNullable(pair.getOperator()).orElse("=");
        return left + ' ' + operator + ' ' + right;
    }

    private String formatColumnReference(final ColumnReference column) {
        if (null == column) {
            return "<未知字段>";
        }
        if (null != column.getRaw() && !column.getRaw().isEmpty()) {
            return column.getRaw();
        }
        String owner = Optional.ofNullable(column.getOwner()).orElse("");
        String name = Optional.ofNullable(column.getName()).orElse("");
        if (owner.isEmpty()) {
            return name.isEmpty() ? "<未知字段>" : name;
        }
        if (name.isEmpty()) {
            return owner;
        }
        return owner + '.' + name;
    }

    private String formatTableReference(final TableReference table) {
        if (null == table) {
            return "<未知表>";
        }
        String base =
                Optional.ofNullable(table.getQualifiedName())
                        .filter(name -> !name.isEmpty())
                        .orElseGet(
                                () ->
                                        Optional.ofNullable(table.getRaw())
                                                .filter(raw -> !raw.isEmpty())
                                                .orElse("<未知表>"));
        if (null != table.getAlias() && !table.getAlias().isEmpty()) {
            return base + " AS " + table.getAlias();
        }
        return base;
    }
}

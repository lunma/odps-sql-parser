package com.sea.odps.sql.validation;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sea.odps.sql.autogen.OdpsLexer;
import com.sea.odps.sql.autogen.OdpsParser;
import com.sea.odps.sql.metadata.extractor.OdpsSQLMetadataExtractor;
import com.sea.odps.sql.metadata.model.core.OdpsSQLMetadata;
import com.sea.odps.sql.visitor.OdpsAstBuilderVisitor;
import com.sea.odps.sql.visitor.core.ASTNode;
import com.sea.odps.sql.visitor.odps.OdpsSQLSelectStatement;

/**
 * ODPS SQL 校验器。
 *
 * <p>负责对 ODPS SQL 语句进行语法校验和语义分析，支持以下功能：
 *
 * <ul>
 *   <li>语法错误检测：通过 ANTLR 语法解析器检测语法错误
 *   <li>AST 构建：将解析树转换为语义 AST
 *   <li>元数据提取：从 AST 中提取表、列、JOIN 等元数据信息
 *   <li>错误报告：提供详细的错误位置和错误信息
 * </ul>
 *
 * <p><b>支持的 SELECT 语句功能</b>：
 *
 * <ul>
 *   <li>基础 SELECT 子句（列、表达式、别名）
 *   <li>DISTINCT 关键字
 *   <li>FROM 子句（表、子查询、JOIN）
 *   <li>WHERE、GROUP BY、HAVING 子句
 *   <li>ORDER BY、LIMIT 子句
 *   <li>集合操作（UNION、INTERSECT、EXCEPT）
 *   <li>WITH CTE（公共表表达式）
 *   <li>HINT 子句
 *   <li>WINDOW 子句
 *   <li>LATERAL VIEW
 * </ul>
 */
public class OdpsSQLValidator {

  private static final Logger log = LoggerFactory.getLogger(OdpsSQLValidator.class);

  private final OdpsSQLMetadataExtractor metadataExtractor;

  /** 构造函数，使用默认的元数据提取器。 */
  public OdpsSQLValidator() {
    this(new OdpsSQLMetadataExtractor());
  }

  /**
   * 构造函数。
   *
   * @param metadataExtractor 元数据提取器
   */
  public OdpsSQLValidator(final OdpsSQLMetadataExtractor metadataExtractor) {
    this.metadataExtractor =
        Objects.requireNonNull(metadataExtractor, "metadataExtractor must not be null");
  }

  /**
   * 校验 SQL 语句。
   *
   * <p>校验流程：
   *
   * <ol>
   *   <li>词法分析：将 SQL 文本转换为 Token 流
   *   <li>语法分析：将 Token 流解析为语法树
   *   <li>语法错误检测：收集语法错误（如果有）
   *   <li>AST 构建：将语法树转换为语义 AST
   *   <li>语句类型检查：验证是否为支持的 SELECT 语句
   *   <li>元数据提取：从 AST 中提取表、列等元数据
   * </ol>
   *
   * @param sql SQL 文本，不能为 null
   * @return 校验结果，包含是否支持、是否有效、错误列表和元数据
   * @throws NullPointerException 如果 sql 为 null
   */
  public OdpsSQLValidationResult validate(final String sql) {
    Objects.requireNonNull(sql, "sql must not be null");

    // 检查空 SQL
    String trimmedSql = sql.trim();
    if (trimmedSql.isEmpty()) {
      OdpsSQLValidationError error =
          new OdpsSQLValidationError(1, 0, "SQL statement cannot be empty");
      return OdpsSQLValidationResult.unsupported(Collections.singletonList(error));
    }

    // 词法分析
    CodePointCharStream stream = CharStreams.fromString(sql);
    OdpsLexer lexer = new OdpsLexer(stream);
    CommonTokenStream tokens = new CommonTokenStream(lexer);

    // 语法分析
    OdpsParser parser = new OdpsParser(tokens);
    OdpsSyntaxErrorListener errorListener = new OdpsSyntaxErrorListener();
    parser.removeErrorListeners();
    parser.addErrorListener(errorListener);

    // AST 构建
    OdpsAstBuilderVisitor visitor = new OdpsAstBuilderVisitor();
    // 设置 token stream，用于提取隐藏通道中的注释
    visitor.setTokenStream(tokens);
    ASTNode node = null;
    try {
      node = visitor.visit(parser.script());
    } catch (UnsupportedOperationException ex) {
      // 捕获明确的不支持操作异常
      String message = String.format(Locale.ROOT, "Unsupported operation: %s", ex.getMessage());
      log.debug("Unsupported operation during AST building: {}", ex.getMessage(), ex);
      OdpsSQLValidationError error = new OdpsSQLValidationError(0, 0, message);
      return OdpsSQLValidationResult.unsupported(Collections.singletonList(error));
    } catch (IllegalArgumentException ex) {
      // 捕获参数错误
      String message = String.format(Locale.ROOT, "Invalid argument: %s", ex.getMessage());
      log.debug("Invalid argument during AST building: {}", ex.getMessage(), ex);
      OdpsSQLValidationError error = new OdpsSQLValidationError(0, 0, message);
      return OdpsSQLValidationResult.unsupported(Collections.singletonList(error));
    } catch (NullPointerException ex) {
      // 捕获空指针异常（可能是解析过程中的内部错误）
      String message = "Internal parsing error: null pointer exception";
      log.warn("Null pointer exception during AST building", ex);
      OdpsSQLValidationError error = new OdpsSQLValidationError(0, 0, message);
      return OdpsSQLValidationResult.unsupported(Collections.singletonList(error));
    } catch (RuntimeException ex) {
      // 捕获其他运行时异常
      String message = String.format(Locale.ROOT, "Parsing error: %s", ex.getMessage());
      log.warn("Runtime exception during AST building: {}", ex.getMessage(), ex);
      OdpsSQLValidationError error = new OdpsSQLValidationError(0, 0, message);
      return OdpsSQLValidationResult.unsupported(Collections.singletonList(error));
    }

    // 检查语法错误
    if (errorListener.hasErrors()) {
      List<OdpsSQLValidationError> errors = errorListener.getErrors();
      log.debug("Syntax errors found: {}", errors.size());
      return OdpsSQLValidationResult.withErrors(errors);
    }

    // 检查 AST 节点类型
    if (null == node) {
      String message = "Failed to build AST: node is null";
      log.debug("AST node is null after parsing");
      OdpsSQLValidationError error = new OdpsSQLValidationError(0, 0, message);
      return OdpsSQLValidationResult.unsupported(Collections.singletonList(error));
    }

    if (!(node instanceof OdpsSQLSelectStatement)) {
      String message =
          String.format(
              Locale.ROOT,
              "Unsupported statement type: %s. Only SELECT statements are currently supported.",
              node.getClass().getSimpleName());
      log.debug("Unsupported statement type: {}", node.getClass().getSimpleName());
      OdpsSQLValidationError error = new OdpsSQLValidationError(0, 0, message);
      return OdpsSQLValidationResult.unsupported(Collections.singletonList(error));
    }

    // 提取元数据
    OdpsSQLSelectStatement selectStatement = (OdpsSQLSelectStatement) node;
    try {
      OdpsSQLMetadata metadata = metadataExtractor.extract(selectStatement);
      log.debug(
          "SQL validation successful, extracted metadata with {} tables, {} fields",
          metadata.getTables().size(),
          metadata.getFields().size());
      return OdpsSQLValidationResult.success(metadata);
    } catch (Exception ex) {
      // 元数据提取过程中的错误
      String message = String.format(Locale.ROOT, "Metadata extraction error: %s", ex.getMessage());
      log.warn("Error during metadata extraction: {}", ex.getMessage(), ex);
      OdpsSQLValidationError error = new OdpsSQLValidationError(0, 0, message);
      return OdpsSQLValidationResult.withErrors(Collections.singletonList(error));
    }
  }
}

package com.sea.odps.sql.core.value.identifier;

import com.google.common.base.Strings;

import com.sea.odps.sql.core.enums.QuoteCharacter;
import com.sea.odps.sql.core.util.SQLUtil;
import com.sea.odps.sql.core.value.ValueASTNode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/** 标识符值类，表示 SQL 中的标识符（如表名、列名等）。 包含标识符的值和引号字符类型。 */
@RequiredArgsConstructor
@Getter
@ToString
public class IdentifierValue implements ValueASTNode<String> {
  private final String value;

  private final QuoteCharacter quoteCharacter;

  public IdentifierValue(final String text) {
    this(SQLUtil.getExactlyValue(text), QuoteCharacter.getQuoteCharacter(text));
  }

  public IdentifierValue(final String text, final String reservedCharacters) {
    this(SQLUtil.getExactlyValue(text, reservedCharacters), QuoteCharacter.getQuoteCharacter(text));
  }

  public String getValueWithQuoteCharacters() {
    return null == value ? "" : quoteCharacter.wrap(value);
  }

  @Override
  public String getValue() {
    return value;
  }

  public static String getQuotedContent(final String text) {
    if (Strings.isNullOrEmpty(text)) {
      return text;
    }
    QuoteCharacter quoteCharacter = QuoteCharacter.getQuoteCharacter(text);
    if (quoteCharacter.equals(QuoteCharacter.NONE)) {
      return text.trim();
    }
    return text.substring(1, text.length() - 1);
  }
}

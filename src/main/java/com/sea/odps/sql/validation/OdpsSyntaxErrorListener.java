package com.sea.odps.sql.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/** 语法错误监听器。 */
public class OdpsSyntaxErrorListener extends BaseErrorListener {

  private final List<OdpsSQLValidationError> errors = new ArrayList<>();

  @Override
  public void syntaxError(
      final Recognizer<?, ?> recognizer,
      final Object offendingSymbol,
      final int line,
      final int charPositionInLine,
      final String msg,
      final RecognitionException e) {
    errors.add(new OdpsSQLValidationError(line, charPositionInLine, msg));
  }

  public List<OdpsSQLValidationError> getErrors() {
    return Collections.unmodifiableList(errors);
  }

  public boolean hasErrors() {
    return !errors.isEmpty();
  }
}

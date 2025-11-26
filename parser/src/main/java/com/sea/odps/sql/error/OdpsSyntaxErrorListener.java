package com.sea.odps.sql.error;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import com.sea.odps.sql.metadata.OdpsSQLMetadataError;

/** 语法错误监听器。 */
public class OdpsSyntaxErrorListener extends BaseErrorListener {

    private final List<OdpsSQLMetadataError> errors = new ArrayList<>();

    @Override
    public void syntaxError(
            final Recognizer<?, ?> recognizer,
            final Object offendingSymbol,
            final int line,
            final int charPositionInLine,
            final String msg,
            final RecognitionException e) {
        errors.add(new OdpsSQLMetadataError(line, charPositionInLine, msg));
    }

    public List<OdpsSQLMetadataError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}

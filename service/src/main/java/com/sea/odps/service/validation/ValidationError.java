package com.sea.odps.service.validation;

/** 校验错误。 */
public class ValidationError {

    private final ValidationErrorType type;
    private final String message;
    private final int line;
    private final int charPositionInLine;

    public ValidationError(
            ValidationErrorType type, String message, int line, int charPositionInLine) {
        this.type = type;
        this.message = message;
        this.line = line;
        this.charPositionInLine = charPositionInLine;
    }

    public ValidationErrorType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public int getLine() {
        return line;
    }

    public int getCharPositionInLine() {
        return charPositionInLine;
    }

    @Override
    public String toString() {
        return String.format(
                "[%s] %s (line %d, column %d)", type, message, line, charPositionInLine);
    }
}

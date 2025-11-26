package com.sea.odps.sql.metadata;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/** SQL 校验错误。 */
@Getter
@ToString
@EqualsAndHashCode(of = {"line", "charPositionInLine", "message"})
public class OdpsSQLMetadataError {

    private final int line;

    private final int charPositionInLine;

    private final String message;

    public OdpsSQLMetadataError(
            final int line, final int charPositionInLine, final String message) {
        this.line = line;
        this.charPositionInLine = charPositionInLine;
        this.message = message;
    }
}

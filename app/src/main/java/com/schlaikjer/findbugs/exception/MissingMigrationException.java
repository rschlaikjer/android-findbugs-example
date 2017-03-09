package com.schlaikjer.findbugs.exception;

import java.util.Locale;

public class MissingMigrationException extends RuntimeException {

    public MissingMigrationException(String dbName, int version) {
        super(String.format(Locale.ENGLISH, "Database '%s' missing migration #%d", dbName, version));
    }

}

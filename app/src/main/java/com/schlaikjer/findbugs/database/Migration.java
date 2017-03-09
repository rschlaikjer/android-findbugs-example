package com.schlaikjer.findbugs.database;

import android.database.sqlite.SQLiteDatabase;

public interface Migration {

    void apply(SQLiteDatabase database);

}

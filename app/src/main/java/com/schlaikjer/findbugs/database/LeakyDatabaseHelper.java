package com.schlaikjer.findbugs.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.SparseArray;

import com.schlaikjer.findbugs.exception.MissingMigrationException;

public class LeakyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "leaky_database";
    private static final int DB_VERSION = 1;

    private static final SparseArray<Migration> migrations = new SparseArray<>();

    static {
        migrations.put(1, new Migration() {
            @Override
            public void apply(SQLiteDatabase database) {
                database.execSQL("CREATE TABLE " + DogsTable.TABLE_NAME + " (" +
                        DogsTable._ID + " INTEGER PRIMARY KEY," +
                        DogsTable.COLUMN_NAME + " TEXT, " +
                        DogsTable.COLUMN_AGE + " INTEGER, " +
                        DogsTable.COLUMN_BREED + " TEXT " +
                        " )");
            }
        });
    }

    public LeakyDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        onUpgrade(sqLiteDatabase, 0, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        for (int i = oldVersion + 1; i <= newVersion; i++) {
            Migration migration;
            if ((migration = migrations.get(i)) != null) {
                migration.apply(sqLiteDatabase);
            } else {
                throw new MissingMigrationException(DB_NAME, i);
            }
        }
    }

    public static abstract class DogsTable implements BaseColumns {
        public static final String TABLE_NAME = "dogs";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_AGE = "age";
        public static final String COLUMN_BREED = "breed";

        public static String[] getProjection() {
            return new String[]{
                    COLUMN_NAME, COLUMN_AGE, COLUMN_BREED
            };
        }
    }

}

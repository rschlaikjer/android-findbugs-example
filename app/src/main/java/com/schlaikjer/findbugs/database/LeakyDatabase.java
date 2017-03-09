package com.schlaikjer.findbugs.database;

import android.content.Context;

public class LeakyDatabase {

    private static volatile LeakyDatabase instance;

    private LeakyDatabaseHelper dbHelper;

    public static LeakyDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (LeakyDatabase.class) {
                if (instance == null) {
                    instance = new LeakyDatabase(context);
                }
            }
        }
        return instance;
    }

    private LeakyDatabase(Context context) {
        dbHelper = new LeakyDatabaseHelper(context);
    }

}

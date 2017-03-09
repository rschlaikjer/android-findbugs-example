package com.schlaikjer.findbugs.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.schlaikjer.findbugs.model.Dog;

import java.util.ArrayList;
import java.util.List;

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

    public void addDog(Dog dog) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(LeakyDatabaseHelper.DogsTable.COLUMN_NAME, dog.getName());
        contentValues.put(LeakyDatabaseHelper.DogsTable.COLUMN_AGE, dog.getAge());
        contentValues.put(LeakyDatabaseHelper.DogsTable.COLUMN_BREED, dog.getBreed());
        db.insert(
                LeakyDatabaseHelper.DogsTable.TABLE_NAME,
                null,
                contentValues
        );
    }

    public Dog getDogLeaky(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.query(
                LeakyDatabaseHelper.DogsTable.TABLE_NAME,
                LeakyDatabaseHelper.DogsTable.getProjection(),
                LeakyDatabaseHelper.DogsTable.COLUMN_NAME + " = ?", // Select
                new String[]{name}, // Selection args
                null, // No group by
                null, // No order by
                null // No having
        );

        c.moveToFirst();
        while (!c.isAfterLast()) {
            return new Dog(
                    c.getString(c.getColumnIndex(LeakyDatabaseHelper.DogsTable.COLUMN_NAME)),
                    c.getInt(c.getColumnIndex(LeakyDatabaseHelper.DogsTable.COLUMN_AGE)),
                    c.getString(c.getColumnIndex(LeakyDatabaseHelper.DogsTable.COLUMN_BREED))
            );
        }

        c.close();
        return null;
    }

    public void finallyTest() {
        Cursor c = null;
        System.out.println("Before");
        try {
            System.out.println("Try");
        } finally {
            System.out.println("Finally");
            c.close();
        }
        System.out.println("After");
    }

    public Dog getDog(String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor c = db.query(
                LeakyDatabaseHelper.DogsTable.TABLE_NAME,
                LeakyDatabaseHelper.DogsTable.getProjection(),
                LeakyDatabaseHelper.DogsTable.COLUMN_NAME + " = ?", // Select
                new String[]{name}, // Selection args
                null, // No group by
                null, // No order by
                null // No having
        );

        try {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                return new Dog(
                        c.getString(c.getColumnIndex(LeakyDatabaseHelper.DogsTable.COLUMN_NAME)),
                        c.getInt(c.getColumnIndex(LeakyDatabaseHelper.DogsTable.COLUMN_AGE)),
                        c.getString(c.getColumnIndex(LeakyDatabaseHelper.DogsTable.COLUMN_BREED))
                );
            }
        } finally {
            c.close();
        }

        return null;
    }

}

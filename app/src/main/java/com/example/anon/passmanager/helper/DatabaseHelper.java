package com.example.anon.passmanager.helper;

/**
 * Created by Anon on 2016-12-15.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "PassManager.db";

    public static final String TABLE_NAME = "simplepasswords";
    public static final String TABLE_UNDO = "undos";
    public static final String TABLE_USERNAME = "usernames";

    public static final String COL_ID = "_id";
    public static final String COL_ALIAS = "_alias";
    public static final String COL_NAME = "_name";
    public static final String COL_PASSWORD = "_password";
    public static final String COL_TYPE = "_type";
    public static final String COL_DATE = "_date";
    public static final String COL_LAST_MODIFIED = "_last_modified";
    
    public static final String COL_SQL = "_sql";
    
    public static final String COL_USERNAME = "_username";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TABLE_NAME +"" + "("+
                        COL_ID              + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_ALIAS           + " TEXT, " +
                        COL_NAME            + " TEXT, " +
                        COL_PASSWORD        + " TEXT, " +
                        COL_TYPE            + " INTEGER, " +
                        COL_DATE            + " TEXT DEFAULT '', " +
                        COL_LAST_MODIFIED   + " TEXT DEFAULT ''" +
                        ")"
        );

        // Undo table to make delete queries restorable
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_UNDO + " (" +
                        COL_SQL + " TEXT" + 
                        ")"
        );

        db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TABLE_USERNAME + " (" +
                        COL_USERNAME + " TEXT" +
                        ")"
        );

        db.execSQL(
                "CREATE TRIGGER IF NOT EXISTS _t1_dn BEFORE DELETE ON " + TABLE_NAME + " BEGIN " +
                        "INSERT INTO " + TABLE_UNDO +
                        " VALUES( " +
                        "'INSERT INTO " + TABLE_NAME +
                        "(" + COL_ID + "," + COL_ALIAS + "," + COL_NAME + "," + COL_PASSWORD +
                        "," + COL_TYPE + "," + COL_DATE + "," + COL_LAST_MODIFIED + ")" +
                        "VALUES('||old." + COL_ID + "||','||quote(old." + COL_ALIAS +
                        ")||','||quote(old." + COL_NAME + ")||','||quote(old." + COL_PASSWORD +
                        ")||','||old." + COL_TYPE + "||','||quote(old." + COL_DATE +
                        ")||','||quote(old." + COL_LAST_MODIFIED + ")||')'); END"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERNAME);
        onCreate(db);
    }
}
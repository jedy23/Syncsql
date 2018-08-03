package com.syncsql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBLiteHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "SQLITE";
    private String create_tab = "create table tab (id integer primary key autoincrement," +
            "name text, surname text, gender text, age text)";
    private String create_temporal = "create table tab_tmp (id integer primary key autoincrement," +
            "name text, surname text, gender text, age text, stat text, tmpid integer)";

    private String drop_test = "drop table if exists test";

    public static final String TABLE_NAME = "tab";
    public static final String TABLE_NAME_TMP = "tab_tmp";

    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_SURNME = "surname";
    public static final String COL_GNDR = "gender";
    public static final String COL_AGE = "age";
    public static final String COL_STAT = "stat";
    public static final String COL_TMPID = "tmpid";




    public DBLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(create_tab);
        db.execSQL(create_temporal);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(drop_test);
        db.execSQL(create_tab);
        db.execSQL(create_temporal);
    }
}
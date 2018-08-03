package com.syncsql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

public class DBLiteManager {

    private SQLiteOpenHelper sqLiteOpenHelper;
    private SQLiteDatabase sqLiteDatabase;

    Context context;

    public DBLiteManager(Context context){
        this.context = context;
        sqLiteOpenHelper = new DBLiteHelper(context);
    }

    public void openDB(){
        sqLiteDatabase = sqLiteOpenHelper.getWritableDatabase();
    }

    public void closeDB(){
        sqLiteDatabase.close();
    }

    public long insert(String table, String name, String surname, String gender, String age,
                       String stat, String tmpid){
        ContentValues contentValues = new ContentValues();

        contentValues.put(DBLiteHelper.COL_NAME, name);
        contentValues.put(DBLiteHelper.COL_SURNME, surname);
        contentValues.put(DBLiteHelper.COL_GNDR, gender);
        contentValues.put(DBLiteHelper.COL_AGE, age);

        if(stat!=null)
            contentValues.put(DBLiteHelper.COL_STAT, stat);
        if(tmpid!=null)
            contentValues.put(DBLiteHelper.COL_TMPID, tmpid);

        return sqLiteDatabase.insert(table, null, contentValues);
    }

    public long delete(String table, String id){
        String where = DBLiteHelper.COL_ID + " = ?";
        String[] args = {id};
        return sqLiteDatabase.delete(table, where, args);
    }

    public Cursor select_all(String table){
        return sqLiteDatabase.query(table,
                null, null, null,
                null, null, null);
    }
}

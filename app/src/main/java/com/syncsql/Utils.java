package com.syncsql;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

public class Utils {

    public static ArrayList<Data> parse_string(String rows){
        ArrayList<Data> cursor = new ArrayList<>();

        rows = rows.substring(1, rows.length()-1);

        for(String s : rows.split(",")){
            // Todo review how is the data here..

        }

        Log.e("Test", ".."+rows);

        return cursor;
    }

}

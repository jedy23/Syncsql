package com.syncsql;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

public class Utils {

    public static ArrayList<Data> parse_string(String rows){
        ArrayList<Data> cursor = new ArrayList<>();

        rows = rows.substring(1, rows.length()-1);

        for(String s : rows.split(",")){

            s = s.substring(1,s.length()-1);
            Data data = new Data();
            // Todo review how is the data here..

            cursor.add(data);
        }

        return cursor;
    }

}

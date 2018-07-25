package com.syncsql;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

public class Utils {

    public static ArrayList<Data> parse_string(String rows){
        ArrayList<Data> cursor = new ArrayList<>();

        rows = rows.substring(1, rows.length()-1);

        boolean isr = false;

        for(int i=0; i<rows.length(); ++i){
            if(rows.charAt(i)=='{')
                isr = true;
            else if(rows.charAt(i)=='}')
                isr = false;
            else{
                if(isr){
                    while(rows.charAt(i)==' ' || rows.charAt(i)=='"' ||
                            rows.charAt(i)!=':') i++;

                }

            }

        }

        return cursor;
    }

}

package com.syncsql;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class Utils {

    public static ArrayList<Data> parse_string(String rows){
        ArrayList<Data> cursor = new ArrayList<>();

        // id, name, surname, gender, age, stat
        /* Parsing this...
        [{"id":"1","name":"name","surname":"surname","gender":"gender","age":"age","stat":"d",
        "tmpid":"1"},{"id":"2","name":"name2","surname":"surname2","gender":"gender2","age":"age2",
        "stat":"d","tmpid":"2"}]
         */

        rows = rows.substring(1, rows.length()-1);

        HashMap<String, Integer> map = new HashMap<>();

        map.put("id", 0);
        map.put("name", 1);
        map.put("surname", 2);
        map.put("gender", 3);
        map.put("age", 4);
        map.put("stat", 5);
        map.put("tmpid", 6);

        boolean isr = false;
        int j,k;
        String tkey, tvalue;

        Data data = new Data();
        for(int i=0; i<rows.length(); ++i){
            if(rows.charAt(i)=='{') {
                isr = true;
                data = new Data();
            }
            else if(rows.charAt(i)=='}') {
                isr = false;
                cursor.add(data);
            }
            else{
                if(isr){
                  j = i;
                  while(rows.charAt(j) != ',' && rows.charAt(j) != '}') j++;
                  k = j;
                  while(rows.charAt(k) != ':') k--;

                  tkey = rows.substring(i+1, k-1);

                  tvalue = rows.substring(k+2, j-1);

                  if(rows.charAt(j)=='}') j--;
                  i = j;

                  data.setvar(map.get(tkey),tvalue);
                }
            }
        }


        return cursor;
    }
}

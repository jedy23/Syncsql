package com.syncsql;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class Utils {

    public static ArrayList<Data> parse_string(String rows){
        ArrayList<Data> cursor = new ArrayList<>();

        rows = rows.substring(1, rows.length()-1);

        // id, name, surname, gender, age, stat

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

        // ToDo test this shit

        for(int i=0; i<rows.length(); ++i){
            if(rows.charAt(i)=='{')
                isr = true;
            else if(rows.charAt(i)=='}')
                isr = false;
            else{
                if(isr){
                  j = i;
                  while(rows.charAt(j) != ',' && rows.charAt(j) != '}') j++;
                  k = j;
                  while(rows.charAt(k) != ':') k--;

                  tkey = rows.substring(i+1, k-2);

                  if(rows.charAt(j)=='}') j--;

                  tvalue = rows.substring(k+2, j-1);

                  Data data = new Data();

                  data.setvar(map.get(tkey),tvalue);

                  cursor.add(data);
                }
            }
        }


        return cursor;
    }
}

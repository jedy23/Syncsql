package com.syncsql;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.QuickContactBadge;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    private EditText nameEdit,surnameEdit,ageEdit;
    private Button saveBtn, loadBtn;
    private RadioGroup radioGroup;
    private String gender;
    private DBLiteManager dbManager;

    private String url_server = "http://192.168.1.181/init.php";
    private static final int CONNECTION_TIMEOUT=30000;
    private static final int READ_TIMEOUT=75000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nameEdit = findViewById(R.id.edit_name);
        surnameEdit = findViewById(R.id.edit_surname);
        ageEdit = findViewById(R.id.edit_age);

        saveBtn = findViewById(R.id.savebtn);
        loadBtn = findViewById(R.id.loadbtn);

        gender = null;

        dbManager = new DBLiteManager(this);

        radioGroup = findViewById(R.id.radioGroup);


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name, surname, age;

                name = nameEdit.getText().toString().trim();
                surname = surnameEdit.getText().toString().trim();
                age = ageEdit.getText().toString().trim();

                if(gender!=null && !TextUtils.isEmpty(name)
                        && !TextUtils.isEmpty(surname) && !TextUtils.isEmpty(age)) {

                    StoreTask task = new StoreTask(name, surname, gender, age);
                    task.execute();
                }
                else{
                    Toast.makeText(MainActivity.this,
                            "Llenar datos", Toast.LENGTH_SHORT).show();
                }
            }
        });
        loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Records.class);
                startActivity(intent);
            }
        });
    }

    public void onRadioButtonClicked(View view){

        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radiobtn_f:
                if(checked)
                    gender = "Femenino";
                break;
            case R.id.radiobtn_m:
                    gender = "Masculino";
                break;
        }
    }

    private class StoreTask extends AsyncTask<Void,Void,Void>{
        private ProgressBar spinner;
        private ProgressDialog progressDialog;
        private HttpURLConnection conn;
        private URL url;

        Data data;

        public StoreTask(String name, String surname, String gender, String age) {
            data = new Data();
            data.setName(name);
            data.setSurname(surname);
            data.setGender(gender);
            data.setAge(age);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*
            spinner = new ProgressBar(getApplicationContext());
            spinner.setVisibility(View.VISIBLE);
            spinner.animate();
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            */
            dbManager.openDB();

            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Guardando información");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Approach offline operations + sync step
            String select_tmp_query = "select * from tab_tmp";
            String insert_query = "insert into tab(name, surname, gender" +
                    ",age) values("+data.getName()+","+data.getSurname()+","+
                    data.getGender()+","+data.getAge()+")";
            String delete_query = "delete from tab where id = ";
            try{
                url = new URL(url_server);

                // create and set connection with remote database
                conn = (HttpURLConnection) url.openConnection();

                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setRequestMethod("POST");


                // handle send and receive
                conn.getDoInput();
                conn.getDoOutput();

                // Todo check the POST parameter in server side
                // Dummy stuff
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("QUERY", select_tmp_query);

                String query = builder.build().getEncodedQuery();

                // Open connection for sending data

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                conn.connect();

                int response_code = conn.getResponseCode();

                String result_f;

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    Log.e("Server","Request complete for tmp_tab");

                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    result_f = result.toString();



                    ArrayList<Data> arraydata = Utils.parse_string(result_f);

                    // check integrity of the data
                    Cursor dblite = dbManager.select_all(DBLiteHelper.TABLE_NAME_TMP);

                    Log.e("Server", "Data Size ::" + arraydata.size());


                    if (arraydata.size() > 0) {
                        // update locally
                        for(Data d : arraydata){
                            // check status
                            // try to erase it first in the remote

                            conn.disconnect();
                            builder = new Uri.Builder()
                                    .appendQueryParameter("QUERY",
                                            delete_query+d.getId());

                            query = builder.build().getEncodedQuery();

                            // Open connection for sending data

                            os = conn.getOutputStream();
                            writer = new BufferedWriter(
                                    new OutputStreamWriter(os, "UTF-8"));
                            writer.write(query);
                            writer.flush();
                            writer.close();
                            os.close();
                            conn.connect();

                            response_code = conn.getResponseCode();
                            if (response_code == HttpURLConnection.HTTP_OK){
                                // update local table
                                switch (d.getStat()){
                                    case "i":
                                        dbManager.insert(DBLiteHelper.TABLE_NAME,
                                                d.getName(),
                                                d.getSurname(),
                                                d.getGender(),
                                                d.getAge(),
                                                null, null);
                                        break;

                                    case "d":
                                        if(dbManager.delete(DBLiteHelper.TABLE_NAME,
                                                d.getId()) == -1)
                                            Log.e("ERROR", "SQLite deleting data "+
                                            d.getId());
                                        break;

                                    case "u":
                                        if(dbManager.update(DBLiteHelper.TABLE_NAME,
                                                d.getId(), d.getName(), d.getSurname(),
                                                d.getGender(), d.getAge(),
                                                null, null) == -1)
                                            Log.e("ERROR", "SQLite updating data "+
                                            d.getId());
                                        break;

                                    default:
                                        // problems with remote database
                                        Log.e("ERROR","Problems with remote database");

                                }

                            }
                        }
                    }

                    Log.e("Local","Data size ::" + dblite.getCount());

                    if (dblite.getCount() > 0) {
                        // update in the server

                        while (dblite.moveToNext()) {
                            os = conn.getOutputStream();
                            writer = new BufferedWriter(
                                    new OutputStreamWriter(os, "UTF-8"));

                            // check status from record
                            String queryr = null;
                            switch (dblite.getString(DBLiteHelper.COL_STATi)) {
                                case "i":
                                    queryr = "insert into tab(name, surname, gender" +
                                            ",age) values(" +
                                            dblite.getString(DBLiteHelper.COL_NAMEi) + "," +
                                            dblite.getString(DBLiteHelper.COL_SURNMEi) + "," +
                                            dblite.getString(DBLiteHelper.COL_GNDRi) + "," +
                                            dblite.getString(DBLiteHelper.COL_AGEi) + ")";
                                    break;

                                case "d":
                                    queryr = "delete from tab where id = " +
                                            dblite.getString(DBLiteHelper.COL_TMPIDi);
                                    break;

                                case "u":
                                    queryr = "update tab set " +
                                            DBLiteHelper.COL_NAME + "=" +
                                            dblite.getString(DBLiteHelper.COL_NAMEi) + "," +
                                            DBLiteHelper.COL_SURNME + "=" +
                                            dblite.getString(DBLiteHelper.COL_SURNMEi) + "," +
                                            DBLiteHelper.COL_AGE + "=" +
                                            dblite.getString(DBLiteHelper.COL_AGEi) +
                                            DBLiteHelper.COL_GNDR + "=" +
                                            dblite.getString(DBLiteHelper.COL_GNDRi) +
                                            "where " + DBLiteHelper.COL_ID + "=" +
                                            dblite.getString(DBLiteHelper.COL_TMPIDi);
                                    break;
                            }

                            if (queryr == null) continue;

                            writer.write(queryr);


                            writer.flush();
                            writer.close();
                            os.close();
                            conn.connect();

                            response_code = conn.getResponseCode();

                            if (response_code == HttpURLConnection.HTTP_OK) {
                                // clear records of temporal

                                if (dbManager.delete(DBLiteHelper.TABLE_NAME_TMP,
                                        dblite.getString(DBLiteHelper.COL_IDi)) == -1) {
                                    Log.e("ERROR", "SQLite deleting from tmp id::" +
                                            dblite.getString(DBLiteHelper.COL_IDi));
                                }

                                // delete record due to the trigger in remote

                                os = conn.getOutputStream();
                                writer = new BufferedWriter(
                                        new OutputStreamWriter(os, "UTF-8"));


                                queryr = "delete from "+ DBLiteHelper.TABLE_NAME_TMP +
                                        " order by id desc limit 1";
                                writer.write(queryr);


                                writer.flush();
                                writer.close();
                                os.close();
                                conn.connect();

                                response_code = conn.getResponseCode();

                            }
                        }
                    }

                    // if duplicates are not allowed, check primary key
                    // in this case is allowed since is autoincrement

                    Log.e("Local", "query ::" + insert_query);

                    // insert the data in both databases
                    dbManager.insert(DBLiteHelper.TABLE_NAME,
                            data.getName(), data.getSurname(), data.getGender(), data.getAge(),
                            null,null);

                    Log.e("Local", "After inserting locally");

                    // ToDo Problems here..?
                    // Todo repeat this everywhere
                    conn.disconnect();
                    conn.connect();

                    os = conn.getOutputStream();
                    writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));

                    writer.write(insert_query);

                    Log.e("Local","before flush for server");

                    writer.flush();
                    writer.close();
                    os.close();
                    conn.connect();

                    response_code = conn.getResponseCode();
                    if(response_code!=HttpURLConnection.HTTP_OK){
                        // insert in tmp table
                        Log.e("Server", "Problemas de conexion");

                        dbManager.insert(DBLiteHelper.TABLE_NAME_TMP,
                                data.getName(),
                                data.getSurname(),
                                data.getGender(),
                                data.getAge(),
                                "i",
                                data.getId());
                    }

                }else{
                    // insert in local temporal table
                    Log.e("No conn", "Inserting locally");

                    dbManager.insert(DBLiteHelper.TABLE_NAME_TMP,
                            data.getName(), data.getSurname(), data.getGender(), data.getAge(),
                            data.getStat(), data.getTmpid());

                    dbManager.insert(DBLiteHelper.TABLE_NAME,
                            data.getName(), data.getSurname(), data.getGender(), data.getAge(),
                            null,null);
                }
            } catch (IOException e){
                Log.e("ERROR", "error detected");
                e.printStackTrace();
                // insert in local temporal table
                Log.e("No conn", "Inserting locally");

                dbManager.insert(DBLiteHelper.TABLE_NAME_TMP,
                        data.getName(), data.getSurname(), data.getGender(), data.getAge(),
                        data.getStat(), data.getTmpid());

                dbManager.insert(DBLiteHelper.TABLE_NAME,
                        data.getName(), data.getSurname(), data.getGender(), data.getAge(),
                        null,null);
            }

            conn.disconnect();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            /*
            spinner.setVisibility(View.INVISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            */
            progressDialog.dismiss();

            dbManager.closeDB();
        }
    }

}

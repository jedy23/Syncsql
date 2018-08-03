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

       /* nameEdit = findViewById(R.id.edit_name);
        surnameEdit = findViewById(R.id.edit_surname);
        ageEdit = findViewById(R.id.edit_age);

        saveBtn = findViewById(R.id.savebtn);
        loadBtn = findViewById(R.id.loadbtn);

        gender = null;

        dbManager = new DBLiteManager(this);

        radioGroup = findViewById(R.id.radioGroup); */

        /*
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name, surname, age;

                name = nameEdit.getText().toString().trim();
                surname = surnameEdit.getText().toString().trim();
                age = ageEdit.getText().toString().trim();

                if(gender!=null && !TextUtils.isEmpty(name)
                        && !TextUtils.isEmpty(surname) && !TextUtils.isEmpty(age))
                    new StoreTask(name, surname, gender, age).execute();
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
        });*/
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
        private HttpURLConnection conn;
        private URL url;

        Data data;

        public StoreTask(String name, String surname, String gender, String age) {
            data = new Data();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            spinner = new ProgressBar(getApplicationContext());
            spinner.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            dbManager.openDB();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Approach offline operations + sync step
            String select_tmp_query = "select * from tab_tmp";
            String insert_query = "insert into tab(name, surname, gender" +
                    ",age) values("+data.getName()+","+data.getSurname()+","+
                    data.getGender()+","+data.getAge()+")";
            String insert_query2;
            // Todo add temporal id, to tab_tmp  in order to delete...
            // in the remote server..
            String delete_query = "delete from tab where id = ";
            try{
                url = new URL(url_server);

                // open connection with remote database
                conn = (HttpURLConnection) url.openConnection();

                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setRequestMethod("POST");


                // handle send and receive
                conn.getDoInput();
                conn.getDoOutput();

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

                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    result_f = result.toString();


                    // ToDo test this
                    ArrayList<Data> arraydata = Utils.parse_string(result_f);

                    // check integrity of the data
                    Cursor dblite = dbManager.select_all(DBLiteHelper.TABLE_NAME_TMP);

                    if (arraydata.size() > 0) {
                        // update locally
                        for(Data d : arraydata){
                            dbManager.insert(DBLiteHelper.TABLE_NAME,
                                    d.getName(),d.getSurname(),
                                    d.getGender(),d.getAge(), null, null);


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
                            if (response_code != HttpURLConnection.HTTP_OK){
                                // update table here for deleting remote later
                                dbManager.insert(DBLiteHelper.TABLE_NAME_TMP,
                                        d.getName(),
                                        d.getSurname(),
                                        d.getGender(),
                                        d.getAge(),
                                        "d",
                                        d.getId());
                            }
                        }
                    }
                    if (dblite.getCount() > 0) {
                        // update in the server

                        while(dblite.moveToNext()){
                            os = conn.getOutputStream();
                            writer = new BufferedWriter(
                                    new OutputStreamWriter(os, "UTF-8"));

                            // check status from record
                            // todo

                            writer.write(query);



                            writer.flush();
                            writer.close();
                            os.close();
                            conn.connect();

                            response_code = conn.getResponseCode();

                            if(response_code==HttpURLConnection.HTTP_OK){
                                // clear records of temporal

                                /*
                                * insert_query2 = "insert into tab(name, surname, gender" +
                    ",age) values("+data.getName()+","+data.getSurname()+","+
                    data.getGender()+","+data.getAge()+")";
                                * */
                            }
                        }


                    }

                    // if duplicates are not allowed, check primary key
                    // in this case is allowed since is autoincrement

                    // insert the data in both databases
                    dbManager.insert(DBLiteHelper.TABLE_NAME,
                            data.getName(), data.getSurname(), data.getGender(), data.getAge(),
                            null,null);

                    os = conn.getOutputStream();
                    writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));

                    writer.write(insert_query);

                    writer.flush();
                    writer.close();
                    os.close();
                    conn.connect();

                    response_code = conn.getResponseCode();
                    if(response_code!=HttpURLConnection.HTTP_OK){
                        // insert in tmp table
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
                   dbManager.insert(DBLiteHelper.TABLE_NAME_TMP,
                      data.getName(), data.getSurname(), data.getGender(), data.getAge(),
                           data.getStat(), data.getTmpid());
                }
            } catch (IOException e){
                e.printStackTrace();
            }

            conn.disconnect();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            spinner.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            dbManager.closeDB();
        }
    }

}

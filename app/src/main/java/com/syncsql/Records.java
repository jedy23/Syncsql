package com.syncsql;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Records extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecordsAdapter adapter;
    private DBLiteManager dbManager;

    private String url_server = "http://192.168.1.181/init.php";
    private static final int CONNECTION_TIMEOUT=30000;
    private static final int READ_TIMEOUT=75000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        adapter = new RecordsAdapter(new ArrayList<Data>());

        dbManager = new DBLiteManager(this);

        // Connect local database and load stuff
        // Try to sync with remote database
        new LoadData().execute();

        recyclerView = findViewById(R.id.recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(
                this, LinearLayoutManager.VERTICAL
        ));
        recyclerView.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback touchCallbackDelete =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Confirm to delete

                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());

                final Data data = (Data) viewHolder.itemView.getTag();

                builder.setMessage("¿Deseas eliminar este registro?");
                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        adapter.deleteData(data);
                        dialogInterface.dismiss();
                    }
                });
                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                builder.create().show();
            }
        };

        ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallbackDelete);
        touchHelper.attachToRecyclerView(recyclerView);

    }

    private class LoadData extends AsyncTask<Void, Void, Void>{

        private ProgressDialog progressDialog;
        private ArrayList<Data> mlist;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            dbManager.openDB();
            mlist = new ArrayList<>();

            progressDialog = new ProgressDialog(Records.this);
            progressDialog.setMessage("Cargando información");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Cursor cursor = dbManager.select_all(DBLiteHelper.TABLE_NAME);

            while(cursor.moveToNext()){
                Data tmp = new Data();

                tmp.setAge(cursor.getString(DBLiteHelper.COL_AGEi));
                tmp.setName(cursor.getString(DBLiteHelper.COL_NAMEi));
                tmp.setSurname(cursor.getString(DBLiteHelper.COL_SURNMEi));
                tmp.setGender(cursor.getString(DBLiteHelper.COL_GNDRi));
                tmp.setId(cursor.getString(DBLiteHelper.COL_IDi));

                mlist.add(tmp);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            dbManager.closeDB();
            adapter.setListData(mlist);
            progressDialog.dismiss();
            new SyncTask().execute();
        }
    }

    private class SyncTask extends AsyncTask<Void,Void,Void>{
        private HttpURLConnection conn;
        private URL url;
        private boolean status;
        private ArrayList<Data> mlist;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e("SYNC", "Thread begins");
            mlist = new ArrayList<>();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Approach offline operations + sync step
            status = true;

            String select_tmp_query = "select * from tab_tmp";
            String delete_query = "delete from tab where id = ";

            try{
                url = new URL(url_server);

                // create and set conn
                conn = (HttpURLConnection) url.openConnection();

                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setRequestMethod("POST");

                // handle send and receive
                conn.getDoInput();
                conn.getDoOutput();

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("QUERY", select_tmp_query);

                String query = builder.build().getEncodedQuery();

                // open conn
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                os.close();
                conn.connect();

                int response_code = conn.getResponseCode();

                String result_f;

                if (response_code == HttpURLConnection.HTTP_OK){

                    Log.e("Server", "Request completed");

                    InputStream inputStream = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    result_f = result.toString();


                    ArrayList<Data> arraydata = Utils.parse_string(result_f);

                    Cursor dblite = dbManager.select_all(DBLiteHelper.TABLE_NAME_TMP);

                    Log.e("Server", "Data Size :: " + arraydata.size());

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


                    // Todo only update if the data is different
                    Cursor cursor = dbManager.select_all(DBLiteHelper.TABLE_NAME);

                    while(cursor.moveToNext()){
                        Data tmp = new Data();

                        tmp.setAge(cursor.getString(DBLiteHelper.COL_AGEi));
                        tmp.setName(cursor.getString(DBLiteHelper.COL_NAMEi));
                        tmp.setSurname(cursor.getString(DBLiteHelper.COL_SURNMEi));
                        tmp.setGender(cursor.getString(DBLiteHelper.COL_GNDRi));
                        tmp.setId(cursor.getString(DBLiteHelper.COL_IDi));

                        mlist.add(tmp);
                    }


                }

            }catch (IOException e){
                Log.e("ERROR","Cannot connect");
                e.printStackTrace();
                status = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Refresh view
            if(status){
                adapter.setListData(mlist);
                adapter.notifyDataSetChanged();
            }else{
                new SyncTask().execute();
            }
        }
    }

}

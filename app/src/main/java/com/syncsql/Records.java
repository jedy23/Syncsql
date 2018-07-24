package com.syncsql;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Records extends AppCompatActivity {

    private ArrayList<Data> listData;
    private RecyclerView recyclerView;
    private RecordsAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        listData = new ArrayList<>();

        adapter = new RecordsAdapter(listData);

        recyclerView = findViewById(R.id.recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(
                this, LinearLayoutManager.VERTICAL
        ));
        recyclerView.setAdapter(adapter);

    }


    private class SyncTask extends AsyncTask<Void,Void,Void>{
        private HttpURLConnection conn;
        private URL url;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // set the dialog progress
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // fetch the data from server and sync

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Refresh view

        }
    }

}

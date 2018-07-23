package com.syncsql;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.ViewHolder> {

    private List<Data> listData;

    public class ViewHolder extends RecyclerView.ViewHolder {
        //public TextView title, year, genre;

        public ViewHolder(View view) {
            super(view);
            //title = (TextView) view.findViewById(R.id.title);
            //genre = (TextView) view.findViewById(R.id.genre);
            //year = (TextView) view.findViewById(R.id.year);
        }


    }

    public RecordsAdapter(List<Data> listData){
        this.listData = listData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
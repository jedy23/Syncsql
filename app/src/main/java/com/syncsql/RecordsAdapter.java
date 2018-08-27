package com.syncsql;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.ViewHolder> {

    private ArrayList<Data> listData;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name, surname, gender, age;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.textview_name);
            surname = view.findViewById(R.id.textview_surname);
            gender = view.findViewById(R.id.textview_gender);
            age = view.findViewById(R.id.textview_age);
        }

        @Override
        public void onClick(View view) {
            // When modifying
            Data data = (Data) view.getTag();
            Log.e("OnClick", "Data::"+data.getName());


        }
    }

    public RecordsAdapter(ArrayList<Data> listData){
        this.listData = listData;
    }

    public void setListData(ArrayList<Data> listData) {
        this.listData = listData;
        notifyDataSetChanged();
    }

    public void deleteData(Data obj){
        // Todo task for deleting from server too and from current database

        int position = listData.indexOf(obj);
        boolean del = listData.remove(obj);
        notifyItemRemoved(position);
        if (!del){
            Log.e("Error", "deleting object");
        }


    }

    public ArrayList<Data> getListData() {
        return listData;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Data data = listData.get(position);
        holder.name.setText(data.getName());
        holder.surname.setText(data.getSurname());
        holder.gender.setText(data.getGender());
        holder.age.setText(data.getAge());

        holder.itemView.setTag(listData.get(position));

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}
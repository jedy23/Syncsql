package com.syncsql;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.ViewHolder> {

    private List<Data> listData;

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name, surname, gender, age;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.textview_name);
            surname = view.findViewById(R.id.textview_surname);
            gender = view.findViewById(R.id.textview_gender);
            age = view.findViewById(R.id.textview_age);
        }
    }

    public RecordsAdapter(List<Data> listData){
        this.listData = listData;
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
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}
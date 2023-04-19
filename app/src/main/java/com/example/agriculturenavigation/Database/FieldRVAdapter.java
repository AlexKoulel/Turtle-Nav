package com.example.agriculturenavigation.Database;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agriculturenavigation.R;

import java.util.ArrayList;

public class FieldRVAdapter extends RecyclerView.Adapter<FieldRVAdapter.ViewHolder>
{
    private ArrayList<FieldModal> fieldModalArrayList;
    private Context context;

    public FieldRVAdapter(ArrayList<FieldModal> fieldModalArrayList, Context context)
    {
        this.fieldModalArrayList = fieldModalArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.field_recyclerview_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FieldRVAdapter.ViewHolder holder, int position) {
        FieldModal modal = fieldModalArrayList.get(position);
        holder.fieldNameTV.setText(modal.getFieldName());
        holder.fieldAreaTV.setText(modal.getFieldArea() + " m²");
        //holder.landSurnameTV.setText(modal.getFieldLocation());

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(context, FieldInfo.class);
                i.putExtra("name",modal.getFieldName());
                i.putExtra("location",modal.getFieldLocation());
                i.putExtra("area",modal.getFieldArea());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return fieldModalArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView fieldNameTV,fieldAreaTV;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            fieldNameTV = itemView.findViewById(R.id.tvFieldName);
            fieldAreaTV = itemView.findViewById(R.id.tvFieldArea);
        }
    }
}

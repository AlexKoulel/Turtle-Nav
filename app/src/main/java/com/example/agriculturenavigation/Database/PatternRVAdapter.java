package com.example.agriculturenavigation.Database;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.versionedparcelable.NonParcelField;

import com.example.agriculturenavigation.Maps.MapsDirections;
import com.example.agriculturenavigation.Maps.PatternActivity;
import com.example.agriculturenavigation.R;

import java.util.ArrayList;

public class PatternRVAdapter extends RecyclerView.Adapter<PatternRVAdapter.ViewHolder>
{
    private ArrayList<PatternModal> patternModalArrayList;
    private Context context;

    public PatternRVAdapter(ArrayList<PatternModal> patternModalArrayList,Context context)
    {
        this.patternModalArrayList = patternModalArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pattern_recyclerview_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatternRVAdapter.ViewHolder holder,int position)
    {
        PatternModal modal = patternModalArrayList.get(position);
        holder.patternNameTV.setText(modal.getPatternname());

        holder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                Intent i = new Intent(context,PatternInfo.class);
                Bundle bundle = new Bundle();
                i.putExtra("patternname",modal.getPatternname());
                i.putExtra("pattern",modal.getPattern());
                i.putExtra("belongsto",modal.getBelongsto());
                i.putExtras(bundle);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return patternModalArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView patternNameTV;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            patternNameTV = itemView.findViewById(R.id.tvPatternName);

        }
    }
}

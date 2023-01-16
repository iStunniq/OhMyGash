package com.example.ohmygash;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

public class ReportViewAdapter extends RecyclerView.Adapter<ReportViewAdapter.ViewHolder>{
    @NonNull

    private ArrayList<DataSnapshot> Reports = new ArrayList<>();
    private Intent placeIntent = new Intent();

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reportitem,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataSnapshot currentReport = Reports.get(position);
        holder.ReportDate.setText(currentReport.child("ReportDate").getValue().toString());
        holder.ReportUsers.setText("Total Users: "+currentReport.child("Users").getValue().toString());

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), ViewReport.class);
            intent.putExtra("ReportId", currentReport.getKey());
            view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return Reports.size();
    }

    public void setReports(@NonNull ArrayList<DataSnapshot> report) {
        this.Reports = report;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView ReportDate,ReportUsers;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ReportDate = itemView.findViewById(R.id.RecViewReportDate);
            ReportUsers = itemView.findViewById(R.id.RecViewTotalUsers);
        }
    }
}

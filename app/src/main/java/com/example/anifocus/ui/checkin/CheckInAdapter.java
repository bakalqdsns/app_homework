package com.example.anifocus.ui.checkin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anifocus.R;
import com.example.anifocus.data.local.entity.CheckInEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckInAdapter extends RecyclerView.Adapter<CheckInAdapter.ViewHolder> {

    private List<CheckInEntity> data;

    public CheckInAdapter(List<CheckInEntity> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void updateData(List<CheckInEntity> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate, tvTime, tvCompletedCount;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_checkin_date);
            tvTime = itemView.findViewById(R.id.tv_checkin_time);
            tvCompletedCount = itemView.findViewById(R.id.tv_checkin_completed);
        }

        void bind(CheckInEntity checkIn) {
            tvDate.setText(checkIn.getDate());
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            tvTime.setText(sdf.format(new Date(checkIn.getCheckInTime())));
                tvCompletedCount.setText("已完成 " + checkIn.getCompletedCount() + " 项");
        }
    }
}

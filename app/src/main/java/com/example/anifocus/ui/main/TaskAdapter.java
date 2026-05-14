package com.example.anifocus.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anifocus.R;
import com.example.anifocus.data.local.entity.TaskEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<TaskEntity> tasks;
    private final OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(TaskEntity task);
        void onTaskCheckBoxClick(TaskEntity task);
    }

    public TaskAdapter(List<TaskEntity> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskEntity task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void updateTasks(List<TaskEntity> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle, tvDeadline, tvPriority, tvProgress;
        private final CheckBox checkBox;
        private final ImageView ivPriority;
        private final View layoutProgress;
        private final ProgressBar pbProgress;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvDeadline = itemView.findViewById(R.id.tv_task_deadline);
            tvPriority = itemView.findViewById(R.id.tv_task_priority);
            tvProgress = itemView.findViewById(R.id.tv_task_progress);
            checkBox = itemView.findViewById(R.id.cb_task_complete);
            ivPriority = itemView.findViewById(R.id.iv_priority);
            layoutProgress = itemView.findViewById(R.id.layout_progress);
            pbProgress = itemView.findViewById(R.id.pb_task);
        }

        void bind(TaskEntity task) {
            tvTitle.setText(task.getTitle());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            tvDeadline.setText(sdf.format(new Date(task.getDeadline())));

            String[] priorityLabels = {"低", "中", "高"};
            String[] priorityColors = {"#4CAF50", "#FFB300", "#FF5252"};
            tvPriority.setText(priorityLabels[task.getPriority()]);
            tvPriority.setTextColor(android.graphics.Color.parseColor(priorityColors[task.getPriority()]));

            // 进度条显示
            if (task.isCompleted()) {
                pbProgress.setProgress(100);
                tvProgress.setText("100%");
                tvProgress.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            } else {
                pbProgress.setProgress(task.getProgress());
                tvProgress.setText(task.getProgress() + "%");
                tvProgress.setTextColor(android.graphics.Color.parseColor("#BB86FC"));
            }

            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(task.isCompleted());

            if (task.isCompleted()) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setAlpha(0.5f);
                layoutProgress.setVisibility(View.GONE);
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitle.setAlpha(1.0f);
                layoutProgress.setVisibility(View.VISIBLE);
            }

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                listener.onTaskCheckBoxClick(task);
            });

            itemView.setOnClickListener(v -> listener.onTaskClick(task));
        }
    }
}

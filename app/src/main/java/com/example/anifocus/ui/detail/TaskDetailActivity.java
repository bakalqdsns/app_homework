package com.example.anifocus.ui.detail;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.anifocus.R;
import com.example.anifocus.data.local.AppDatabase;
import com.example.anifocus.data.local.dao.TaskDao;
import com.example.anifocus.data.local.entity.TaskEntity;
import com.example.anifocus.ui.task.TaskEditActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class TaskDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvContent, tvDeadline, tvPriority, tvStatus, tvProgress;
    private Button btnComplete, btnEdit, btnDelete, btnSaveProgress;
    private ImageView btnBack;
    private SeekBar seekBarProgress;
    private TaskDao taskDao;
    private TaskEntity currentTask;
    private long taskId;
    private int savedProgress;

    private final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> loadTask()
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_AniFocus);
        setContentView(R.layout.activity_task_detail);

        AppDatabase db = AppDatabase.getInstance(this);
        taskDao = db.taskDao();

        taskId = getIntent().getLongExtra("task_id", -1);
        if (taskId == -1) {
            Toast.makeText(this, "任务不存在", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadTask();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tv_detail_title);
        tvContent = findViewById(R.id.tv_detail_content);
        tvDeadline = findViewById(R.id.tv_detail_deadline);
        tvPriority = findViewById(R.id.tv_detail_priority);
        tvStatus = findViewById(R.id.tv_detail_status);
        tvProgress = findViewById(R.id.tv_detail_progress);
        btnComplete = findViewById(R.id.btn_complete_task);
        btnEdit = findViewById(R.id.btn_edit_task);
        btnDelete = findViewById(R.id.btn_delete_task);
        btnSaveProgress = findViewById(R.id.btn_save_progress);
        btnBack = findViewById(R.id.btn_back_detail);
        seekBarProgress = findViewById(R.id.seekbar_progress);

        btnBack.setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskEditActivity.class);
            intent.putExtra(TaskEditActivity.EXTRA_TASK_ID, taskId);
            editLauncher.launch(intent);
        });
        btnDelete.setOnClickListener(v -> deleteTask());
        btnComplete.setOnClickListener(v -> toggleComplete());

        // 进度条只能往前拖动
        seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    // 只能拖到已保存进度之后，不允许后退
                    if (progress < savedProgress) {
                        seekBar.setProgress(savedProgress);
                    } else {
                        tvProgress.setText(progress + "%");
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnSaveProgress.setOnClickListener(v -> saveProgress());
    }

    private void loadTask() {
        Executors.newSingleThreadExecutor().execute(() -> {
            currentTask = taskDao.getTaskById(taskId);
            if (currentTask != null) {
                savedProgress = currentTask.getProgress();
                runOnUiThread(() -> {
                    tvTitle.setText(currentTask.getTitle());
                    tvContent.setText(currentTask.getContent().isEmpty() ? "无" : currentTask.getContent());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    tvDeadline.setText(sdf.format(new Date(currentTask.getDeadline())));
                    String[] priorities = {"低", "中", "高"};
                    tvPriority.setText(priorities[currentTask.getPriority()]);

                    // 进度显示
                    seekBarProgress.setProgress(currentTask.getProgress());
                    tvProgress.setText(currentTask.getProgress() + "%");
                    savedProgress = currentTask.getProgress();

                    if (currentTask.isCompleted()) {
                        tvStatus.setText("已完成");
                        tvStatus.setTextColor(getResources().getColor(R.color.priority_low, null));
                        btnComplete.setText("标记未完成");
                        seekBarProgress.setEnabled(false);
                        btnSaveProgress.setEnabled(false);
                    } else {
                        tvStatus.setText("进行中");
                        tvStatus.setTextColor(getResources().getColor(R.color.anime_purple, null));
                        btnComplete.setText("标记完成");
                        seekBarProgress.setEnabled(true);
                        btnSaveProgress.setEnabled(true);
                    }
                });
            }
        });
    }

    private void saveProgress() {
        if (currentTask == null) return;
        int newProgress = seekBarProgress.getProgress();

        if (newProgress < savedProgress) {
            seekBarProgress.setProgress(savedProgress);
            return;
        }

        // 进度100%自动标记完成
        if (newProgress == 100) {
            Executors.newSingleThreadExecutor().execute(() -> {
                currentTask.setProgress(100);
                currentTask.setCompleted(true);
                taskDao.update(currentTask);
                savedProgress = 100;
                runOnUiThread(() -> {
                    Toast.makeText(this, "进度已保存，任务已完成！", Toast.LENGTH_SHORT).show();
                    loadTask();
                });
            });
        } else {
            Executors.newSingleThreadExecutor().execute(() -> {
                currentTask.setProgress(newProgress);
                taskDao.update(currentTask);
                savedProgress = newProgress;
                runOnUiThread(() -> {
                    Toast.makeText(this, "进度已保存：" + newProgress + "%", Toast.LENGTH_SHORT).show();
                });
            });
        }
    }

    private void toggleComplete() {
        if (currentTask == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            if (currentTask.isCompleted()) {
                taskDao.markAsUncompleted(taskId);
                currentTask.setCompleted(false);
            } else {
                taskDao.markAsCompleted(taskId);
                currentTask.setProgress(100);
                taskDao.updateProgress(taskId, 100);
                currentTask.setCompleted(true);
            }
            loadTask();
        });
    }

    private void deleteTask() {
        Executors.newSingleThreadExecutor().execute(() -> {
            taskDao.deleteById(taskId);
            runOnUiThread(() -> {
                Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}

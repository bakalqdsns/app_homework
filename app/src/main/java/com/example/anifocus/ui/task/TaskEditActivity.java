package com.example.anifocus.ui.task;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.anifocus.R;
import com.example.anifocus.data.local.AppDatabase;
import com.example.anifocus.data.local.dao.TaskDao;
import com.example.anifocus.data.local.entity.TaskEntity;
import com.example.anifocus.ui.main.MainActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class TaskEditActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "task_id";

    private EditText etTitle, etContent;
    private Button btnPickDate, btnSave, btnDelete;
    private RadioGroup rgPriority;
    private TaskDao taskDao;
    private TaskEntity currentTask;
    private long selectedDeadline;
    private boolean isEditMode = false;

    private final ActivityResultLauncher<android.content.Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> finish()
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_AniFocus);
        setContentView(R.layout.activity_task_edit);

        AppDatabase db = AppDatabase.getInstance(this);
        taskDao = db.taskDao();

        initViews();
        loadTask();
    }

    private void initViews() {
        etTitle = findViewById(R.id.et_task_title);
        etContent = findViewById(R.id.et_task_content);
        btnPickDate = findViewById(R.id.btn_pick_date);
        btnSave = findViewById(R.id.btn_save_task);
        btnDelete = findViewById(R.id.btn_delete_task);
        rgPriority = findViewById(R.id.rg_priority);

        selectedDeadline = System.currentTimeMillis();
        updateDateButton();

        btnPickDate.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> saveTask());

        btnDelete.setOnClickListener(v -> {
            if (currentTask != null) {
                deleteTask();
            }
        });

        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
    }

    private void loadTask() {
        long taskId = getIntent().getLongExtra(EXTRA_TASK_ID, -1);
        if (taskId != -1) {
            isEditMode = true;
            btnDelete.setVisibility(Button.VISIBLE);
            Executors.newSingleThreadExecutor().execute(() -> {
                currentTask = taskDao.getTaskById(taskId);
                if (currentTask != null) {
                    runOnUiThread(() -> {
                        etTitle.setText(currentTask.getTitle());
                        etContent.setText(currentTask.getContent());
                        selectedDeadline = currentTask.getDeadline();
                        updateDateButton();
                        int[] priorityIds = {R.id.rb_low, R.id.rb_medium, R.id.rb_high};
                        rgPriority.check(priorityIds[currentTask.getPriority()]);
                    });
                }
            });
        } else {
            btnDelete.setVisibility(Button.GONE);
        }
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(selectedDeadline);
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth, 23, 59, 59);
                    selectedDeadline = selected.getTimeInMillis();
                    updateDateButton();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void updateDateButton() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        btnPickDate.setText("截止日期: " + sdf.format(new Date(selectedDeadline)));
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("标题不能为空");
            return;
        }

        final int priority;
        int checkedId = rgPriority.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_low) priority = 0;
        else if (checkedId == R.id.rb_high) priority = 2;
        else priority = 1;

        Executors.newSingleThreadExecutor().execute(() -> {
            if (isEditMode && currentTask != null) {
                currentTask.setTitle(title);
                currentTask.setContent(content);
                currentTask.setDeadline(selectedDeadline);
                currentTask.setPriority(priority);
                taskDao.update(currentTask);
            } else {
                TaskEntity task = new TaskEntity();
                task.setTitle(title);
                task.setContent(content);
                task.setDeadline(selectedDeadline);
                task.setPriority(priority);
                taskDao.insert(task);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void deleteTask() {
        if (currentTask != null) {
            Executors.newSingleThreadExecutor().execute(() -> {
                taskDao.delete(currentTask);
                runOnUiThread(() -> {
                    Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        }
    }
}

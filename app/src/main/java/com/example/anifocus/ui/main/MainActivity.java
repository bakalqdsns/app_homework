package com.example.anifocus.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anifocus.R;
import com.example.anifocus.data.local.AppDatabase;
import com.example.anifocus.data.local.dao.TaskDao;
import com.example.anifocus.data.local.entity.TaskEntity;
import com.example.anifocus.data.remote.QuoteRepository;
import com.example.anifocus.ui.checkin.CheckInActivity;
import com.example.anifocus.ui.detail.TaskDetailActivity;
import com.example.anifocus.ui.task.TaskEditActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private TaskDao taskDao;
    private TextView quoteText, quoteSource;
    private View quoteCard;
    private boolean loadingQuote = false;

    private final ActivityResultLauncher<Intent> taskEditLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> refreshTasks()
    );

    private final ActivityResultLauncher<Intent> detailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> refreshTasks()
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_AniFocus);
        setContentView(R.layout.activity_main);

        initViews();
        initDatabase();
        setupRecyclerView();
        loadQuote();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTasks();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_tasks);
        quoteText = findViewById(R.id.tv_quote_text);
        quoteSource = findViewById(R.id.tv_quote_source);
        quoteCard = findViewById(R.id.card_quote);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add_task);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskEditActivity.class);
            taskEditLauncher.launch(intent);
        });

        View btnCheckIn = findViewById(R.id.btn_go_checkin);
        btnCheckIn.setOnClickListener(v -> {
            startActivity(new Intent(this, CheckInActivity.class));
        });

        quoteCard.setOnClickListener(v -> loadQuote());
    }

    private void initDatabase() {
        AppDatabase db = AppDatabase.getInstance(this);
        taskDao = db.taskDao();
    }

    private void setupRecyclerView() {
        adapter = new TaskAdapter(new ArrayList<>(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadQuote() {
        if (loadingQuote) return;
        loadingQuote = true;

        quoteText.setText("正在获取语录...");
        quoteSource.setText("—— AniFocus");

        QuoteRepository repo = new QuoteRepository();
        repo.fetchQuotes(new QuoteRepository.QuoteCallback() {
            @Override
            public void onSuccess(List<QuoteRepository.Quote> quotes) {
                loadingQuote = false;
                if (!quotes.isEmpty()) {
                    QuoteRepository.Quote q = quotes.get(0);
                    String source = q.character != null && !q.character.isEmpty()
                            ? " ——《 " + q.anime + " 》" + q.character
                            : " ——《 " + q.anime + " 》";
                    quoteText.setText(q.text);
                    quoteSource.setText(source);
                }
            }

            @Override
            public void onError(String error) {
                loadingQuote = false;
                quoteText.setText("今天的你也要加油哦！");
                quoteSource.setText("—— AniFocus");
            }
        });
    }

    private void refreshTasks() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<TaskEntity> tasks = taskDao.getAllTasks();
            runOnUiThread(() -> {
                adapter.updateTasks(tasks);
            });
        });
    }

    @Override
    public void onTaskClick(TaskEntity task) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra("task_id", task.getId());
        detailLauncher.launch(intent);
    }

    @Override
    public void onTaskCheckBoxClick(TaskEntity task) {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (task.isCompleted()) {
                taskDao.markAsUncompleted(task.getId());
            } else {
                taskDao.markAsCompleted(task.getId());
            }
            refreshTasks();
        });
    }
}

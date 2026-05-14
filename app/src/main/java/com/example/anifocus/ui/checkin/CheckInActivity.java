package com.example.anifocus.ui.checkin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.anifocus.R;
import com.example.anifocus.data.local.AppDatabase;
import com.example.anifocus.data.local.dao.CheckInDao;
import com.example.anifocus.data.local.dao.TaskDao;
import com.example.anifocus.data.local.entity.CheckInEntity;
import com.example.anifocus.util.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class CheckInActivity extends AppCompatActivity {

    private TextView tvStreakDays, tvTodayPending, tvTodayInProgress, tvTodayDone, tvCheckInStatus;
    private Button btnCheckIn;
    private RecyclerView recyclerHistory;
    private CheckInAdapter adapter;
    private CheckInDao checkInDao;
    private TaskDao taskDao;
    private NotificationHelper notificationHelper;
    private String todayStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_AniFocus);
        setContentView(R.layout.activity_check_in);

        todayStr = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        AppDatabase db = AppDatabase.getInstance(this);
        checkInDao = db.checkInDao();
        taskDao = db.taskDao();
        notificationHelper = new NotificationHelper(this);

        initViews();
        setupRecyclerView();
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews() {
        tvStreakDays = findViewById(R.id.tv_streak_days);
        tvTodayPending = findViewById(R.id.tv_today_pending);
        tvTodayInProgress = findViewById(R.id.tv_today_in_progress);
        tvTodayDone = findViewById(R.id.tv_today_done);
        tvCheckInStatus = findViewById(R.id.tv_checkin_status);
        btnCheckIn = findViewById(R.id.btn_checkin);
        recyclerHistory = findViewById(R.id.recycler_checkin_history);

        btnCheckIn.setOnClickListener(v -> performCheckIn());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new CheckInAdapter(new ArrayList<>());
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistory.setAdapter(adapter);
    }

    private void loadData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // 加载打卡历史
            List<CheckInEntity> history = checkInDao.getAllCheckIns();
            // 三类任务统计
            int pendingCount = taskDao.getPendingCount();
            int inProgressCount = taskDao.getInProgressCount();
            int doneCount = taskDao.getCompletedCount();
            // 计算连续打卡天数
            int streak = calculateStreak(history);
            // 检查今日是否已打卡
            int hasChecked = checkInDao.hasCheckedIn(todayStr);

            runOnUiThread(() -> {
                adapter.updateData(history);
                tvStreakDays.setText(String.valueOf(streak));
                tvTodayPending.setText(String.valueOf(pendingCount));
                tvTodayInProgress.setText(String.valueOf(inProgressCount));
                tvTodayDone.setText(String.valueOf(doneCount));

                if (hasChecked > 0) {
                    btnCheckIn.setEnabled(false);
                    btnCheckIn.setText("今日已打卡");
                    tvCheckInStatus.setText("太棒了！今日学习已完成～");
                } else {
                    if (doneCount == 0 && inProgressCount == 0) {
                        // 没有任何学习活动
                        btnCheckIn.setEnabled(false);
                        btnCheckIn.setText("还没有任务");
                        tvCheckInStatus.setText("还没有任何学习活动哦，快去添加任务吧！");
                    } else {
                        btnCheckIn.setEnabled(true);
                        btnCheckIn.setText("今日打卡");
                        tvCheckInStatus.setText("完成了一些学习就去打卡吧！");
                    }
                }
            });
        });
    }

    private void performCheckIn() {
        Executors.newSingleThreadExecutor().execute(() -> {
            if (checkInDao.hasCheckedIn(todayStr) > 0) {
                runOnUiThread(() -> Toast.makeText(this, "今日已打卡", Toast.LENGTH_SHORT).show());
                return;
            }

            int inProgressCount = taskDao.getInProgressCount();
            int doneCount = taskDao.getCompletedCount();

            // 条件：有进度变化（进行中 > 0）或全部完成（已完成 > 0 且进行中 = 0）
            if (inProgressCount == 0 && doneCount == 0) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "还没有任何学习活动哦", Toast.LENGTH_SHORT).show();
                    btnCheckIn.setEnabled(false);
                });
                return;
            }

            CheckInEntity checkIn = new CheckInEntity();
            checkIn.setDate(todayStr);
            checkIn.setCompletedCount(doneCount);
            checkInDao.insert(checkIn);

            List<CheckInEntity> history = checkInDao.getAllCheckIns();
            int streak = calculateStreak(history);

            runOnUiThread(() -> {
                Toast.makeText(this, "打卡成功！", Toast.LENGTH_SHORT).show();
                btnCheckIn.setEnabled(false);
                btnCheckIn.setText("今日已打卡");
                tvCheckInStatus.setText("太棒了！今日学习已完成～");
                tvStreakDays.setText(String.valueOf(streak));
                adapter.updateData(history);
                notificationHelper.sendCheckInSuccess(streak);
            });
        });
    }

    private int calculateStreak(List<CheckInEntity> history) {
        if (history == null || history.isEmpty()) return 0;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        int streak = 0;
        Calendar cal = Calendar.getInstance();

        for (CheckInEntity checkIn : history) {
            String expected = sdf.format(cal.getTime());
            if (checkIn.getDate().equals(expected)) {
                streak++;
                cal.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
        }
        return streak;
    }
}

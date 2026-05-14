package com.example.anifocus.data.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.anifocus.data.local.AppDatabase;
import com.example.anifocus.data.local.dao.TaskDao;
import com.example.anifocus.data.local.entity.TaskEntity;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class AniFocusProvider extends ContentProvider {

    public static final String AUTHORITY = "com.anifocus.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/tasks");

    private static final int TASKS = 1;
    private static final int TASK_ID = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, "tasks", TASKS);
        uriMatcher.addURI(AUTHORITY, "tasks/#", TASK_ID);
    }

    private static final String[] PROJECTION = {"id", "title", "content", "deadline", "priority", "isCompleted", "createdAt"};

    private AppDatabase db;

    @Override
    public boolean onCreate() {
        db = AppDatabase.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        if (projection == null) projection = PROJECTION;

        List<TaskEntity> tasks;
        switch (uriMatcher.match(uri)) {
            case TASKS:
                tasks = getAllTasksSync();
                break;
            case TASK_ID:
                long id = ContentUris.parseId(uri);
                TaskEntity t = getTaskByIdSync(id);
                tasks = t != null ? List.of(t) : List.of();
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        MatrixCursor cursor = new MatrixCursor(projection);
        for (TaskEntity task : tasks) {
            cursor.addRow(new Object[]{
                    task.getId(),
                    task.getTitle(),
                    task.getContent(),
                    task.getDeadline(),
                    task.getPriority(),
                    task.isCompleted() ? 1 : 0,
                    task.getCreatedAt()
            });
        }

        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case TASKS:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + ".tasks";
            case TASK_ID:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + ".tasks";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (uriMatcher.match(uri) != TASKS) {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        TaskEntity task = contentValuesToTask(values);
        long id = insertSync(task);

        if (id > 0) {
            Uri resultUri = ContentUris.withAppendedId(CONTENT_URI, id);
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(resultUri, null);
            }
            return resultUri;
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        int rows;

        switch (uriMatcher.match(uri)) {
            case TASKS:
                rows = 0;
                break;
            case TASK_ID:
                long id = ContentUris.parseId(uri);
                rows = deleteSync(id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (rows > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        int rows;

        switch (uriMatcher.match(uri)) {
            case TASKS:
                rows = 0;
                break;
            case TASK_ID:
                long id = ContentUris.parseId(uri);
                TaskEntity task = getTaskByIdSync(id);
                if (task != null) {
                    applyValues(task, values);
                    updateSync(task);
                    rows = 1;
                } else {
                    rows = 0;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (rows > 0 && getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rows;
    }

    // --- 同步操作（使用 CountDownLatch）---

    private List<TaskEntity> getAllTasksSync() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<List<TaskEntity>> ref = new AtomicReference<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ref.set(db.taskDao().getAllTasks());
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}
        List<TaskEntity> result = ref.get();
        return result != null ? result : List.of();
    }

    private TaskEntity getTaskByIdSync(long id) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<TaskEntity> ref = new AtomicReference<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ref.set(db.taskDao().getTaskById(id));
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}
        return ref.get();
    }

    private long insertSync(TaskEntity task) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Long> ref = new AtomicReference<>(-1L);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                ref.set(db.taskDao().insert(task));
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}
        return ref.get();
    }

    private void updateSync(TaskEntity task) {
        CountDownLatch latch = new CountDownLatch(1);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                db.taskDao().update(task);
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}
    }

    private int deleteSync(long id) {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Integer> ref = new AtomicReference<>(0);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                db.taskDao().deleteById(id);
                ref.set(1);
            } catch (Exception e) {
                ref.set(0);
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {}
        return ref.get();
    }

    private TaskEntity contentValuesToTask(ContentValues values) {
        TaskEntity task = new TaskEntity();
        if (values.containsKey("id")) task.setId(values.getAsLong("id"));
        if (values.containsKey("title")) task.setTitle(values.getAsString("title"));
        if (values.containsKey("content")) task.setContent(values.getAsString("content"));
        if (values.containsKey("deadline")) task.setDeadline(values.getAsLong("deadline"));
        if (values.containsKey("priority")) task.setPriority(values.getAsInteger("priority"));
        if (values.containsKey("isCompleted")) task.setCompleted(values.getAsInteger("isCompleted") == 1);
        if (values.containsKey("createdAt")) task.setCreatedAt(values.getAsLong("createdAt"));
        return task;
    }

    private void applyValues(TaskEntity task, ContentValues values) {
        if (values.containsKey("title")) task.setTitle(values.getAsString("title"));
        if (values.containsKey("content")) task.setContent(values.getAsString("content"));
        if (values.containsKey("deadline")) task.setDeadline(values.getAsLong("deadline"));
        if (values.containsKey("priority")) task.setPriority(values.getAsInteger("priority"));
        if (values.containsKey("isCompleted")) task.setCompleted(values.getAsInteger("isCompleted") == 1);
    }
}

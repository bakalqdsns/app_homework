package com.example.anifocus.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class TaskEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String title;
    private String content;
    private long deadline;
    private int priority;
    private boolean isCompleted;
    private long createdAt;
    private int progress;

    public TaskEntity() {
        this.createdAt = System.currentTimeMillis();
        this.isCompleted = false;
        this.priority = 1;
        this.progress = 0;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getDeadline() { return deadline; }
    public void setDeadline(long deadline) { this.deadline = deadline; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
}

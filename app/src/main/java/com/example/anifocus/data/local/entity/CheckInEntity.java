package com.example.anifocus.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "checkins")
public class CheckInEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String date;
    private int completedCount;
    private long checkInTime;

    public CheckInEntity() {
        this.checkInTime = System.currentTimeMillis();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getCompletedCount() { return completedCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }

    public long getCheckInTime() { return checkInTime; }
    public void setCheckInTime(long checkInTime) { this.checkInTime = checkInTime; }
}

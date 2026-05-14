package com.example.anifocus.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.anifocus.data.local.entity.CheckInEntity;

import java.util.List;

@Dao
public interface CheckInDao {
    @Insert
    long insert(CheckInEntity checkIn);

    @Query("SELECT * FROM checkins ORDER BY checkInTime DESC")
    List<CheckInEntity> getAllCheckIns();

    @Query("SELECT * FROM checkins WHERE date = :date LIMIT 1")
    CheckInEntity getCheckInByDate(String date);

    @Query("SELECT COUNT(*) FROM checkins")
    int getTotalCount();

    @Query("SELECT COUNT(*) FROM checkins WHERE date = :date")
    int hasCheckedIn(String date);
}

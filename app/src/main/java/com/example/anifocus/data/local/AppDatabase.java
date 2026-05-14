package com.example.anifocus.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.example.anifocus.data.local.dao.CheckInDao;
import com.example.anifocus.data.local.dao.TaskDao;
import com.example.anifocus.data.local.entity.CheckInEntity;
import com.example.anifocus.data.local.entity.TaskEntity;

@Database(entities = {TaskEntity.class, CheckInEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract TaskDao taskDao();
    public abstract CheckInDao checkInDao();

    public SupportSQLiteDatabase getReadableDatabase() {
        return getOpenHelper().getReadableDatabase();
    }

    public SupportSQLiteDatabase getWritableDatabase() {
        return getOpenHelper().getWritableDatabase();
    }

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "anifocus_database"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}

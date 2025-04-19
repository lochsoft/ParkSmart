package com.lochana.parkingassistant;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {NotificationEntity.class, ParkingData.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract NotificationDao notificationDao();
    public abstract ParkingDataDao parkingDataDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "notification_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // For testing only; avoid in production!
                    .build();
        }
        return instance;
    }
}

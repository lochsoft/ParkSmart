package com.lochana.parkingassistant;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class NotificationEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String message;

    public NotificationEntity(String title, String message) {
        this.title = title;
        this.message = message;
    }
}


package com.lochana.parkingassistant;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface NotificationDao {

    @Insert
    void insert(NotificationEntity notification);

    @Query("SELECT * FROM notifications ORDER BY id DESC")
    List<NotificationEntity> getAllNotifications();

    @Query("DELETE FROM notifications")
    void deleteAll();

}

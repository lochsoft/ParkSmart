package com.lochana.parkingassistant;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ParkingDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ParkingData data);

    @Query("SELECT * FROM parking_data")
    List<ParkingData> getAll();

    @Query("DELETE FROM parking_data")
    void deleteAll();

    @androidx.room.Delete
    void delete(ParkingData data);
}

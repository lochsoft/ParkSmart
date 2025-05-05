package com.lochana.parkingassistant;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface ParkingLocationDao {

    @Insert
    void insert(ParkingLocationEntity location);

    @Update
    void update(ParkingLocationEntity location);

    @Delete
    void delete(ParkingLocationEntity location);

    @Query("SELECT * FROM parking_locations")
    List<ParkingLocationEntity> getAllLocations();

    @Query("SELECT * FROM parking_locations WHERE id = :id")
    ParkingLocationEntity getLocationById(int id);
}

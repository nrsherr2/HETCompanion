package edu.ncsu.csc.assist.data.sqlite.access;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import edu.ncsu.csc.assist.data.sqlite.entities.Alert;

@Dao
public interface AlertDao {

    @Query("SELECT * FROM alerts WHERE type = :type")
    List<Alert> getAll(String type);

    @Insert
    long insert(Alert alert);

    @Insert
    long[] insertAll(List<Alert> alerts);
} 

package edu.ncsu.csc.assist.data.sqlite.access;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import edu.ncsu.csc.assist.data.sqlite.entities.ConfigOption;

@Dao
public interface ConfigOptionDao {

    @Query("SELECT * FROM config")
    List<ConfigOption> getAll();

    @Query("SELECT * FROM config WHERE [key] = :key")
    ConfigOption getByKey(String key);

    @Insert
    void insert(ConfigOption configOption);

    @Delete
    int delete(ConfigOption configOption);

    @Query("DELETE FROM config WHERE [key] = :key")
    int deleteByKey(String key);
}

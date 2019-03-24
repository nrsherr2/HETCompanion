package edu.ncsu.csc.assist.data.sqlite.entities;

import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "config")
public class ConfigOption {

    @PrimaryKey()
    @ColumnInfo(name = "key")
    @NonNull
    public String key;

    @ColumnInfo(name = "value")
    @NonNull
    public String value;

    private ConfigOption() {

    }

    public ConfigOption(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "ConfigOption[%s, %s]", key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigOption that = (ConfigOption) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}

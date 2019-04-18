package edu.ncsu.csc.assist.data.sqlite.entities;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alerts")
public class Alert {

    @PrimaryKey()
    @ColumnInfo(name = "id")
    @NonNull
    public long id;

    @ColumnInfo(name = "type")
    @NonNull
    public String type;

    @ColumnInfo(name = "timestamp")
    @NonNull
    public long timestamp;

    @ColumnInfo(name = "message")
    @NonNull
    public String message;

    @ColumnInfo(name = "read")
    @NonNull
    public boolean read;

    private Alert() {

    }

    public Alert(String type, long timestamp, String message, boolean read) {
        this.type = type;
        this.timestamp = timestamp;
        this.message = message;
        this.read = read;
    }

    public enum AlertType {
        PACKET_LOSS;
    }


    public long getId() {
        return id;
    }

    @NonNull
    public String getType() {
        return type;
    }

    @NonNull
    public long getTimestamp() {
        return timestamp;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                ", read=" + read +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alert alert = (Alert) o;
        return id == alert.id &&
                read == alert.read &&
                Objects.equals(type, alert.type) &&
                Objects.equals(timestamp, alert.timestamp) &&
                Objects.equals(message, alert.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, timestamp, message, read);
    }
}

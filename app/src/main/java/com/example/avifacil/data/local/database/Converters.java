package com.example.avifacil.data.local.database;

import androidx.room.TypeConverter;
import com.example.avifacil.data.local.entity.StatusLote;
import java.util.Date;

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static String fromStatus(StatusLote status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static StatusLote toStatus(String status) {
        return status == null ? null : StatusLote.valueOf(status);
    }
}

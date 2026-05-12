package com.example.avifacil.data.local.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;
import com.example.avifacil.data.local.dao.AvicultorDao;
import com.example.avifacil.data.local.dao.LoteDao;
import com.example.avifacil.data.local.dao.RegistroDao;
import com.example.avifacil.data.local.entity.AvicultorEntity;
import com.example.avifacil.data.local.entity.LoteEntity;
import com.example.avifacil.data.local.entity.RegistroEntity;

@Database(entities = {AvicultorEntity.class, LoteEntity.class, RegistroEntity.class}, version = 14, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;

    public abstract AvicultorDao avicultorDao();
    public abstract LoteDao loteDao();
    public abstract RegistroDao registroDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "avifacil_pro_v14_db")
                            .fallbackToDestructiveMigration()
                            .fallbackToDestructiveMigrationOnDowngrade()
                            .build();
                }
            }
        }
        return instance;
    }
}

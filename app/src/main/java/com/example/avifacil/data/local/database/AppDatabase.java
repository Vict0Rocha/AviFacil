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

/**
 * AppDatabase: Configuração do Banco de Dados Local (SQLite via Room).
 * Define as tabelas (entidades), versão do banco e conversores de tipos.
 * 
 * SGBD: SQLite
 * Persistência: Local e Offline-first
 */
@Database(entities = {AvicultorEntity.class, LoteEntity.class, RegistroEntity.class}, version = 15, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;

    // DAOs (Data Access Objects) para manipulação das tabelas
    public abstract AvicultorDao avicultorDao();
    public abstract LoteDao loteDao();
    public abstract RegistroDao registroDao();

    /**
     * Padrão Singleton para garantir uma única instância do banco de dados em todo o app.
     */
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "avifacil_pro_v15_db")
                            .fallbackToDestructiveMigration() // Recria o banco se a versão mudar sem migração definida
                            .fallbackToDestructiveMigrationOnDowngrade()
                            .build();
                }
            }
        }
        return instance;
    }
}

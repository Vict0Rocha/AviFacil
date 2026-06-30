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
 * ARMAZENAMENTO DE DADOS - PERSISTÊNCIA LOCAL (SGBD SQLITE):
 * 
 * O AviFácil utiliza a biblioteca Room (abstração do SQLite) para persistência local.
 * Esta arquitetura permite que o aplicativo funcione totalmente OFFLINE, garantindo
 * que o produtor rural possa registrar dados mesmo sem internet no aviário.
 */
@Database(
    entities = {
        AvicultorEntity.class, 
        LoteEntity.class, 
        RegistroEntity.class
    }, 
    version = 15, // Versão do esquema do banco de dados
    exportSchema = false
)
@TypeConverters({Converters.class}) // Conversores para tipos complexos como Date
public abstract class AppDatabase extends RoomDatabase {
    
    // Instância estática para o padrão Singleton
    private static volatile AppDatabase instance;

    /**
     * DAOs (Data Access Objects):
     * Interfaces que definem as consultas SQL (SELECT, INSERT, UPDATE, DELETE).
     * O Room gera automaticamente a implementação desses métodos.
     */
    public abstract AvicultorDao avicultorDao();
    public abstract LoteDao loteDao();
    public abstract RegistroDao registroDao();

    /**
     * Singleton: Garante que exista apenas uma conexão aberta com o banco de dados
     * durante todo o ciclo de vida do aplicativo, economizando memória e recursos.
     */
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    // Criação do banco de dados físico no armazenamento interno do celular
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "avifacil_pro_v15_db")
                            .fallbackToDestructiveMigration() // Recria o banco caso a versão mude
                            .build();
                }
            }
        }
        return instance;
    }
}

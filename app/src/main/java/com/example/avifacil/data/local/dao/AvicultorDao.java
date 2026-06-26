package com.example.avifacil.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.avifacil.data.local.entity.AvicultorEntity;
import java.util.List;

@Dao
public interface AvicultorDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE)
    long insert(AvicultorEntity avicultor);

    @Update
    void update(AvicultorEntity avicultor);

    @Query("UPDATE avicultores SET deleted = 1, sincronizado = 0, updatedAt = :timestamp WHERE id = :id")
    void softDelete(long id, long timestamp);

    @Query("SELECT * FROM avicultores WHERE deleted = 0")
    List<AvicultorEntity> getAllAtivos();

    @Query("SELECT * FROM avicultores WHERE id = :id AND deleted = 0")
    AvicultorEntity getById(long id);

    @Query("SELECT * FROM avicultores WHERE uuid = :uuid AND deleted = 0")
    AvicultorEntity getByUuid(String uuid);

    @Query("SELECT * FROM avicultores WHERE uuid = :uuid")
    AvicultorEntity getByUuidSemFiltro(String uuid);

    @Query("SELECT * FROM avicultores WHERE sincronizado = 0")
    List<AvicultorEntity> getPendentesSincronizacao();

    @Query("UPDATE avicultores SET sincronizado = 1 WHERE id = :id")
    void marcarComoSincronizado(long id);
}

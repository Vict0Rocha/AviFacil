package com.example.avifacil.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.avifacil.data.local.entity.AvicultorEntity;
import java.util.List;

@Dao
public interface AvicultorDao {
    @Insert
    long insert(AvicultorEntity avicultor);

    @Update
    void update(AvicultorEntity avicultor);

    @Query("UPDATE avicultores SET deleted = 1, sincronizado = 0, updatedAt = :timestamp WHERE id = :id")
    void softDelete(long id, long timestamp);

    @Query("SELECT * FROM avicultores WHERE deleted = 0")
    List<AvicultorEntity> getAllAtivos();

    @Query("SELECT * FROM avicultores WHERE id = :id AND deleted = 0")
    AvicultorEntity getById(long id);
}

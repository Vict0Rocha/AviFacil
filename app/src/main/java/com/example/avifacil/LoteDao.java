package com.example.avifacil;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface LoteDao {
    @Insert
    long insert(LoteEntity lote);

    @Update
    void update(LoteEntity lote);

    @Query("UPDATE lotes SET deleted = 1, sincronizado = 0 WHERE id = :id")
    void softDelete(long id);

    @Query("SELECT * FROM lotes WHERE avicultorId = :avicultorId AND deleted = 0")
    List<LoteEntity> getLotesAtivosPorAvicultor(long avicultorId);

    @Query("SELECT * FROM lotes WHERE id = :id AND deleted = 0")
    LoteEntity getById(long id);

    @Query("SELECT * FROM lotes WHERE avicultorId = :avicultorId AND status = 'ATIVO' AND deleted = 0 LIMIT 1")
    LoteEntity getLoteAtivo(long avicultorId);

    @Query("SELECT EXISTS(SELECT 1 FROM lotes WHERE avicultorId = :avicultorId AND numeroLote = :numeroLote AND deleted = 0)")
    boolean existeNumeroLote(long avicultorId, String numeroLote);
}

package com.example.avifacil.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.avifacil.data.local.entity.RegistroEntity;
import java.util.Date;
import java.util.List;

@Dao
public interface RegistroDao {
    @Insert(onConflict = androidx.room.OnConflictStrategy.IGNORE)
    long insert(RegistroEntity registro);

    @Update
    void update(RegistroEntity registro);

    @Query("UPDATE registros SET deleted = 1, sincronizado = 0, updatedAt = :timestamp WHERE id = :id")
    void softDelete(long id, long timestamp);

    @Query("SELECT * FROM registros WHERE loteId = :loteId AND deleted = 0 ORDER BY dataRegistro ASC")
    List<RegistroEntity> getRegistrosPorLote(long loteId);

    @Query("SELECT * FROM registros WHERE id = :id AND deleted = 0")
    RegistroEntity getById(long id);

    @Query("SELECT * FROM registros WHERE uuid = :uuid AND deleted = 0")
    RegistroEntity getByUuid(String uuid);

    @Query("SELECT * FROM registros WHERE uuid = :uuid")
    RegistroEntity getByUuidSemFiltro(String uuid);

    @Query("SELECT * FROM registros WHERE loteId = :loteId AND deleted = 0 ORDER BY dataRegistro DESC LIMIT 1")
    RegistroEntity getUltimoRegistro(long loteId);

    @Query("SELECT SUM(avesMortasPeriodo) FROM registros WHERE loteId = :loteId AND deleted = 0")
    int getTotalAvesMortas(long loteId);

    @Query("SELECT SUM(consumoRacaoPeriodo) FROM registros WHERE loteId = :loteId AND deleted = 0")
    double getTotalConsumoRacao(long loteId);

    @Query("SELECT EXISTS(SELECT 1 FROM registros WHERE loteId = :loteId AND dataRegistro = :dataRegistro AND deleted = 0)")
    boolean existeRegistroNaData(long loteId, Date dataRegistro);

    @Query("SELECT COALESCE(SUM(r.avesMortasPeriodo), 0) FROM registros r " +
           "INNER JOIN lotes l ON r.loteId = l.id " +
           "WHERE l.avicultorId = :avicultorId AND r.deleted = 0 AND l.deleted = 0")
    int getTotalAvesMortasGeral(long avicultorId);

    @Query("SELECT * FROM registros WHERE sincronizado = 0")
    List<RegistroEntity> getPendentesSincronizacao();

    @Query("UPDATE registros SET sincronizado = 1 WHERE id = :id")
    void marcarComoSincronizado(long id);
}

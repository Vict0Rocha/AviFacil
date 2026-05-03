package com.example.avifacil;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.Date;
import java.util.List;

@Dao
public interface RegistroDao {
    @Insert
    long insert(RegistroEntity registro);

    @Update
    void update(RegistroEntity registro);

    @Query("UPDATE registros SET deleted = 1, sincronizado = 0 WHERE id = :id")
    void softDelete(long id);

    @Query("SELECT * FROM registros WHERE loteId = :loteId AND deleted = 0 ORDER BY dataRegistro ASC")
    List<RegistroEntity> getRegistrosPorLote(long loteId);

    @Query("SELECT * FROM registros WHERE loteId = :loteId AND deleted = 0 ORDER BY dataRegistro DESC LIMIT 1")
    RegistroEntity getUltimoRegistro(long loteId);

    @Query("SELECT SUM(avesMortasPeriodo) FROM registros WHERE loteId = :loteId AND deleted = 0")
    int getTotalAvesMortas(long loteId);

    @Query("SELECT SUM(consumoRacaoPeriodo) FROM registros WHERE loteId = :loteId AND deleted = 0")
    double getTotalConsumoRacao(long loteId);

    @Query("SELECT EXISTS(SELECT 1 FROM registros WHERE loteId = :loteId AND dataRegistro = :dataRegistro AND deleted = 0)")
    boolean existeRegistroNaData(long loteId, Date dataRegistro);
}

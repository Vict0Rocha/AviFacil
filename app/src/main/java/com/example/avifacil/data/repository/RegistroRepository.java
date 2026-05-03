package com.example.avifacil.data.repository;

import com.example.avifacil.data.local.dao.RegistroDao;
import com.example.avifacil.data.local.entity.RegistroEntity;
import java.util.Date;
import java.util.List;

public class RegistroRepository {
    private final RegistroDao registroDao;

    public RegistroRepository(RegistroDao registroDao) {
        this.registroDao = registroDao;
    }

    public long insert(RegistroEntity registro) {
        registro.setSincronizado(false);
        registro.setUpdatedAt(System.currentTimeMillis());
        return registroDao.insert(registro);
    }

    public void update(RegistroEntity registro) {
        registro.setSincronizado(false);
        registro.setUpdatedAt(System.currentTimeMillis());
        registroDao.update(registro);
    }

    public void softDelete(long id) {
        registroDao.softDelete(id, System.currentTimeMillis());
    }

    public List<RegistroEntity> getRegistrosPorLote(long loteId) {
        return registroDao.getRegistrosPorLote(loteId);
    }

    public RegistroEntity getUltimoRegistro(long loteId) {
        return registroDao.getUltimoRegistro(loteId);
    }

    public int getTotalAvesMortas(long loteId) {
        return registroDao.getTotalAvesMortas(loteId);
    }

    public double getTotalConsumoRacao(long loteId) {
        return registroDao.getTotalConsumoRacao(loteId);
    }

    public boolean existeRegistroNaData(long loteId, Date dataRegistro) {
        return registroDao.existeRegistroNaData(loteId, dataRegistro);
    }
}

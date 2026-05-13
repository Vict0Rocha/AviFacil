package com.example.avifacil.data.repository;

import com.example.avifacil.data.local.dao.LoteDao;
import com.example.avifacil.data.local.entity.LoteEntity;
import java.util.List;

public class LoteRepository {
    private final LoteDao loteDao;

    public LoteRepository(LoteDao loteDao) {
        this.loteDao = loteDao;
    }

    public long insert(LoteEntity lote) {
        lote.setSincronizado(false);
        lote.setUpdatedAt(System.currentTimeMillis());
        return loteDao.insert(lote);
    }

    public void update(LoteEntity lote) {
        lote.setSincronizado(false);
        lote.setUpdatedAt(System.currentTimeMillis());
        loteDao.update(lote);
    }

    public void softDelete(long id) {
        loteDao.softDelete(id, System.currentTimeMillis());
    }

    public List<LoteEntity> getLotesAtivosPorAvicultor(long avicultorId) {
        return loteDao.getLotesAtivosPorAvicultor(avicultorId);
    }

    public LoteEntity getById(long id, long avicultorId) {
        return loteDao.getById(id, avicultorId);
    }

    public LoteEntity getLoteAtivo(long avicultorId) {
        return loteDao.getLoteAtivo(avicultorId);
    }

    public boolean existeNumeroLote(long avicultorId, String numeroLote) {
        return loteDao.existeNumeroLote(avicultorId, numeroLote);
    }
}

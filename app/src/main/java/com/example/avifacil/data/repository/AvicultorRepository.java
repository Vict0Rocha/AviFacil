package com.example.avifacil.data.repository;

import com.example.avifacil.data.local.dao.AvicultorDao;
import com.example.avifacil.data.local.entity.AvicultorEntity;
import java.util.List;

public class AvicultorRepository {
    private final AvicultorDao avicultorDao;

    public AvicultorRepository(AvicultorDao avicultorDao) {
        this.avicultorDao = avicultorDao;
    }

    public long insert(AvicultorEntity avicultor) {
        avicultor.setSincronizado(false);
        avicultor.setUpdatedAt(System.currentTimeMillis());
        return avicultorDao.insert(avicultor);
    }

    public void update(AvicultorEntity avicultor) {
        avicultor.setSincronizado(false);
        avicultor.setUpdatedAt(System.currentTimeMillis());
        avicultorDao.update(avicultor);
    }

    public void softDelete(long id) {
        avicultorDao.softDelete(id, System.currentTimeMillis());
    }

    public List<AvicultorEntity> getAllAtivos() {
        return avicultorDao.getAllAtivos();
    }

    public AvicultorEntity getById(long id) {
        return avicultorDao.getById(id);
    }

    public AvicultorEntity getByUuid(String uuid) {
        return avicultorDao.getByUuid(uuid);
    }
}

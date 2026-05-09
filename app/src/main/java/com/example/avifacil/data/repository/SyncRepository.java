package com.example.avifacil.data.repository;

import com.example.avifacil.data.local.dao.AvicultorDao;
import com.example.avifacil.data.local.dao.LoteDao;
import com.example.avifacil.data.local.dao.RegistroDao;
import com.example.avifacil.data.local.entity.AvicultorEntity;
import com.example.avifacil.data.local.entity.LoteEntity;
import com.example.avifacil.data.local.entity.RegistroEntity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SyncRepository {
    private final AvicultorDao avicultorDao;
    private final LoteDao loteDao;
    private final RegistroDao registroDao;
    private final FirebaseFirestore firestore;

    public SyncRepository(AvicultorDao avicultorDao, LoteDao loteDao, RegistroDao registroDao) {
        this.avicultorDao = avicultorDao;
        this.loteDao = loteDao;
        this.registroDao = registroDao;
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void sincronizarPendentes() throws ExecutionException, InterruptedException {
        sincronizarAvicultores();
        sincronizarLotes();
        sincronizarRegistros();
    }

    private void sincronizarAvicultores() throws ExecutionException, InterruptedException {
        List<AvicultorEntity> pendentes = avicultorDao.getPendentesSincronizacao();
        for (AvicultorEntity avicultor : pendentes) {
            Tasks.await(firestore.collection("avicultores")
                    .document(avicultor.getUuid())
                    .set(avicultor));
            avicultorDao.marcarComoSincronizado(avicultor.getId());
        }
    }

    private void sincronizarLotes() throws ExecutionException, InterruptedException {
        List<LoteEntity> pendentes = loteDao.getPendentesSincronizacao();
        for (LoteEntity lote : pendentes) {
            Tasks.await(firestore.collection("avicultores")
                    .document(lote.getAvicultorUuid())
                    .collection("lotes")
                    .document(lote.getUuid())
                    .set(lote));
            loteDao.marcarComoSincronizado(lote.getId());
        }
    }

    private void sincronizarRegistros() throws ExecutionException, InterruptedException {
        List<RegistroEntity> pendentes = registroDao.getPendentesSincronizacao();
        for (RegistroEntity registro : pendentes) {
            LoteEntity lote = loteDao.getById(registro.getLoteId());
            if (lote != null) {
                Tasks.await(firestore.collection("avicultores")
                        .document(lote.getAvicultorUuid())
                        .collection("lotes")
                        .document(lote.getUuid())
                        .collection("registros")
                        .document(registro.getUuid())
                        .set(registro));
                registroDao.marcarComoSincronizado(registro.getId());
            }
        }
    }
}

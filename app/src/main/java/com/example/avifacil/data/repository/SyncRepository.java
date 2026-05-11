package com.example.avifacil.data.repository;

import android.util.Log;
import com.example.avifacil.data.local.dao.AvicultorDao;
import com.example.avifacil.data.local.dao.LoteDao;
import com.example.avifacil.data.local.dao.RegistroDao;
import com.example.avifacil.data.local.entity.AvicultorEntity;
import com.example.avifacil.data.local.entity.LoteEntity;
import com.example.avifacil.data.local.entity.RegistroEntity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SyncRepository {
    private static final String TAG = "SyncRepository";
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

    private void sincronizarAvicultores() {
        List<AvicultorEntity> pendentes = avicultorDao.getPendentesSincronizacao();
        for (AvicultorEntity avicultor : pendentes) {
            try {
                if (avicultor.getUuid() == null) continue;
                Tasks.await(firestore.collection("avicultores")
                        .document(avicultor.getUuid())
                        .set(avicultor));
                avicultorDao.marcarComoSincronizado(avicultor.getId());
            } catch (Exception e) {
                Log.e(TAG, "Erro ao sincronizar avicultor: " + avicultor.getUuid(), e);
            }
        }
    }

    private void sincronizarLotes() {
        List<LoteEntity> pendentes = loteDao.getPendentesSincronizacao();
        for (LoteEntity lote : pendentes) {
            try {
                if (lote.getUuid() == null || lote.getAvicultorUuid() == null) continue;
                Tasks.await(firestore.collection("avicultores")
                        .document(lote.getAvicultorUuid())
                        .collection("lotes")
                        .document(lote.getUuid())
                        .set(lote));
                loteDao.marcarComoSincronizado(lote.getId());
            } catch (Exception e) {
                Log.e(TAG, "Erro ao sincronizar lote: " + lote.getUuid(), e);
            }
        }
    }

    private void sincronizarRegistros() {
        List<RegistroEntity> pendentes = registroDao.getPendentesSincronizacao();
        for (RegistroEntity registro : pendentes) {
            try {
                LoteEntity lote = loteDao.getById(registro.getLoteId());
                if (lote != null && lote.getUuid() != null && lote.getAvicultorUuid() != null) {
                    registro.setLoteUuid(lote.getUuid());
                    Tasks.await(firestore.collection("avicultores")
                            .document(lote.getAvicultorUuid())
                            .collection("lotes")
                            .document(lote.getUuid())
                            .collection("registros")
                            .document(registro.getUuid())
                            .set(registro));
                    registroDao.marcarComoSincronizado(registro.getId());
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao sincronizar registro: " + registro.getUuid(), e);
            }
        }
    }

    public boolean baixarDados(String avicultorUuid) throws ExecutionException, InterruptedException {
        // 1. Baixar Avicultor
        DocumentSnapshot avDoc = Tasks.await(firestore.collection("avicultores").document(avicultorUuid).get());
        if (avDoc.exists()) {
            AvicultorEntity remoteAv = avDoc.toObject(AvicultorEntity.class);
            if (remoteAv != null) {
                AvicultorEntity localAv = avicultorDao.getByUuid(remoteAv.getUuid());
                long avLocalId;
                if (localAv == null) {
                    remoteAv.setId(0); // Garante que o Room gere um novo ID local
                    avLocalId = avicultorDao.insert(remoteAv);
                } else {
                    avLocalId = localAv.getId();
                    remoteAv.setId(avLocalId);
                    avicultorDao.update(remoteAv);
                }
                avicultorDao.marcarComoSincronizado(avLocalId);

                // 2. Baixar Lotes (Sync secundário - não deve impedir o login se falhar)
                try {
                    baixarLotesERegistros(avicultorUuid, avLocalId);
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao baixar lotes/registros (não fatal): " + e.getMessage());
                }
                return true;
            }
        }
        return false;
    }

    private void baixarLotesERegistros(String avicultorUuid, long avLocalId) throws ExecutionException, InterruptedException {
        QuerySnapshot loteDocs = Tasks.await(firestore.collection("avicultores")
                .document(avicultorUuid)
                .collection("lotes").get());

        for (QueryDocumentSnapshot loteDoc : loteDocs) {
            LoteEntity remoteLote = loteDoc.toObject(LoteEntity.class);
            if (remoteLote != null) {
                LoteEntity localLote = loteDao.getByUuid(remoteLote.getUuid());
                long loteLocalId;
                remoteLote.setAvicultorId(avLocalId);
                if (localLote == null) {
                    remoteLote.setId(0);
                    loteLocalId = loteDao.insert(remoteLote);
                } else {
                    loteLocalId = localLote.getId();
                    remoteLote.setId(loteLocalId);
                    loteDao.update(remoteLote);
                }
                loteDao.marcarComoSincronizado(loteLocalId);

                // 3. Baixar Registros
                QuerySnapshot regDocs = Tasks.await(firestore.collection("avicultores")
                        .document(avicultorUuid)
                        .collection("lotes")
                        .document(remoteLote.getUuid())
                        .collection("registros").get());

                for (QueryDocumentSnapshot regDoc : regDocs) {
                    RegistroEntity remoteReg = regDoc.toObject(RegistroEntity.class);
                    if (remoteReg != null) {
                        RegistroEntity localReg = registroDao.getByUuid(remoteReg.getUuid());
                        remoteReg.setLoteId(loteLocalId);
                        if (localReg == null) {
                            remoteReg.setId(0);
                            long regId = registroDao.insert(remoteReg);
                            registroDao.marcarComoSincronizado(regId);
                        } else {
                            remoteReg.setId(localReg.getId());
                            registroDao.update(remoteReg);
                            registroDao.marcarComoSincronizado(localReg.getId());
                        }
                    }
                }
            }
        }
    }
}

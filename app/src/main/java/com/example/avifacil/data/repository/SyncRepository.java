package com.example.avifacil.data.repository;

import android.util.Log;
import com.google.firebase.firestore.Source;
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

/**
 * ARMAZENAMENTO DE DADOS - SINCRONIZAÇÃO EM NUVEM (FIREBASE FIRESTORE):
 * 
 * Este repositório é o "cérebro" da sincronização. Ele implementa a estratégia 
 * "Offline-first": os dados são salvos primeiro no SQLite local e depois 
 * sincronizados com o Google Firebase Firestore (Nuvem).
 */
public class SyncRepository {
    private static final String TAG = "SyncRepository";
    
    // Conexões locais (DAOs) e remota (Firestore)
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

    /**
     * MÉTODO DE SAÍDA (UPLOAD): 
     * Percorre os dados locais que possuem a flag 'sincronizado = false' 
     * e os envia para as coleções correspondentes no Firestore.
     */
    public void sincronizarPendentes() throws ExecutionException, InterruptedException {
        sincronizarAvicultores();
        sincronizarLotes();
        sincronizarRegistros();
    }

    /**
     * Sincroniza os dados dos avicultores pendentes.
     */
    private void sincronizarAvicultores() {
        List<AvicultorEntity> pendentes = avicultorDao.getPendentesSincronizacao();
        for (AvicultorEntity avicultor : pendentes) {
            try {
                if (avicultor.getUuid() == null) continue;

                if (avicultor.isDeleted()) {
                    Tasks.await(firestore.collection("avicultores")
                            .document(avicultor.getUuid())
                            .delete());
                } else {
                    Tasks.await(firestore.collection("avicultores")
                            .document(avicultor.getUuid())
                            .set(avicultor));
                }
                avicultorDao.marcarComoSincronizado(avicultor.getId());
            } catch (Exception e) {
                Log.e(TAG, "Erro ao sincronizar avicultor: " + avicultor.getUuid(), e);
            }
        }
    }

    /**
     * Exemplo de lógica de sincronização de lotes:
     * 1. Busca lotes marcados como pendentes no SQLite.
     * 2. Se deletado localmente -> Deleta na nuvem.
     * 3. Caso contrário -> Faz o 'set' (upsert) na nuvem.
     * 4. Sucesso? Marca como sincronizado no SQLite local.
     */
    private void sincronizarLotes() {
        List<LoteEntity> pendentes = loteDao.getPendentesSincronizacao();
        for (LoteEntity lote : pendentes) {
            try {
                if (lote.getUuid() == null || lote.getAvicultorUuid() == null) continue;
                
                if (lote.isDeleted()) {
                    // Operação de exclusão lógica refletida na nuvem
                    Tasks.await(firestore.collection("avicultores")
                            .document(lote.getAvicultorUuid())
                            .collection("lotes")
                            .document(lote.getUuid())
                            .delete());
                } else {
                    // Envio dos dados para o Firestore
                    Tasks.await(firestore.collection("avicultores")
                            .document(lote.getAvicultorUuid())
                            .collection("lotes")
                            .document(lote.getUuid())
                            .set(lote));
                }
                // Marcação de sucesso local
                loteDao.marcarComoSincronizado(lote.getId());
            } catch (Exception e) {
                Log.e(TAG, "Erro ao sincronizar lote: " + lote.getUuid(), e);
            }
        }
    }

    /**
     * Sincroniza os registros de produção pendentes.
     */
    private void sincronizarRegistros() {
        List<RegistroEntity> pendentes = registroDao.getPendentesSincronizacao();
        for (RegistroEntity registro : pendentes) {
            try {
                if (registro.getUuid() == null || registro.getLoteUuid() == null) continue;

                LoteEntity lote = loteDao.getByUuidSemFiltro(registro.getLoteUuid());
                if (lote == null || lote.getAvicultorUuid() == null) continue;

                if (registro.isDeleted()) {
                    Tasks.await(firestore.collection("avicultores")
                            .document(lote.getAvicultorUuid())
                            .collection("lotes")
                            .document(registro.getLoteUuid())
                            .collection("registros")
                            .document(registro.getUuid())
                            .delete());
                } else {
                    Tasks.await(firestore.collection("avicultores")
                            .document(lote.getAvicultorUuid())
                            .collection("lotes")
                            .document(registro.getLoteUuid())
                            .collection("registros")
                            .document(registro.getUuid())
                            .set(registro));
                }
                registroDao.marcarComoSincronizado(registro.getId());
            } catch (Exception e) {
                Log.e(TAG, "Erro ao sincronizar registro: " + registro.getUuid(), e);
            }
        }
    }

    /**
     * MÉTODO DE ENTRADA (DOWNLOAD):
     * Baixa os dados da nuvem para o celular. 
     * Utiliza timestamps (updatedAt) para garantir que a versão mais recente vença 
     * conflitos de edição entre dispositivos diferentes.
     */
    public boolean baixarDados(String avicultorUuid) throws ExecutionException, InterruptedException {
        // Busca o perfil do avicultor na nuvem usando o UUID único gerado pelo Firebase Auth
        DocumentSnapshot avDoc = Tasks.await(firestore.collection("avicultores")
                .document(avicultorUuid).get(Source.SERVER));
        
        if (avDoc.exists()) {
            AvicultorEntity remoteAv = avDoc.toObject(AvicultorEntity.class);
            if (remoteAv != null) {
                // Lógica de Reconciliação: 
                // Se não existe localmente -> Insere.
                // Se existe e a versão remota é mais nova -> Atualiza SQLite.
                atualizarLocalmente(remoteAv);
                
                // Cascata: Após baixar o avicultor, baixa seus lotes e registros
                baixarLotesERegistros(avicultorUuid, remoteAv.getId());
                return true;
            }
        }
        return false;
    }

    private void atualizarLocalmente(AvicultorEntity remoteAv) {
        AvicultorEntity localAv = avicultorDao.getByUuidSemFiltro(remoteAv.getUuid());
        if (localAv == null) {
            avicultorDao.insert(remoteAv);
        } else if (remoteAv.getUpdatedAt() > localAv.getUpdatedAt()) {
            remoteAv.setId(localAv.getId());
            avicultorDao.update(remoteAv);
        }
    }

    private void baixarLotesERegistros(String avicultorUuid, long localAvicultorId) throws ExecutionException, InterruptedException {
        QuerySnapshot lotesSnapshot = Tasks.await(firestore.collection("avicultores")
                .document(avicultorUuid)
                .collection("lotes")
                .get(Source.SERVER));

        for (QueryDocumentSnapshot loteDoc : lotesSnapshot) {
            LoteEntity remoteLote = loteDoc.toObject(LoteEntity.class);
            if (remoteLote != null) {
                remoteLote.setAvicultorId(localAvicultorId);
                long localLoteId = atualizarLoteLocalmente(remoteLote);
                baixarRegistros(avicultorUuid, remoteLote.getUuid(), localLoteId);
            }
        }
    }

    private long atualizarLoteLocalmente(LoteEntity remoteLote) {
        LoteEntity localLote = loteDao.getByUuidSemFiltro(remoteLote.getUuid());
        if (localLote == null) {
            return loteDao.insert(remoteLote);
        } else {
            if (remoteLote.getUpdatedAt() > localLote.getUpdatedAt()) {
                remoteLote.setId(localLote.getId());
                loteDao.update(remoteLote);
            }
            return localLote.getId();
        }
    }

    private void baixarRegistros(String avicultorUuid, String loteUuid, long localLoteId) throws ExecutionException, InterruptedException {
        QuerySnapshot registrosSnapshot = Tasks.await(firestore.collection("avicultores")
                .document(avicultorUuid)
                .collection("lotes")
                .document(loteUuid)
                .collection("registros")
                .get(Source.SERVER));

        for (QueryDocumentSnapshot regDoc : registrosSnapshot) {
            RegistroEntity remoteReg = regDoc.toObject(RegistroEntity.class);
            if (remoteReg != null) {
                remoteReg.setLoteId(localLoteId);
                atualizarRegistroLocalmente(remoteReg);
            }
        }
    }

    private void atualizarRegistroLocalmente(RegistroEntity remoteReg) {
        RegistroEntity localReg = registroDao.getByUuidSemFiltro(remoteReg.getUuid());
        if (localReg == null) {
            registroDao.insert(remoteReg);
        } else if (remoteReg.getUpdatedAt() > localReg.getUpdatedAt()) {
            remoteReg.setId(localReg.getId());
            registroDao.update(remoteReg);
        }
    }
}

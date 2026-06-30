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
    
    // ... lógica similar aplicada para Lotes e Registros no método baixarLotesERegistros
}

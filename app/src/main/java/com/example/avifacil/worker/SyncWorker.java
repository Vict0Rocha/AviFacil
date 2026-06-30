package com.example.avifacil.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.avifacil.data.local.database.AppDatabase;
import com.example.avifacil.data.repository.SyncRepository;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Worker do WorkManager para execução de sincronização em segundo plano.
 * 
 * Executa o fluxo bidirecional de dados:
 * 1. Upload de alterações locais (registros offline).
 * 2. Download de atualizações remotas (bloqueios, dados de outros dispositivos).
 */
public class SyncWorker extends Worker {
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        SyncRepository syncRepository = new SyncRepository(
                db.avicultorDao(),
                db.loteDao(),
                db.registroDao()
        );

        try {
            // 1. Enviar alterações locais para a nuvem
            syncRepository.sincronizarPendentes();

            // 2. Baixar dados da nuvem para o local (se houver usuário logado)
            String uid = FirebaseAuth.getInstance().getUid();
            if (uid != null) {
                syncRepository.baixarDados(uid);
            }

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.retry();
        }
    }
}

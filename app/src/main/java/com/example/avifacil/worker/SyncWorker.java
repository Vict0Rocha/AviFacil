package com.example.avifacil.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.avifacil.data.local.database.AppDatabase;
import com.example.avifacil.data.repository.SyncRepository;

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
            syncRepository.sincronizarPendentes();
            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}

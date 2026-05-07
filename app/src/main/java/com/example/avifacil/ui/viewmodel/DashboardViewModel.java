package com.example.avifacil.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.avifacil.data.local.database.AppDatabase;
import com.example.avifacil.data.local.entity.LoteEntity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardViewModel extends AndroidViewModel {
    private final AppDatabase db;
    private final ExecutorService executorService;

    private final MutableLiveData<DashboardData> dashboardData = new MutableLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<DashboardData> getDashboardData() {
        return dashboardData;
    }

    public void carregarDados(long avicultorId) {
        executorService.execute(() -> {
            try {
                int totalLotes = db.loteDao().countTotalLotes(avicultorId);
                int ativos = db.loteDao().countLotesPorStatus(avicultorId, com.example.avifacil.data.local.entity.StatusLote.ATIVO.name());
                int encerrados = db.loteDao().countLotesPorStatus(avicultorId, com.example.avifacil.data.local.entity.StatusLote.ENCERRADO.name());
                int avesAlojadas = db.loteDao().sumAvesAlojadas(avicultorId);
                int avesMortas = db.registroDao().getTotalAvesMortasGeral(avicultorId);
                List<LoteEntity> lotes = db.loteDao().getAllLotesDashboard(avicultorId);

                double mortalidade = avesAlojadas > 0 ? (avesMortas * 100.0) / avesAlojadas : 0;

                dashboardData.postValue(new DashboardData(
                    totalLotes, ativos, encerrados, avesAlojadas, avesMortas, mortalidade, lotes
                ));
            } catch (Exception e) {
                android.util.Log.e("DashboardVM", "Erro ao carregar dados", e);
                // Posta dados vazios para evitar crash na UI
                dashboardData.postValue(new DashboardData(0, 0, 0, 0, 0, 0, new java.util.ArrayList<>()));
            }
        });
    }

    public static class DashboardData {
        public final int totalLotes, ativos, encerrados, avesAlojadas, avesMortas;
        public final double mortalidadeGeral;
        public final List<LoteEntity> lotes;

        public DashboardData(int totalLotes, int ativos, int encerrados, int avesAlojadas, int avesMortas, double mortalidadeGeral, List<LoteEntity> lotes) {
            this.totalLotes = totalLotes;
            this.ativos = ativos;
            this.encerrados = encerrados;
            this.avesAlojadas = avesAlojadas;
            this.avesMortas = avesMortas;
            this.mortalidadeGeral = mortalidadeGeral;
            this.lotes = lotes;
        }
    }
}

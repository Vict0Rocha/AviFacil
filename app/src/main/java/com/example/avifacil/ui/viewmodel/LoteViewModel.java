package com.example.avifacil.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.avifacil.data.local.database.AppDatabase;
import com.example.avifacil.data.local.entity.LoteEntity;
import com.example.avifacil.data.repository.LoteRepository;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoteViewModel extends AndroidViewModel {
    private final LoteRepository repository;
    private final ExecutorService executorService;

    private final MutableLiveData<List<LoteEntity>> lotesAtivos = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> successMessage = new MutableLiveData<>();

    private final MutableLiveData<LoteEntity> loteAtual = new MutableLiveData<>();

    public LoteViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        repository = new LoteRepository(db.loteDao());
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<LoteEntity>> getLotesAtivos() {
        return lotesAtivos;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getSuccessAction() {
        return successMessage;
    }

    public LiveData<LoteEntity> getLoteAtual() {
        return loteAtual;
    }

    public void carregarLote(long id, long avicultorId) {
        executorService.execute(() -> {
            try {
                loteAtual.postValue(repository.getById(id, avicultorId));
            } catch (Exception e) {
                errorMessage.postValue("Erro ao carregar lote: " + e.getMessage());
            }
        });
    }

    public void carregarLotes(long avicultorId) {
        executorService.execute(() -> {
            try {
                lotesAtivos.postValue(repository.getLotesAtivosPorAvicultor(avicultorId));
            } catch (Exception e) {
                errorMessage.postValue("Erro ao carregar lotes: " + e.getMessage());
            }
        });
    }

    public void criarLote(long avicultorId, String avicultorUuid, String numero, String linhagem, String galpao, Date inicio, int qtdInicial, double pesoInicial, String observacoes) {
        if (numero == null || numero.trim().isEmpty()) {
            errorMessage.setValue("Número do lote é obrigatório");
            return;
        }
        executorService.execute(() -> {
            try {
                if (repository.existeNumeroLote(avicultorId, numero)) {
                    errorMessage.postValue("Este número de lote já existe para este avicultor");
                    return;
                }
                LoteEntity lote = new LoteEntity(avicultorId, avicultorUuid, numero, linhagem, galpao, inicio, qtdInicial, pesoInicial, observacoes);
                repository.insert(lote);
                successMessage.postValue(true);
                carregarLotes(avicultorId);
            } catch (Exception e) {
                errorMessage.postValue("Erro ao criar lote: " + e.getMessage());
            }
        });
    }

    public void encerrarLote(LoteEntity lote) {
        executorService.execute(() -> {
            try {
                lote.setStatus(com.example.avifacil.data.local.entity.StatusLote.ENCERRADO);
                repository.update(lote);
                successMessage.postValue(true);
                carregarLotes(lote.getAvicultorId());
            } catch (Exception e) {
                errorMessage.postValue("Erro ao encerrar lote: " + e.getMessage());
            }
        });
    }
}

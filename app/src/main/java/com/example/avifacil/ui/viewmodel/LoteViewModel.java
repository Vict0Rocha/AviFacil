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

    public void carregarLotes(long avicultorId) {
        executorService.execute(() -> {
            try {
                lotesAtivos.postValue(repository.getLotesAtivosPorAvicultor(avicultorId));
            } catch (Exception e) {
                errorMessage.postValue("Erro ao carregar lotes: " + e.getMessage());
            }
        });
    }

    public void criarLote(long avicultorId, String numero, Date inicio, int qtdInicial) {
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
                LoteEntity lote = new LoteEntity(avicultorId, numero, inicio, qtdInicial);
                repository.insert(lote);
                successMessage.postValue(true);
                carregarLotes(avicultorId);
            } catch (Exception e) {
                errorMessage.postValue("Erro ao criar lote: " + e.getMessage());
            }
        });
    }
}

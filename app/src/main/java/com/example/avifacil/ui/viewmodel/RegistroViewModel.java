package com.example.avifacil.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.avifacil.data.local.database.AppDatabase;
import com.example.avifacil.data.local.entity.RegistroEntity;
import com.example.avifacil.data.repository.RegistroRepository;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistroViewModel extends AndroidViewModel {
    private final RegistroRepository repository;
    private final ExecutorService executorService;

    private final MutableLiveData<List<RegistroEntity>> registrosLote = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> successMessage = new MutableLiveData<>();

    public RegistroViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        repository = new RegistroRepository(db.registroDao());
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<RegistroEntity>> getRegistrosLote() {
        return registrosLote;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getSuccessAction() {
        return successMessage;
    }

    public void carregarRegistros(long loteId) {
        executorService.execute(() -> {
            try {
                registrosLote.postValue(repository.getRegistrosPorLote(loteId));
            } catch (Exception e) {
                errorMessage.postValue("Erro ao carregar registros: " + e.getMessage());
            }
        });
    }

    public void adicionarRegistro(long loteId, Date data, int mortas, double racao) {
        executorService.execute(() -> {
            try {
                if (repository.existeRegistroNaData(loteId, data)) {
                    errorMessage.postValue("Já existe um registro para esta data");
                    return;
                }
                RegistroEntity registro = new RegistroEntity(loteId, data, mortas, racao);
                repository.insert(registro);
                successMessage.postValue(true);
                carregarRegistros(loteId);
            } catch (Exception e) {
                errorMessage.postValue("Erro ao salvar registro: " + e.getMessage());
            }
        });
    }
}

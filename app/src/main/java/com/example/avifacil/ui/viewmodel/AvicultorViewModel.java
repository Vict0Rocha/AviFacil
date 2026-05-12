package com.example.avifacil.ui.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.avifacil.R;
import com.example.avifacil.data.local.database.AppDatabase;
import com.example.avifacil.data.local.entity.AvicultorEntity;
import com.example.avifacil.data.repository.AvicultorRepository;
import com.example.avifacil.data.repository.SyncRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AvicultorViewModel extends AndroidViewModel {
    private final AvicultorRepository repository;
    private final SyncRepository syncRepository;
    private final ExecutorService executorService;

    private final MutableLiveData<List<AvicultorEntity>> avicultoresAtivos = new MutableLiveData<>();
    private final MutableLiveData<AvicultorEntity> avicultorLogado = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> successMessage = new MutableLiveData<>();

    public AvicultorViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        repository = new AvicultorRepository(db.avicultorDao());
        syncRepository = new SyncRepository(db.avicultorDao(), db.loteDao(), db.registroDao());
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<AvicultorEntity>> getAvicultoresAtivos() {
        return avicultoresAtivos;
    }

    public LiveData<AvicultorEntity> getAvicultorLogado() {
        return avicultorLogado;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getSuccessAction() {
        return successMessage;
    }

    public void carregarAvicultorPorUuid(String uuid) {
        executorService.execute(() -> {
            try {
                // Tenta carregar local primeiro
                AvicultorEntity avicultor = repository.getByUuid(uuid);
                
                if (avicultor == null) {
                    // Se não tem local, tenta baixar do Firestore
                    boolean encontrado = syncRepository.baixarDados(uuid);
                    if (encontrado) {
                        avicultor = repository.getByUuid(uuid);
                    } else {
                        // Não existe nem no Firestore, é uma conta realmente nova
                        errorMessage.postValue("Perfil não encontrado");
                        avicultorLogado.postValue(null);
                        return;
                    }
                }
                
                avicultorLogado.postValue(avicultor);
            } catch (Exception e) {
                Log.e("AvicultorViewModel", "Erro ao carregar dados", e);
                errorMessage.postValue("Erro ao carregar dados: " + e.getMessage());
                avicultorLogado.postValue(null);
            }
        });
    }

    public void salvarAvicultor(String nome, String email, String propriedade, String uuid) {
        if (nome == null || nome.trim().isEmpty()) {
            errorMessage.setValue(getApplication().getString(R.string.msg_erro_nome));
            return;
        }
        if (propriedade == null || propriedade.trim().isEmpty()) {
            errorMessage.setValue(getApplication().getString(R.string.msg_erro_propriedade));
            return;
        }
        executorService.execute(() -> {
            try {
                AvicultorEntity avicultor = new AvicultorEntity(nome, email, propriedade);
                if (uuid != null) {
                    avicultor.setUuid(uuid);
                }
                repository.insert(avicultor);
                successMessage.postValue(true);
            } catch (Exception e) {
                errorMessage.postValue("Erro ao salvar: " + e.getMessage());
            }
        });
    }
}

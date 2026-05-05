package com.example.avifacil.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.avifacil.R;
import com.example.avifacil.data.local.database.AppDatabase;
import com.example.avifacil.data.local.entity.AvicultorEntity;
import com.example.avifacil.data.repository.AvicultorRepository;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AvicultorViewModel extends AndroidViewModel {
    private final AvicultorRepository repository;
    private final ExecutorService executorService;

    private final MutableLiveData<List<AvicultorEntity>> avicultoresAtivos = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> successMessage = new MutableLiveData<>();

    public AvicultorViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        repository = new AvicultorRepository(db.avicultorDao());
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<AvicultorEntity>> getAvicultoresAtivos() {
        return avicultoresAtivos;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getSuccessAction() {
        return successMessage;
    }

    public void carregarAvicultores() {
        executorService.execute(() -> {
            try {
                List<AvicultorEntity> lista = repository.getAllAtivos();
                avicultoresAtivos.postValue(lista);
            } catch (Exception e) {
                errorMessage.postValue("Erro ao carregar: " + e.getMessage());
                // Em caso de erro crítico no banco, enviamos uma lista vazia 
                // para que o app siga para a tela de cadastro e tente se recuperar
                avicultoresAtivos.postValue(new java.util.ArrayList<>());
            }
        });
    }

    public void salvarAvicultor(String nome, String email, String propriedade) {
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
                repository.insert(avicultor);
                successMessage.postValue(true);
            } catch (Exception e) {
                errorMessage.postValue("Erro ao salvar: " + e.getMessage());
            }
        });
    }
}

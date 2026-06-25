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
                // 1. Tentar carregar local primeiro para resposta rápida (Offline-first)
                AvicultorEntity local = repository.getByUuid(uuid);
                if (local != null) {
                    avicultorLogado.postValue(local);
                }

                // 2. Tentar atualizar dados do servidor em background (não bloqueia a UI se estiver offline)
                try {
                    syncRepository.baixarDados(uuid);
                    
                    // Recarrega após baixar para ver se mudou algo (ex: status de bloqueio remoto)
                    AvicultorEntity atualizado = repository.getByUuid(uuid);
                    if (atualizado != null) {
                        // Só postamos novamente se houver mudança relevante ou se não tínhamos nada antes
                        if (local == null || !atualizado.getStatus().equals(local.getStatus()) || atualizado.isBloqueado() != local.isBloqueado()) {
                            avicultorLogado.postValue(atualizado);
                        }
                    }
                } catch (Exception e) {
                    Log.w("AvicultorViewModel", "Falha ao baixar dados remotos, mantendo locais: " + e.getMessage());
                }

                // 3. Se não tem nada local nem remoto após a tentativa
                if (local == null && avicultorLogado.getValue() == null) {
                    // Verificamos novamente o DB pois baixarDados pode ter inserido algo
                    AvicultorEntity finalCheck = repository.getByUuid(uuid);
                    if (finalCheck == null) {
                        errorMessage.postValue("Perfil não encontrado");
                        avicultorLogado.postValue(null);
                    } else {
                        avicultorLogado.postValue(finalCheck);
                    }
                }
            } catch (Exception e) {
                Log.e("AvicultorViewModel", "Erro fatal ao carregar dados", e);
                errorMessage.postValue("Erro ao carregar dados: " + e.getMessage());
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
                AvicultorEntity existente = repository.getByUuid(uuid);
                if (existente != null) {
                    existente.setNome(nome);
                    existente.setNomePropriedade(propriedade);
                    existente.setPerfilCompleto(true);
                    existente.setSincronizado(false);
                    existente.setUpdatedAt(System.currentTimeMillis());
                    repository.update(existente);
                } else {
                    AvicultorEntity avicultor = new AvicultorEntity(nome, email, propriedade);
                    if (uuid != null) {
                        avicultor.setUuid(uuid);
                    }
                    avicultor.setPerfilCompleto(true);
                    repository.insert(avicultor);
                }
                successMessage.postValue(true);
            } catch (Exception e) {
                errorMessage.postValue("Erro ao salvar: " + e.getMessage());
            }
        });
    }

    public void atualizarSenha(String novaSenha, String confirmarSenha) {
        if (novaSenha.isEmpty() || novaSenha.length() < 6) {
            errorMessage.setValue(getApplication().getString(R.string.msg_erro_senha_curta));
            return;
        }
        if (!novaSenha.equals(confirmarSenha)) {
            errorMessage.setValue(getApplication().getString(R.string.msg_erro_senhas_diferentes));
            return;
        }

        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.updatePassword(novaSenha).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    successMessage.postValue(true);
                } else {
                    errorMessage.setValue("Erro ao alterar senha: " + task.getException().getMessage());
                }
            });
        }
    }
}

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

    private final MutableLiveData<RegistroEntity> registroParaEdicao = new MutableLiveData<>();

    private final AppDatabase db;

    public RegistroViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        repository = new RegistroRepository(db.registroDao());
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<RegistroEntity>> getRegistrosLote() {
        return registrosLote;
    }

    public LiveData<RegistroEntity> getRegistroParaEdicao() {
        return registroParaEdicao;
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

    public void carregarRegistro(long id) {
        executorService.execute(() -> {
            try {
                registroParaEdicao.postValue(repository.getById(id));
            } catch (Exception e) {
                errorMessage.postValue("Erro ao carregar registro: " + e.getMessage());
            }
        });
    }

    public void adicionarRegistro(long loteId, String loteUuid, Date data, int mortas, double racao, double peso, double precoInsumo, String tipoInsumo, String observacoes) {
        executorService.execute(() -> {
            try {
                if (repository.existeRegistroNaData(loteId, data)) {
                    errorMessage.postValue("Já existe um registro para esta data");
                    return;
                }

                RegistroEntity ultimo = repository.getUltimoRegistro(loteId);
                if (ultimo != null && !data.after(ultimo.getDataRegistro())) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                    errorMessage.postValue("A data do novo registro deve ser posterior ao último registro (" + sdf.format(ultimo.getDataRegistro()) + ")");
                    return;
                }

                com.example.avifacil.data.local.entity.LoteEntity lote = db.loteDao().getByIdSemFiltro(loteId);
                if (lote != null) {
                    if (data.before(lote.getDataInicio())) {
                        errorMessage.postValue("A data do registro não pode ser anterior ao alojamento");
                        return;
                    }

                    int totalMortasAtual = repository.getTotalAvesMortas(loteId);
                    if (totalMortasAtual + mortas > lote.getQuantidadeAvesInicial()) {
                        errorMessage.postValue("A mortalidade total não pode exceder a quantidade inicial de aves (" + lote.getQuantidadeAvesInicial() + ")");
                        return;
                    }
                }

                RegistroEntity registro = new RegistroEntity(loteId, loteUuid, data, mortas, racao, peso, precoInsumo, tipoInsumo);
                registro.setObservacoes(observacoes);
                repository.insert(registro);
                
                atualizarEstatisticasLote(loteId);
                
                successMessage.postValue(true);
                carregarRegistros(loteId);
            } catch (Exception e) {
                errorMessage.postValue("Erro ao salvar registro: " + e.getMessage());
            }
        });
    }

    public void editarRegistro(long id, Date data, int mortas, double racao, double peso, double precoInsumo, String tipoInsumo, String observacoes) {
        executorService.execute(() -> {
            try {
                RegistroEntity registro = repository.getById(id);
                if (registro != null) {
                    if (!registro.getDataRegistro().equals(data) && repository.existeRegistroNaData(registro.getLoteId(), data)) {
                        errorMessage.postValue("Já existe um registro para esta nova data");
                        return;
                    }

                    com.example.avifacil.data.local.entity.LoteEntity lote = db.loteDao().getByIdSemFiltro(registro.getLoteId());
                    if (lote != null) {
                        int totalMortasOutros = repository.getTotalAvesMortas(lote.getId()) - registro.getAvesMortasPeriodo();
                        if (totalMortasOutros + mortas > lote.getQuantidadeAvesInicial()) {
                            errorMessage.postValue("A mortalidade total não pode exceder a quantidade inicial de aves (" + lote.getQuantidadeAvesInicial() + ")");
                            return;
                        }
                    }

                    registro.setDataRegistro(data);
                    registro.setAvesMortasPeriodo(mortas);
                    registro.setConsumoRacaoPeriodo(racao);
                    registro.setPesoAtualMedio(peso);
                    registro.setPrecoKgInsumo(precoInsumo);
                    registro.setTipoInsumo(tipoInsumo);
                    registro.setObservacoes(observacoes);
                    repository.update(registro);
                    
                    atualizarEstatisticasLote(registro.getLoteId());
                    
                    successMessage.postValue(true);
                    carregarRegistros(registro.getLoteId());
                }
            } catch (Exception e) {
                errorMessage.postValue("Erro ao editar registro: " + e.getMessage());
            }
        });
    }

    public void excluirRegistro(long id) {
        executorService.execute(() -> {
            try {
                RegistroEntity registro = repository.getById(id);
                if (registro != null) {
                    long loteId = registro.getLoteId();
                    repository.softDelete(id);
                    
                    atualizarEstatisticasLote(loteId);
                    
                    successMessage.postValue(true);
                    carregarRegistros(loteId);
                }
            } catch (Exception e) {
                errorMessage.postValue("Erro ao excluir registro: " + e.getMessage());
            }
        });
    }

    private void atualizarEstatisticasLote(long loteId) {
        com.example.avifacil.data.local.entity.LoteEntity lote = db.loteDao().getByIdSemFiltro(loteId);
        if (lote == null) return;

        List<RegistroEntity> registros = repository.getRegistrosPorLote(loteId);
        
        double pesoMedio = com.example.avifacil.util.ZootecniaCalculator.calcularPesoMedioAtualKg(registros);
        double ca = com.example.avifacil.util.ZootecniaCalculator.calcularConversaoAlimentar(lote, registros);
        
        lote.setPesoAtualMedio(pesoMedio);
        lote.setConversaoAlimentar(ca);
        lote.setUpdatedAt(System.currentTimeMillis());
        lote.setSincronizado(false);

        db.loteDao().update(lote);
    }
}

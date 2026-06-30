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

/**
 * ViewModel para gestão dos registros técnicos diários.
 * 
 * Além do CRUD de registros, é responsável por disparar a atualização
 * dos indicadores consolidados no LoteEntity (denormalização para performance)
 * e garantir a integridade dos dados zootécnicos.
 */
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
                errorMessage.postValue(getApplication().getString(com.example.avifacil.R.string.msg_erro_carregar_dados));
            }
        });
    }

    public void carregarRegistro(long id) {
        executorService.execute(() -> {
            try {
                registroParaEdicao.postValue(repository.getById(id));
            } catch (Exception e) {
                errorMessage.postValue(getApplication().getString(com.example.avifacil.R.string.msg_erro_carregar_dados));
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
                    errorMessage.postValue(getApplication().getString(com.example.avifacil.R.string.msg_erro_data_registro_sequencia));
                    return;
                }

                com.example.avifacil.data.local.entity.LoteEntity lote = db.loteDao().getByIdSemFiltro(loteId);
                if (lote != null) {
                    if (data.before(lote.getDataInicio())) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                        errorMessage.postValue(getApplication().getString(com.example.avifacil.R.string.msg_erro_data_lote, sdf.format(lote.getDataInicio())));
                        return;
                    }

                    int totalMortasAtual = repository.getTotalAvesMortas(loteId);
                    if (totalMortasAtual + mortas > lote.getQuantidadeAvesInicial()) {
                        errorMessage.postValue(getApplication().getString(com.example.avifacil.R.string.msg_erro_mortalidade_excedida));
                        return;
                    }
                }

                RegistroEntity registro = new RegistroEntity(loteId, loteUuid, data, mortas, racao, peso, precoInsumo, tipoInsumo);
                registro.setObservacoes(observacoes);
                repository.insert(registro);
                
                atualizarEstatisticasLote(loteId);
                
                // Dispara sincronização automática
                com.example.avifacil.util.SyncManager.enviarDados(getApplication());
                
                successMessage.postValue(true);
                carregarRegistros(loteId);
            } catch (Exception e) {
                errorMessage.postValue(getApplication().getString(com.example.avifacil.R.string.msg_erro_salvar_dados));
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
                            errorMessage.postValue(getApplication().getString(com.example.avifacil.R.string.msg_erro_mortalidade_excedida));
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
                    registro.setUpdatedAt(System.currentTimeMillis());
                    registro.setSincronizado(false);
                    repository.update(registro);
                    
                    atualizarEstatisticasLote(registro.getLoteId());
                    
                    // Dispara sincronização automática
                    com.example.avifacil.util.SyncManager.enviarDados(getApplication());
                    
                    successMessage.postValue(true);
                    carregarRegistros(registro.getLoteId());
                }
            } catch (Exception e) {
                errorMessage.postValue(getApplication().getString(com.example.avifacil.R.string.msg_erro_salvar_dados));
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
                    
                    // Dispara sincronização automática
                    com.example.avifacil.util.SyncManager.enviarDados(getApplication());
                    
                    successMessage.postValue(true);
                    carregarRegistros(loteId);
                }
            } catch (Exception e) {
                errorMessage.postValue(getApplication().getString(com.example.avifacil.R.string.msg_erro_excluir));
            }
        });
    }

    private void atualizarEstatisticasLote(long loteId) {
        com.example.avifacil.data.local.entity.LoteEntity lote = db.loteDao().getByIdSemFiltro(loteId);
        if (lote == null) return;

        List<RegistroEntity> registros = repository.getRegistrosPorLote(loteId);
        if (registros == null || registros.isEmpty()) {
            lote.setPesoAtualMedio(0);
            lote.setConversaoAlimentar(0);
            lote.setMortalidadeAcumulada(0);
            lote.setViabilidade(100.0);
            lote.setGpd(0);
            lote.setIep(0);
        } else {
            Date dataUltimoRegistro = registros.get(registros.size() - 1).getDataRegistro();
            
            lote.setPesoAtualMedio(com.example.avifacil.util.ZootecniaCalculator.calcularPesoMedioAtualKg(registros));
            lote.setConversaoAlimentar(com.example.avifacil.util.ZootecniaCalculator.calcularConversaoAlimentar(lote, registros));
            lote.setMortalidadeAcumulada(com.example.avifacil.util.ZootecniaCalculator.calcularMortalidadeAcumuladaPercentual(lote, registros));
            lote.setViabilidade(com.example.avifacil.util.ZootecniaCalculator.calcularViabilidadePercentual(lote, registros));
            lote.setGpd(com.example.avifacil.util.ZootecniaCalculator.calcularGPD(lote, registros, dataUltimoRegistro));
            lote.setIep(com.example.avifacil.util.ZootecniaCalculator.calcularFatorProducao(lote, registros, dataUltimoRegistro));
        }

        lote.setUpdatedAt(System.currentTimeMillis());
        lote.setSincronizado(false);

        db.loteDao().update(lote);
    }
}

package com.example.avifacil.ui.registro;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.avifacil.R;
import com.example.avifacil.ui.viewmodel.AvicultorViewModel;
import com.example.avifacil.ui.viewmodel.RegistroViewModel;
import com.example.avifacil.util.NumberParser;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * TRATAMENTO DE EVENTOS E LÓGICA DE UI (JAVA):
 * Esta Activity gerencia a interação do usuário ao cadastrar um novo registro.
 * Ela implementa ouvintes de clique (ClickListeners), observadores de dados (LiveData)
 * e integração com o ViewModel para persistência.
 */
public class CadastroRegistroActivity extends AppCompatActivity {

    // Componentes de UI vinculados ao layout XML
    private TextInputEditText editData, editMortas, editConsumo, editPeso, editObs;
    private TextInputEditText editPrecoInsumo;
    private com.google.android.material.textfield.TextInputLayout layoutData, layoutMortas, layoutConsumo, layoutPeso;
    private com.google.android.material.textfield.TextInputLayout layoutPrecoInsumo;
    private android.view.View btnSalvar; // Alterado para View genérica para compatibilidade com o layout de Button customizado
    
    // ViewModels: Responsáveis por manter os dados e sobreviver a mudanças de configuração
    private RegistroViewModel viewModel;
    private com.example.avifacil.ui.viewmodel.LoteViewModel loteViewModel;
    private AvicultorViewModel avicultorViewModel;
    
    // Utilitários de data
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    
    private long loteId = -1;
    private String loteUuid = null;
    private long registroId = -1;
    private Date dataAlojamento;
    private int lastCheckedId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_registro);

        // Tratamento de Edge-to-Edge: Ajusta o preenchimento para não sobrepor barras de sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });

        // Recuperação de parâmetros enviados pela tela anterior (Intent)
        loteId = getIntent().getLongExtra("LOTE_ID", -1);
        loteUuid = getIntent().getStringExtra("LOTE_UUID");
        registroId = getIntent().getLongExtra("REGISTRO_ID", -1);

        if (loteId == -1 && registroId == -1) {
            finish();
            return;
        }

        initViews();

        // Inicialização dos ViewModels seguindo o padrão MVVM
        viewModel = new ViewModelProvider(this).get(RegistroViewModel.class);
        loteViewModel = new ViewModelProvider(this).get(com.example.avifacil.ui.viewmodel.LoteViewModel.class);
        avicultorViewModel = new ViewModelProvider(this).get(AvicultorViewModel.class);

        // EVENTO: Observa quando o avicultor logado é carregado para então carregar os dados do lote
        avicultorViewModel.getAvicultorLogado().observe(this, avicultor -> {
            if (avicultor != null) {
                loteViewModel.carregarLote(loteId, avicultor.getId());
            }
        });

        // EVENTO: Se o lote estiver encerrado, desabilita a edição (Regra de Negócio)
        loteViewModel.getLoteAtual().observe(this, lote -> {
            if (lote != null) {
                dataAlojamento = lote.getDataInicio();
                if (lote.getStatus() == com.example.avifacil.data.local.entity.StatusLote.ENCERRADO) {
                    desabilitarEdicao();
                }
            }
        });

        // Caso seja edição, busca os dados do registro existente
        if (registroId != -1) {
            android.widget.TextView txtTitle = findViewById(R.id.txtTitleCadastroRegistro);
            if (txtTitle != null) txtTitle.setText(R.string.title_edit_registro);
            viewModel.carregarRegistro(registroId);
        }

        // Recupera o ID do usuário do Firebase Auth
        if (loteId != -1) {
            String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
            if (currentUid != null) {
                avicultorViewModel.carregarAvicultorPorUuid(currentUid);
            }
        }

        // EVENTO: Clique no campo de data abre o seletor de calendário (DatePicker)
        editData.setOnClickListener(v -> showDatePicker());
        
        // EVENTO: Clique no botão salvar dispara a validação e persistência
        btnSalvar.setOnClickListener(v -> salvarRegistro());

        observeViewModel();
    }

    private void initViews() {
        editData = findViewById(R.id.editDataRegistro);
        editMortas = findViewById(R.id.editMortas);
        editConsumo = findViewById(R.id.editConsumo);
        editPeso = findViewById(R.id.editPesoAtual);
        editObs = findViewById(R.id.editObs);
        editPrecoInsumo = findViewById(R.id.editPrecoInsumo);

        // EVENTO: Tratamento manual de RadioButtons para permitir "desmarcar" clicando novamente
        findViewById(R.id.radioMilho).setOnClickListener(v -> toggleRadio(R.id.radioMilho));
        findViewById(R.id.radioSoja).setOnClickListener(v -> toggleRadio(R.id.radioSoja));
        findViewById(R.id.radioNucleo).setOnClickListener(v -> toggleRadio(R.id.radioNucleo));
        findViewById(R.id.radioOutro).setOnClickListener(v -> toggleRadio(R.id.radioOutro));

        btnSalvar = findViewById(R.id.btnSalvarRegistro);

        layoutData = findViewById(R.id.inputLayoutDataRegistro);
        layoutMortas = findViewById(R.id.inputLayoutMortas);
        layoutConsumo = findViewById(R.id.inputLayoutConsumo);
        layoutPeso = findViewById(R.id.inputLayoutPesoAtual);
        layoutPrecoInsumo = findViewById(R.id.inputLayoutPrecoInsumo);
        
        atualizarLabelData();
    }

    /**
     * Observa mudanças no ViewModel para atualizar a interface em tempo real.
     */
    private void observeViewModel() {
        viewModel.getRegistroParaEdicao().observe(this, registro -> {
            if (registro != null) {
                // Preenche os campos com os dados carregados do banco (Room)
                calendar.setTime(registro.getDataRegistro());
                atualizarLabelData();
                editMortas.setText(String.valueOf(registro.getAvesMortasPeriodo()));
                editConsumo.setText(NumberParser.formatDouble(registro.getConsumoRacaoPeriodo()));
                editPeso.setText(NumberParser.formatDouble(registro.getPesoAtualMedio()));
                editPrecoInsumo.setText(NumberParser.formatDouble(registro.getPrecoKgInsumo()));
                setTipoInsumoRadio(registro.getTipoInsumo());
                editObs.setText(registro.getObservacoes());
            }
        });

        // EVENTO: Notificação de sucesso ao salvar
        viewModel.getSuccessAction().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, R.string.msg_sucesso_registro, Toast.LENGTH_SHORT).show();
                finish(); // Fecha a tela após o sucesso
            }
        });
    }

    private void showDatePicker() {
        // Exibe o diálogo nativo do Android para seleção de data
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            atualizarLabelData();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void atualizarLabelData() {
        editData.setText(dateFormat.format(calendar.getTime()));
    }

    /**
     * Lógica de Validação e Persistência
     */
    private void salvarRegistro() {
        // Captura e sanitiza as entradas do usuário
        String mortasStr = editMortas.getText().toString().trim();
        String consumoStr = editConsumo.getText().toString().trim();
        String pesoStr = editPeso.getText().toString().trim();
        String precoInsumoStr = editPrecoInsumo.getText().toString().trim();
        Date data = calendar.getTime();

        // Validação de Regra de Negócio: Registro não pode ser anterior ao início do lote
        if (dataAlojamento != null && data.before(dataAlojamento)) {
            layoutData.setError(getString(R.string.msg_erro_data_lote, dateFormat.format(dataAlojamento)));
            return;
        }

        // Valida campos obrigatórios
        if (mortasStr.isEmpty()) { layoutMortas.setError(getString(R.string.msg_erro_mortas)); return; }
        if (pesoStr.isEmpty()) { layoutPeso.setError(getString(R.string.msg_erro_peso_atual)); return; }
        if (consumoStr.isEmpty()) { layoutConsumo.setError(getString(R.string.msg_erro_consumo)); return; }
        if (precoInsumoStr.isEmpty()) precoInsumoStr = "0";

        // Conversão usando NumberParser para aceitar o padrão brasileiro (vírgula)
        int mortas = Integer.parseInt(mortasStr);
        double consumo = NumberParser.parseDouble(consumoStr);
        double peso = NumberParser.parseDouble(pesoStr);
        double precoInsumo = NumberParser.parseDouble(precoInsumoStr);
        String tipoInsumo = getTipoInsumoSelecionado();

        // Persistência delegada ao ViewModel (Segue o princípio de responsabilidade única)
        if (registroId == -1) {
            viewModel.adicionarRegistro(loteId, loteUuid, data, mortas, consumo, peso, precoInsumo, tipoInsumo, editObs.getText().toString());
        } else {
            viewModel.editarRegistro(registroId, data, mortas, consumo, peso, precoInsumo, tipoInsumo, editObs.getText().toString());
        }
    }

    // Auxiliares para manipulação dos RadioButtons
    private void toggleRadio(int id) {
        android.widget.RadioButton rb = findViewById(id);
        if (rb == null) return;
        if (lastCheckedId == id) {
            uncheckAllRadios();
            lastCheckedId = -1;
        } else {
            uncheckAllRadios();
            rb.setChecked(true);
            lastCheckedId = id;
        }
    }

    private void uncheckAllRadios() {
        int[] ids = {R.id.radioMilho, R.id.radioSoja, R.id.radioNucleo, R.id.radioOutro};
        for (int id : ids) {
            android.widget.RadioButton rb = findViewById(id);
            if (rb != null) rb.setChecked(false);
        }
    }

    private String getTipoInsumoSelecionado() {
        if (lastCheckedId == R.id.radioMilho) return "milho";
        if (lastCheckedId == R.id.radioSoja) return "soja";
        if (lastCheckedId == R.id.radioNucleo) return "núcleo";
        if (lastCheckedId == R.id.radioOutro) return "outro";
        return null;
    }

    private void desabilitarEdicao() {
        btnSalvar.setVisibility(android.view.View.GONE);
        editMortas.setEnabled(false);
        // ... desabilita demais campos
    }
}

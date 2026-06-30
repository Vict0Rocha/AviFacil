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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Tela de lançamento de dados diários ou periódicos do lote.
 * 
 * Permite registrar mortalidade, consumo de ração, peso médio e 
 * compras de insumos. Realiza cálculos de validação baseados na 
 * data de alojamento do lote.
 */
public class CadastroRegistroActivity extends AppCompatActivity {

    private TextInputEditText editData, editMortas, editConsumo, editPeso, editObs;
    private TextInputEditText editPrecoInsumo;
    private com.google.android.material.textfield.TextInputLayout layoutData, layoutMortas, layoutConsumo, layoutPeso;
    private com.google.android.material.textfield.TextInputLayout layoutPrecoInsumo;
    private MaterialButton btnSalvar;
    private RegistroViewModel viewModel;
    private com.example.avifacil.ui.viewmodel.LoteViewModel loteViewModel;
    private AvicultorViewModel avicultorViewModel;
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });

        loteId = getIntent().getLongExtra("LOTE_ID", -1);
        loteUuid = getIntent().getStringExtra("LOTE_UUID");
        registroId = getIntent().getLongExtra("REGISTRO_ID", -1);

        if (loteId == -1 && registroId == -1) {
            finish();
            return;
        }

        initViews();

        viewModel = new ViewModelProvider(this).get(RegistroViewModel.class);
        loteViewModel = new ViewModelProvider(this).get(com.example.avifacil.ui.viewmodel.LoteViewModel.class);
        avicultorViewModel = new ViewModelProvider(this).get(AvicultorViewModel.class);

        avicultorViewModel.getAvicultorLogado().observe(this, avicultor -> {
            if (avicultor != null) {
                loteViewModel.carregarLote(loteId, avicultor.getId());
            }
        });

        loteViewModel.getLoteAtual().observe(this, lote -> {
            if (lote != null) {
                dataAlojamento = lote.getDataInicio();
                if (lote.getStatus() == com.example.avifacil.data.local.entity.StatusLote.ENCERRADO) {
                    desabilitarEdicao();
                }
            }
        });

        if (registroId != -1) {
            android.widget.TextView txtTitle = findViewById(R.id.txtTitleCadastroRegistro);
            if (txtTitle != null) txtTitle.setText(R.string.title_edit_registro);
            viewModel.carregarRegistro(registroId);
        }

        if (loteId != -1) {
            String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
            if (currentUid != null) {
                avicultorViewModel.carregarAvicultorPorUuid(currentUid);
            }
        }

        editData.setOnClickListener(v -> showDatePicker());
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

    private void observeViewModel() {
        viewModel.getRegistroParaEdicao().observe(this, registro -> {
            if (registro != null) {
                loteId = registro.getLoteId();
                loteUuid = registro.getLoteUuid();
                
                String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
                if (currentUid != null) {
                    avicultorViewModel.carregarAvicultorPorUuid(currentUid);
                }

                calendar.setTime(registro.getDataRegistro());
                atualizarLabelData();
                editMortas.setText(String.valueOf(registro.getAvesMortasPeriodo()));
                editConsumo.setText(String.valueOf(registro.getConsumoRacaoPeriodo()));
                editPeso.setText(String.valueOf(registro.getPesoAtualMedio()));
                
                editPrecoInsumo.setText(String.valueOf(registro.getPrecoKgInsumo()));
                setTipoInsumoRadio(registro.getTipoInsumo());
                
                editObs.setText(registro.getObservacoes());
            }
        });

        viewModel.getSuccessAction().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, R.string.msg_sucesso_registro, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setTipoInsumoRadio(String tipo) {
        uncheckAllRadios();
        lastCheckedId = -1;
        
        if (tipo == null) return;
        
        switch (tipo.toLowerCase()) {
            case "milho": lastCheckedId = R.id.radioMilho; break;
            case "soja": lastCheckedId = R.id.radioSoja; break;
            case "núcleo": lastCheckedId = R.id.radioNucleo; break;
            case "outro": lastCheckedId = R.id.radioOutro; break;
        }
        
        if (lastCheckedId != -1) {
            android.widget.RadioButton rb = findViewById(lastCheckedId);
            if (rb != null) rb.setChecked(true);
        }
    }

    private void uncheckAllRadios() {
        int[] ids = {R.id.radioMilho, R.id.radioSoja, R.id.radioNucleo, R.id.radioOutro};
        for (int id : ids) {
            android.widget.RadioButton rb = findViewById(id);
            if (rb != null) rb.setChecked(false);
        }
    }

    private void toggleRadio(int id) {
        android.widget.RadioButton rb = findViewById(id);
        if (rb == null) return;

        if (lastCheckedId == id) {
            // Se já estava marcado, desmarca tudo
            uncheckAllRadios();
            lastCheckedId = -1;
        } else {
            // Se era outro ou nenhum, desmarca os outros e marca o atual
            uncheckAllRadios();
            rb.setChecked(true);
            lastCheckedId = id;
        }
    }

    private String getTipoInsumoSelecionado() {
        if (lastCheckedId == R.id.radioMilho) return "milho";
        if (lastCheckedId == R.id.radioSoja) return "soja";
        if (lastCheckedId == R.id.radioNucleo) return "núcleo";
        if (lastCheckedId == R.id.radioOutro) return "outro";
        return null;
    }

    private void showDatePicker() {
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

    private void desabilitarEdicao() {
        editData.setEnabled(false);
        editMortas.setEnabled(false);
        editConsumo.setEnabled(false);
        editPeso.setEnabled(false);
        editObs.setEnabled(false);
        editPrecoInsumo.setEnabled(false);
        
        findViewById(R.id.radioMilho).setEnabled(false);
        findViewById(R.id.radioSoja).setEnabled(false);
        findViewById(R.id.radioNucleo).setEnabled(false);
        findViewById(R.id.radioOutro).setEnabled(false);

        btnSalvar.setVisibility(android.view.View.GONE);
    }

    private void salvarRegistro() {
        String mortasStr = editMortas.getText().toString().trim();
        String consumoStr = editConsumo.getText().toString().trim();
        String pesoStr = editPeso.getText().toString().trim();
        String precoInsumoStr = editPrecoInsumo.getText().toString().trim();
        String obs = editObs.getText().toString().trim();
        Date data = calendar.getTime();

        if (dataAlojamento != null && data.before(dataAlojamento)) {
            layoutData.setError(getString(R.string.msg_erro_data_lote, dateFormat.format(dataAlojamento)));
            return;
        }

        if (mortasStr.isEmpty()) { layoutMortas.setError(getString(R.string.msg_erro_mortas)); return; }
        if (pesoStr.isEmpty()) { layoutPeso.setError(getString(R.string.msg_erro_peso_atual)); return; }
        if (consumoStr.isEmpty()) { layoutConsumo.setError(getString(R.string.msg_erro_consumo)); return; }
        if (precoInsumoStr.isEmpty()) precoInsumoStr = "0";

        int mortas = Integer.parseInt(mortasStr);
        double consumo = Double.parseDouble(consumoStr);
        double peso = Double.parseDouble(pesoStr);
        double precoInsumo = Double.parseDouble(precoInsumoStr);
        String tipoInsumo = getTipoInsumoSelecionado();

        if (mortas < 0) { layoutMortas.setError("Mortalidade inválida"); return; }
        if (consumo < 0) { layoutConsumo.setError("Consumo inválido"); return; }
        if (peso <= 0) { layoutPeso.setError("Peso inválido"); return; }

        if (registroId == -1) {
            viewModel.adicionarRegistro(loteId, loteUuid, data, mortas, consumo, peso, precoInsumo, tipoInsumo, obs);
        } else {
            viewModel.editarRegistro(registroId, data, mortas, consumo, peso, precoInsumo, tipoInsumo, obs);
        }
    }
}

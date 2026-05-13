package com.example.avifacil.ui.registro;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

public class CadastroRegistroActivity extends AppCompatActivity {

    private TextInputEditText editData, editMortas, editConsumo, editPeso, editObs;
    private com.google.android.material.textfield.TextInputLayout layoutData, layoutMortas, layoutConsumo, layoutPeso;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_registro);

        // Ajuste para bordas infinitas (Edge-to-Edge)
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

        // Observa o avicultor para carregar o lote com segurança
        avicultorViewModel.getAvicultorLogado().observe(this, avicultor -> {
            if (avicultor != null) {
                loteViewModel.carregarLote(loteId, avicultor.getId());
            }
        });

        // Busca data de alojamento do lote para validação
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

        // Se tivermos o loteId, carregamos o avicultor para disparar o fluxo
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
        btnSalvar = findViewById(R.id.btnSalvarRegistro);

        layoutData = findViewById(R.id.inputLayoutDataRegistro);
        layoutMortas = findViewById(R.id.inputLayoutMortas);
        layoutConsumo = findViewById(R.id.inputLayoutConsumo);
        layoutPeso = findViewById(R.id.inputLayoutPesoAtual);
        
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
        btnSalvar.setVisibility(android.view.View.GONE);
        Toast.makeText(this, "Lote encerrado. Não é possível alterar registros.", Toast.LENGTH_SHORT).show();
    }

    private void salvarRegistro() {
        String mortasStr = editMortas.getText().toString().trim();
        String consumoStr = editConsumo.getText().toString().trim();
        String pesoStr = editPeso.getText().toString().trim();
        String obs = editObs.getText().toString().trim();
        Date data = calendar.getTime();

        if (dataAlojamento != null && data.before(dataAlojamento)) {
            layoutData.setError(getString(R.string.msg_erro_data_lote, dateFormat.format(dataAlojamento)));
            return;
        } else {
            layoutData.setError(null);
        }

        if (mortasStr.isEmpty()) {
            layoutMortas.setError(getString(R.string.msg_erro_mortas));
            return;
        } else {
            layoutMortas.setError(null);
        }

        if (pesoStr.isEmpty()) {
            layoutPeso.setError(getString(R.string.msg_erro_peso_atual));
            return;
        } else {
            layoutPeso.setError(null);
        }

        if (consumoStr.isEmpty()) {
            layoutConsumo.setError(getString(R.string.msg_erro_consumo));
            return;
        } else {
            layoutConsumo.setError(null);
        }

        int mortas = Integer.parseInt(mortasStr);
        double consumo = Double.parseDouble(consumoStr);
        double peso = Double.parseDouble(pesoStr);

        if (registroId == -1) {
            viewModel.adicionarRegistro(loteId, loteUuid, data, mortas, consumo, peso, obs);
        } else {
            viewModel.editarRegistro(registroId, data, mortas, consumo, peso, obs);
        }
    }
}

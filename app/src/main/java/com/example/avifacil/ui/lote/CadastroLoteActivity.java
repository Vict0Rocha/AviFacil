package com.example.avifacil.ui.lote;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.avifacil.R;
import com.example.avifacil.ui.viewmodel.LoteViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CadastroLoteActivity extends AppCompatActivity {

    private TextInputLayout layoutNumero, layoutLinhagem, layoutData, layoutQtd, layoutPeso;
    private TextInputEditText editNumero, editLinhagem, editGalpao, editData, editQtd, editPeso, editObs;
    private LoteViewModel viewModel;
    private Calendar calendar = Calendar.getInstance();
    private long avicultorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_lote);

        avicultorId = getIntent().getLongExtra("AVICULTOR_ID", -1);
        if (avicultorId == -1) {
            finish();
            return;
        }

        initViews();
        viewModel = new ViewModelProvider(this).get(LoteViewModel.class);

        editData.setOnClickListener(v -> showDatePicker());
        findViewById(R.id.btnCriarLote).setOnClickListener(v -> salvarLote());

        observeViewModel();
    }

    private void initViews() {
        layoutNumero = findViewById(R.id.inputLayoutNumeroLote);
        layoutLinhagem = findViewById(R.id.inputLayoutLinhagem);
        layoutData = findViewById(R.id.inputLayoutDataInicio);
        layoutQtd = findViewById(R.id.inputLayoutQtdAves);
        layoutPeso = findViewById(R.id.inputLayoutPesoInicial);

        editNumero = findViewById(R.id.editNumeroLote);
        editLinhagem = findViewById(R.id.editLinhagem);
        editGalpao = findViewById(R.id.editGalpao);
        editData = findViewById(R.id.editDataInicio);
        editQtd = findViewById(R.id.editQtdAves);
        editPeso = findViewById(R.id.editPesoInicial);
        editObs = findViewById(R.id.editObservacoes);

        updateLabel();
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateLabel() {
        String format = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        editData.setText(sdf.format(calendar.getTime()));
    }

    private void salvarLote() {
        String numero = editNumero.getText().toString().trim();
        String linhagem = editLinhagem.getText().toString().trim();
        String galpao = editGalpao.getText().toString().trim();
        String qtdStr = editQtd.getText().toString().trim();
        String pesoStr = editPeso.getText().toString().trim();
        String obs = editObs.getText().toString().trim();
        Date dataInicio = calendar.getTime();

        boolean isValid = true;

        if (numero.isEmpty()) {
            layoutNumero.setError(getString(R.string.msg_erro_numero_lote));
            isValid = false;
        } else {
            layoutNumero.setError(null);
        }

        if (linhagem.isEmpty()) {
            layoutLinhagem.setError(getString(R.string.msg_erro_linhagem));
            isValid = false;
        } else {
            layoutLinhagem.setError(null);
        }

        if (qtdStr.isEmpty() || Integer.parseInt(qtdStr) <= 0) {
            layoutQtd.setError(getString(R.string.msg_erro_qtd_inicial));
            isValid = false;
        } else {
            layoutQtd.setError(null);
        }

        if (pesoStr.isEmpty()) {
            layoutPeso.setError(getString(R.string.msg_erro_peso_inicial));
            isValid = false;
        } else {
            layoutPeso.setError(null);
        }

        if (isValid) {
            int qtd = Integer.parseInt(qtdStr);
            double peso = Double.parseDouble(pesoStr);
            viewModel.criarLote(avicultorId, numero, linhagem, galpao, dataInicio, qtd, peso, obs);
        }
    }

    private void observeViewModel() {
        viewModel.getSuccessAction().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, R.string.msg_sucesso_lote, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                if (error.contains(getString(R.string.msg_erro_lote_existe)) || error.contains("existe")) {
                    layoutNumero.setError(getString(R.string.msg_erro_lote_existe));
                } else {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

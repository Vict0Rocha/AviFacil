package com.example.avifacil.ui.registro;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import com.example.avifacil.R;
import com.example.avifacil.ui.viewmodel.RegistroViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CadastroRegistroActivity extends AppCompatActivity {

    private TextInputEditText editData, editMortas, editConsumo, editObs;
    private MaterialButton btnSalvar;
    private RegistroViewModel viewModel;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private long loteId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_registro);

        loteId = getIntent().getLongExtra("LOTE_ID", -1);
        if (loteId == -1) {
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbarCadastroRegistro);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        editData = findViewById(R.id.editDataRegistro);
        editMortas = findViewById(R.id.editMortas);
        editConsumo = findViewById(R.id.editConsumo);
        editObs = findViewById(R.id.editObs);
        btnSalvar = findViewById(R.id.btnSalvarRegistro);

        viewModel = new ViewModelProvider(this).get(RegistroViewModel.class);

        editData.setOnClickListener(v -> showDatePicker());
        
        // Preenche com data atual por padrão
        atualizarLabelData();

        btnSalvar.setOnClickListener(v -> salvarRegistro());

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

    private void salvarRegistro() {
        String mortasStr = editMortas.getText().toString().trim();
        String consumoStr = editConsumo.getText().toString().trim();
        String obs = editObs.getText().toString().trim();

        if (mortasStr.isEmpty()) {
            editMortas.setError(getString(R.string.msg_erro_mortas));
            return;
        }

        if (consumoStr.isEmpty()) {
            editConsumo.setError(getString(R.string.msg_erro_consumo));
            return;
        }

        int mortas = Integer.parseInt(mortasStr);
        double consumo = Double.parseDouble(consumoStr);
        Date data = calendar.getTime();

        viewModel.adicionarRegistro(loteId, data, mortas, consumo, obs);
    }
}
package com.example.avifacil.ui.avicultor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.avifacil.MainActivity;
import com.example.avifacil.R;
import com.example.avifacil.ui.viewmodel.AvicultorViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class CadastroAvicultorActivity extends AppCompatActivity {

    private AvicultorViewModel viewModel;
    private TextInputEditText editNome, editEmail, editPropriedade;
    private Button btnSalvar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_avicultor);

        editNome = findViewById(R.id.editNome);
        editEmail = findViewById(R.id.editEmail);
        editPropriedade = findViewById(R.id.editPropriedade);
        btnSalvar = findViewById(R.id.btnSalvar);

        viewModel = new ViewModelProvider(this).get(AvicultorViewModel.class);

        observeViewModel();

        btnSalvar.setOnClickListener(v -> {
            String nome = editNome.getText().toString();
            String email = editEmail.getText().toString();
            String propriedade = editPropriedade.getText().toString();
            viewModel.salvarAvicultor(nome, email, propriedade);
        });
    }

    private void observeViewModel() {
        viewModel.getSuccessAction().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, R.string.msg_sucesso_cadastro, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}

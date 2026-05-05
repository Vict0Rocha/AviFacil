package com.example.avifacil.ui.avicultor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.avifacil.R;
import com.example.avifacil.ui.home.HomeActivity;
import com.example.avifacil.ui.viewmodel.AvicultorViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class CadastroAvicultorActivity extends AppCompatActivity {

    private AvicultorViewModel viewModel;
    private TextInputEditText editNome, editEmail, editPropriedade;
    private TextInputLayout inputLayoutNome, inputLayoutPropriedade;
    private Button btnSalvar;
    private android.widget.ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_avicultor);

        // Inicialização dos componentes com os novos IDs
        scrollView = findViewById(R.id.scrollViewCadastro);
        editNome = findViewById(R.id.editNomeAvicultor);
        editEmail = findViewById(R.id.editEmailAvicultor);
        editPropriedade = findViewById(R.id.editPropriedadeAvicultor);
        inputLayoutNome = findViewById(R.id.inputLayoutNome);
        inputLayoutPropriedade = findViewById(R.id.inputLayoutPropriedade);
        btnSalvar = findViewById(R.id.btnSalvarAvicultor);

        viewModel = new ViewModelProvider(this).get(AvicultorViewModel.class);

        observeViewModel();

        btnSalvar.setOnClickListener(v -> {
            // Limpa erros anteriores
            inputLayoutNome.setError(null);
            inputLayoutPropriedade.setError(null);

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
                // Navega para a Home após o cadastro com sucesso
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                // Validação visível na interface usando setError do TextInputLayout
                if (error.equals(getString(R.string.msg_erro_nome))) {
                    inputLayoutNome.setError(error);
                } else if (error.equals(getString(R.string.msg_erro_propriedade))) {
                    inputLayoutPropriedade.setError(error);
                } else {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

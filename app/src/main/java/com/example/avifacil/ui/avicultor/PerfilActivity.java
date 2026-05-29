package com.example.avifacil.ui.avicultor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.avifacil.R;
import com.example.avifacil.data.local.entity.AvicultorEntity;
import com.example.avifacil.ui.viewmodel.AvicultorViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class PerfilActivity extends AppCompatActivity {

    private AvicultorViewModel viewModel;
    private TextInputEditText editNome, editPropriedade, editSenha, editConfirmarSenha;
    private TextInputLayout inputLayoutNome, inputLayoutPropriedade, inputLayoutSenha, inputLayoutConfirmarSenha;
    private Button btnSalvar;
    private ImageButton btnVoltar;
    private String userUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        initViews();
        viewModel = new ViewModelProvider(this).get(AvicultorViewModel.class);
        
        userUuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        viewModel.carregarAvicultorPorUuid(userUuid);

        observeViewModel();

        btnVoltar.setOnClickListener(v -> finish());

        btnSalvar.setOnClickListener(v -> {
            String nome = editNome.getText().toString();
            String propriedade = editPropriedade.getText().toString();
            String senha = editSenha.getText().toString();
            String confirmarSenha = editConfirmarSenha.getText().toString();

            // Salva dados do avicultor
            viewModel.salvarAvicultor(nome, null, propriedade, userUuid);

            // Se digitou algo na senha, tenta atualizar
            if (!senha.isEmpty()) {
                viewModel.atualizarSenha(senha, confirmarSenha);
            }
        });
    }

    private void initViews() {
        editNome = findViewById(R.id.editNomeAvicultor);
        editPropriedade = findViewById(R.id.editPropriedadeAvicultor);
        editSenha = findViewById(R.id.editNovaSenha);
        editConfirmarSenha = findViewById(R.id.editConfirmarSenha);
        inputLayoutNome = findViewById(R.id.inputLayoutNome);
        inputLayoutPropriedade = findViewById(R.id.inputLayoutPropriedade);
        inputLayoutSenha = findViewById(R.id.inputLayoutSenha);
        inputLayoutConfirmarSenha = findViewById(R.id.inputLayoutConfirmarSenha);
        btnSalvar = findViewById(R.id.btnSalvarPerfil);
        btnVoltar = findViewById(R.id.btnVoltar);
    }

    private void observeViewModel() {
        viewModel.getAvicultorLogado().observe(this, avicultor -> {
            if (avicultor != null) {
                editNome.setText(avicultor.getNome());
                editPropriedade.setText(avicultor.getNomePropriedade());
            }
        });

        viewModel.getSuccessAction().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, R.string.msg_sucesso_perfil, Toast.LENGTH_SHORT).show();
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

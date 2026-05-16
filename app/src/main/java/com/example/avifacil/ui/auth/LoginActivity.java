package com.example.avifacil.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.avifacil.R;
import com.example.avifacil.ui.avicultor.CadastroAvicultorActivity;
import com.example.avifacil.ui.dashboard.DashboardActivity;
import com.example.avifacil.ui.viewmodel.AvicultorViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editEmail, editSenha;
    private TextInputLayout inputLayoutSenha;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private AvicultorViewModel avicultorViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ajuste para bordas infinitas (Edge-to-Edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });

        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        inputLayoutSenha = findViewById(R.id.inputLayoutSenha);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        // Mostrar ícone de senha apenas quando houver texto
        inputLayoutSenha.setEndIconVisible(false);
        editSenha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                inputLayoutSenha.setEndIconVisible(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mAuth = FirebaseAuth.getInstance();
        avicultorViewModel = new ViewModelProvider(this).get(AvicultorViewModel.class);

        // Se já estiver logado, verifica perfil local
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            verificarPerfilLocal(currentUser.getUid());
        }

        btnLogin.setOnClickListener(v -> loginUsuario());

        // Observar o perfil logado
        avicultorViewModel.getAvicultorLogado().observe(this, avicultor -> {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            if (avicultor != null) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            } else {
                // Se não tem perfil no banco, mas está logado no Auth, vai para cadastro
                if (mAuth.getCurrentUser() != null) {
                    startActivity(new Intent(this, CadastroAvicultorActivity.class));
                    finish();
                }
            }
        });

        // Observar erros de carregamento
        avicultorViewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                if (error.contains("Perfil não encontrado")) {
                    startActivity(new Intent(this, CadastroAvicultorActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loginUsuario() {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            avicultorViewModel.carregarAvicultorPorUuid(user.getUid());
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);
                        Toast.makeText(this, getErrorMessage(task.getException()), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void verificarPerfilLocal(String uuid) {
        progressBar.setVisibility(View.VISIBLE);
        avicultorViewModel.carregarAvicultorPorUuid(uuid);
    }

    private String getErrorMessage(Exception exception) {
        String errorMsg = "Ocorreu um erro inesperado. Tente novamente.";
        if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();
            switch (errorCode) {
                case "ERROR_INVALID_EMAIL": errorMsg = "O endereço de e-mail está mal formatado."; break;
                case "ERROR_WRONG_PASSWORD": errorMsg = "Senha incorreta."; break;
                case "ERROR_USER_NOT_FOUND": errorMsg = "Não existe usuário correspondente a este e-mail."; break;
                case "ERROR_TOO_MANY_REQUESTS": errorMsg = "Muitas tentativas. Tente novamente mais tarde."; break;
                case "ERROR_NETWORK_REQUEST_FAILED": errorMsg = "Erro de rede. Verifique sua conexão."; break;
            }
        }
        return errorMsg;
    }
}

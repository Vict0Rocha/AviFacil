package com.example.avifacil.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.avifacil.R;
import com.example.avifacil.data.local.entity.AvicultorEntity;
import com.example.avifacil.ui.avicultor.CadastroAvicultorActivity;
import com.example.avifacil.ui.dashboard.DashboardActivity;
import com.example.avifacil.ui.viewmodel.AvicultorViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import androidx.lifecycle.ViewModelProvider;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editEmail, editSenha;
    private Button btnLogin, btnRegistrar;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private AvicultorViewModel avicultorViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editEmail = findViewById(R.id.editEmail);
        editSenha = findViewById(R.id.editSenha);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        avicultorViewModel = new ViewModelProvider(this).get(AvicultorViewModel.class);

        // Se já estiver logado, verifica se tem perfil local
        if (mAuth.getCurrentUser() != null) {
            verificarPerfilLocal(mAuth.getCurrentUser().getUid());
        }

        btnLogin.setOnClickListener(v -> loginUsuario());
        btnRegistrar.setOnClickListener(v -> registrarUsuario());

        // Observar o perfil logado apenas uma vez
        avicultorViewModel.getAvicultorLogado().observe(this, avicultor -> {
            if (avicultor == null) return;
            
            progressBar.setVisibility(View.GONE);
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null && avicultor.getUuid().equals(user.getUid())) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            }
        });

        // Observar erros de carregamento
        avicultorViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && (error.contains("Erro ao carregar dados") || error.contains("Perfil não encontrado"))) {
                progressBar.setVisibility(View.GONE);
                btnLogin.setEnabled(true);
                btnRegistrar.setEnabled(true);
                // Se deu erro ou realmente não existe nada na nuvem, vai para cadastro
                startActivity(new Intent(this, CadastroAvicultorActivity.class));
                finish();
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
                        avicultorViewModel.carregarAvicultorPorUuid(task.getResult().getUser().getUid());
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);
                        String errorMsg = getErrorMessage(task.getException());
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void verificarPerfilLocal(String uuid) {
        progressBar.setVisibility(View.VISIBLE);
        avicultorViewModel.carregarAvicultorPorUuid(uuid);
    }

    private void registrarUsuario() {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (senha.length() < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnRegistrar.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, CadastroAvicultorActivity.class));
                        finish();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnRegistrar.setEnabled(true);
                        String errorMsg = getErrorMessage(task.getException());
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String getErrorMessage(Exception exception) {
        String errorMsg = "Ocorreu um erro inesperado. Tente novamente.";

        if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();

            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    errorMsg = "O endereço de e-mail está mal formatado.";
                    break;
                case "ERROR_WRONG_PASSWORD":
                    errorMsg = "Senha incorreta.";
                    break;
                case "ERROR_USER_NOT_FOUND":
                    errorMsg = "Não existe usuário correspondente a este e-mail.";
                    break;
                case "ERROR_USER_DISABLED":
                    errorMsg = "Este usuário foi desativado.";
                    break;
                case "ERROR_TOO_MANY_REQUESTS":
                    errorMsg = "Muitas tentativas. Tente novamente mais tarde.";
                    break;
                case "ERROR_OPERATION_NOT_ALLOWED":
                    errorMsg = "O login com e-mail e senha não está habilitado.";
                    break;
                case "ERROR_WEAK_PASSWORD":
                    errorMsg = "A senha é muito fraca.";
                    break;
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    errorMsg = "Este e-mail já está cadastrado em outra conta.";
                    break;
                case "ERROR_NETWORK_REQUEST_FAILED":
                    errorMsg = "Erro de rede. Verifique sua conexão com a internet.";
                    break;
            }
        } else if (exception != null) {
            errorMsg = exception.getLocalizedMessage();
        }

        return errorMsg;
    }
}
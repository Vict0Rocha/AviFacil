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
import com.example.avifacil.ui.avicultor.PerfilActivity;
import com.example.avifacil.ui.dashboard.DashboardActivity;
import com.example.avifacil.ui.viewmodel.AvicultorViewModel;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

/**
 * Tela de Autenticação (Login)
 * 
 * Responsável por gerenciar o acesso do usuário via Firebase Auth e 
 * garantir a consistência do perfil local (SQLite) com a nuvem (Firestore).
 * Implementa estratégia offline-first permitindo entrada se a sessão estiver ativa.
 */
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

        findViewById(R.id.textEsqueciSenha).setOnClickListener(v -> recuperarSenha());

        // Observar o perfil logado
        avicultorViewModel.getAvicultorLogado().observe(this, avicultor -> {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
            if (avicultor != null) {
                // Verifica tanto a flag booleana quanto o status em String para garantir o bloqueio vindo do site
                if (avicultor.isBloqueado() || "BLOQUEADO".equalsIgnoreCase(avicultor.getStatus())) {
                    mAuth.signOut();
                    Toast.makeText(this, R.string.msg_bloqueado, Toast.LENGTH_LONG).show();
                    return;
                }
                
                // Após o login com sucesso, redireciona para o Dashboard
                // A flag precisaSincronizarSenha não deve bloquear o acesso se ele já logou com a senha nova
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

    private void recuperarSenha() {
        String email = editEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Informe seu e-mail para receber o link de recuperação", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "E-mail de recuperação enviado para: " + email, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Erro ao enviar e-mail: " + getErrorMessage(task.getException()), Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Realiza a autenticação via Firebase Auth.
     * Ao obter sucesso, dispara o carregamento do perfil no ViewModel.
     */
    private void loginUsuario() {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Garante que qualquer sessão anterior seja encerrada antes de tentar o novo login
        mAuth.signOut();

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

    /**
     * Estratégia Offline-first:
     * Verifica se existe um perfil localmente para o UUID autenticado.
     * Tenta atualizar o token em background sem interromper o fluxo do usuário.
     */
    private void verificarPerfilLocal(String uuid) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // Offline-first: Carrega o perfil local imediatamente sem esperar o reload do Firebase
            avicultorViewModel.carregarAvicultorPorUuid(uuid);
            
            // Tenta recarregar em background apenas para validar se a conta ainda existe/não foi deletada
            // Se falhar por falta de rede, não fazemos nada (mantemos offline)
            user.reload().addOnFailureListener(e -> {
                if (e instanceof com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                    mAuth.signOut();
                    Toast.makeText(this, "Sessão encerrada pelo servidor.", Toast.LENGTH_LONG).show();
                    // O observer no ViewModel cuidará de redirecionar se o avicultor sumir ou for bloqueado
                }
            });
        }
    }

    /**
     * Mapeamento de erros técnicos do Firebase para mensagens amigáveis ao usuário final.
     */
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
                case "ERROR_USER_DISABLED": errorMsg = "Esta conta de usuário foi desativada."; break;
                case "ERROR_USER_TOKEN_EXPIRED": 
                case "ERROR_INVALID_USER_TOKEN": errorMsg = "Sessão expirada. Por favor, faça login novamente."; break;
                case "ERROR_WEAK_PASSWORD": errorMsg = "A senha é muito fraca."; break;
                default: errorMsg = "Erro de autenticação (" + errorCode + "): " + exception.getLocalizedMessage(); break;
            }
        } else if (exception != null) {
            // Captura erros de rede, SSL ou outros erros inesperados
            errorMsg = "Erro: " + exception.getLocalizedMessage();
        }
        return errorMsg;
    }
}

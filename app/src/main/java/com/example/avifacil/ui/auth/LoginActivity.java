package com.example.avifacil.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private TextInputEditText editEmail, editSenha;
    private Button btnLogin, btnIrParaRegistro, btnGoogle;
    private TextView txtEsqueciSenha;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
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
        btnLogin = findViewById(R.id.btnLogin);
        btnIrParaRegistro = findViewById(R.id.btnIrParaRegistro);
        btnGoogle = findViewById(R.id.btnGoogle);
        txtEsqueciSenha = findViewById(R.id.txtEsqueciSenha);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        avicultorViewModel = new ViewModelProvider(this).get(AvicultorViewModel.class);

        // Configurar Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Se já estiver logado e verificado, verifica perfil local
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified()) {
            verificarPerfilLocal(currentUser.getUid());
        } else if (currentUser != null) {
            // Se logado mas não verificado, desloga para forçar login com verificação
            mAuth.signOut();
            mGoogleSignInClient.signOut();
        }

        btnLogin.setOnClickListener(v -> loginUsuario());
        btnIrParaRegistro.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
        btnGoogle.setOnClickListener(v -> signInWithGoogle());
        txtEsqueciSenha.setOnClickListener(v -> mostrarDialogoRecuperarSenha());

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

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Erro ao autenticar com Google", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);
        btnGoogle.setEnabled(false);

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Google accounts are usually already verified
                        if (user != null) {
                            avicultorViewModel.carregarAvicultorPorUuid(user.getUid());
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);
                        btnGoogle.setEnabled(true);
                        Toast.makeText(this, "Erro na autenticação Firebase com Google", Toast.LENGTH_SHORT).show();
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
                            if (user.isEmailVerified()) {
                                avicultorViewModel.carregarAvicultorPorUuid(user.getUid());
                            } else {
                                progressBar.setVisibility(View.GONE);
                                btnLogin.setEnabled(true);
                                mAuth.signOut();
                                mGoogleSignInClient.signOut();
                                Toast.makeText(this, "Por favor, verifique seu e-mail antes de entrar.", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnLogin.setEnabled(true);
                        Toast.makeText(this, getErrorMessage(task.getException()), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void mostrarDialogoRecuperarSenha() {
        final TextInputEditText input = new TextInputEditText(this);
        input.setHint(R.string.hint_email);
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        new AlertDialog.Builder(this)
                .setTitle(R.string.title_recuperar_senha)
                .setMessage(R.string.msg_digite_email_recuperacao)
                .setView(input, 40, 0, 40, 0)
                .setPositiveButton(R.string.btn_enviar, (dialog, which) -> {
                    String email = input.getText().toString().trim();
                    if (!email.isEmpty()) {
                        mAuth.sendPasswordResetEmail(email)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(this, getString(R.string.msg_redefinir_senha_enviado, email), Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(this, getErrorMessage(task.getException()), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                })
                .setNegativeButton(R.string.btn_cancelar, null)
                .show();
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
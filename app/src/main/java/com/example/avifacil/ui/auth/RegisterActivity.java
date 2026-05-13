package com.example.avifacil.ui.auth;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.avifacil.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText editEmail, editSenha, editConfirmarSenha;
    private TextInputLayout inputLayoutSenha, inputLayoutConfirmarSenha;
    private Button btnRegistrar;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editEmail = findViewById(R.id.editRegisterEmail);
        editSenha = findViewById(R.id.editRegisterSenha);
        editConfirmarSenha = findViewById(R.id.editRegisterConfirmarSenha);
        inputLayoutSenha = findViewById(R.id.inputLayoutRegisterSenha);
        inputLayoutConfirmarSenha = findViewById(R.id.inputLayoutRegisterConfirmarSenha);
        btnRegistrar = findViewById(R.id.btnFinalizarRegistro);
        progressBar = findViewById(R.id.progressBarRegister);

        mAuth = FirebaseAuth.getInstance();

        btnRegistrar.setOnClickListener(v -> registrarUsuario());

        // Mostrar ícone de senha apenas ao digitar
        setupPasswordToggle(editSenha, inputLayoutSenha);
        setupPasswordToggle(editConfirmarSenha, inputLayoutConfirmarSenha);
    }

    private void setupPasswordToggle(TextInputEditText editText, TextInputLayout inputLayout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                inputLayout.setEndIconMode(s.length() > 0 ? TextInputLayout.END_ICON_PASSWORD_TOGGLE : TextInputLayout.END_ICON_NONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void registrarUsuario() {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();
        String confirmarSenha = editConfirmarSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            Toast.makeText(this, getString(R.string.msg_erro_senhas_diferentes), Toast.LENGTH_SHORT).show();
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
                        enviarEmailVerificacao();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        btnRegistrar.setEnabled(true);
                        Toast.makeText(RegisterActivity.this, getErrorMessage(task.getException()), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void enviarEmailVerificacao() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, 
                                    getString(R.string.msg_email_verificacao_enviado, user.getEmail()), 
                                    Toast.LENGTH_LONG).show();
                            finish(); // Volta para a tela de login
                        } else {
                            Toast.makeText(RegisterActivity.this, "Erro ao enviar e-mail de verificação.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private String getErrorMessage(Exception exception) {
        String errorMsg = "Ocorreu um erro inesperado. Tente novamente.";
        if (exception instanceof FirebaseAuthException) {
            String errorCode = ((FirebaseAuthException) exception).getErrorCode();
            switch (errorCode) {
                case "ERROR_INVALID_EMAIL": errorMsg = "O endereço de e-mail está mal formatado."; break;
                case "ERROR_WEAK_PASSWORD": errorMsg = "A senha é muito fraca."; break;
                case "ERROR_EMAIL_ALREADY_IN_USE": errorMsg = "Este e-mail já está cadastrado."; break;
                case "ERROR_NETWORK_REQUEST_FAILED": errorMsg = "Erro de rede. Verifique sua conexão."; break;
            }
        }
        return errorMsg;
    }
}
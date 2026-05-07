package com.example.avifacil;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.avifacil.data.local.entity.AvicultorEntity;
import com.example.avifacil.ui.avicultor.CadastroAvicultorActivity;
import com.example.avifacil.ui.viewmodel.AvicultorViewModel;
import java.util.List;

import com.example.avifacil.ui.dashboard.DashboardActivity;
import com.example.avifacil.ui.home.HomeActivity;

public class MainActivity extends AppCompatActivity {
    private AvicultorViewModel viewModel;
    private boolean transitionStarted = false;
    private final android.os.Handler handler = new android.os.Handler();
    private final Runnable timeoutRunnable = () -> {
        if (!transitionStarted) {
            android.util.Log.e("AviFacil", "Timeout: Banco de dados não respondeu. Indo para cadastro.");
            navigateToCadastro();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(AvicultorViewModel.class);

        // Agenda o escape de segurança para 4 segundos
        handler.postDelayed(timeoutRunnable, 4000);

        viewModel.getAvicultoresAtivos().observe(this, avicultores -> {
            if (avicultores != null && !transitionStarted) {
                checkFlow(avicultores);
            }
        });
        
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                android.widget.Toast.makeText(this, error, android.widget.Toast.LENGTH_LONG).show();
                // Se houver erro, tenta ir para o cadastro após o Toast
                handler.postDelayed(this::navigateToCadastro, 2000);
            }
        });
        
        viewModel.carregarAvicultores();
    }

    private void checkFlow(List<AvicultorEntity> avicultores) {
        if (avicultores.isEmpty()) {
            navigateToCadastro();
        } else {
            navigateToHome();
        }
    }

    private void navigateToCadastro() {
        if (transitionStarted) return;
        transitionStarted = true;
        handler.removeCallbacks(timeoutRunnable);
        Intent intent = new Intent(this, CadastroAvicultorActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToHome() {
        if (transitionStarted) return;
        transitionStarted = true;
        handler.removeCallbacks(timeoutRunnable);
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(timeoutRunnable);
        super.onDestroy();
    }
}


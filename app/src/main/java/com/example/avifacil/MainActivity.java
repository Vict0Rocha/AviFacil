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

public class MainActivity extends AppCompatActivity {
    private TextView txtBoasVindas;
    private TextView txtStatus;
    private AvicultorViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout simples provisório para a Home
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 50, 50, 50);
        layout.setGravity(android.view.Gravity.CENTER);

        txtBoasVindas = new TextView(this);
        txtBoasVindas.setTextSize(24);
        txtBoasVindas.setGravity(android.view.Gravity.CENTER);
        layout.addView(txtBoasVindas);

        txtStatus = new TextView(this);
        txtStatus.setText(R.string.next_step_lotes);
        txtStatus.setGravity(android.view.Gravity.CENTER);
        layout.addView(txtStatus);

        setContentView(layout);

        viewModel = new ViewModelProvider(this).get(AvicultorViewModel.class);

        // Observa a lista de avicultores para decidir o fluxo
        viewModel.getAvicultoresAtivos().observe(this, this::checkFlow);
        
        // Carrega os dados iniciais
        viewModel.carregarAvicultores();
    }

    private void checkFlow(List<AvicultorEntity> avicultores) {
        if (avicultores == null) return;

        if (avicultores.isEmpty()) {
            // Caso 1: NÃO existe avicultor -> Ir para Cadastro
            Intent intent = new Intent(this, CadastroAvicultorActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Caso 2: EXISTE avicultor -> Mostrar Boas-vindas
            AvicultorEntity avicultor = avicultores.get(0);
            txtBoasVindas.setText(getString(R.string.welcome_message, avicultor.getNome(), avicultor.getNomePropriedade()));
        }
    }
}

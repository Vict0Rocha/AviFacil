package com.example.avifacil;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.avifacil.data.local.entity.AvicultorEntity;
import com.example.avifacil.ui.viewmodel.AvicultorViewModel;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView label;
    private AvicultorViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        label = new TextView(this);
        label.setText("Iniciando Arquitetura MVVM...");
        label.setGravity(android.view.Gravity.CENTER);
        label.setTextSize(20);
        setContentView(label);

        // Inicializa o ViewModel
        viewModel = new ViewModelProvider(this).get(AvicultorViewModel.class);

        // Observa os dados
        viewModel.getAvicultoresAtivos().observe(this, this::updateUI);

        // Observa erros
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) label.setText(error);
        });

        // Testa o fluxo: Salva e carrega
        viewModel.salvarAvicultor("Victor TCC", "victor@tcc.com");
    }

    private void updateUI(List<AvicultorEntity> lista) {
        if (lista == null || lista.isEmpty()) {
            label.setText("Nenhum avicultor encontrado.");
            return;
        }

        StringBuilder sb = new StringBuilder("Arquitetura MVVM OK!\nAvicultores:\n");
        for (AvicultorEntity a : lista) {
            sb.append("- ").append(a.getNome()).append("\n");
        }
        label.setText(sb.toString());
    }
}

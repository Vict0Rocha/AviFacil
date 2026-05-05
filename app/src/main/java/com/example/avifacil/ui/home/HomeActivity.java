package com.example.avifacil.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.avifacil.R;
import com.example.avifacil.data.local.entity.AvicultorEntity;
import com.example.avifacil.ui.viewmodel.AvicultorViewModel;
import com.example.avifacil.ui.lote.ListaLotesActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView txtBoasVindas, txtPropriedade;
    private Button btnIrParaLotes;
    private AvicultorViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        txtBoasVindas = findViewById(R.id.txtBoasVindasHome);
        txtPropriedade = findViewById(R.id.txtNomePropriedadeHome);
        btnIrParaLotes = findViewById(R.id.btnIrParaLotes);

        btnIrParaLotes.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ListaLotesActivity.class);
            startActivity(intent);
        });

        viewModel = new ViewModelProvider(this).get(AvicultorViewModel.class);

        viewModel.getAvicultoresAtivos().observe(this, avicultores -> {
            if (avicultores != null && !avicultores.isEmpty()) {
                AvicultorEntity avicultor = avicultores.get(0);
                txtBoasVindas.setText(getString(R.string.welcome_message, avicultor.getNome()));
                txtPropriedade.setText(avicultor.getNomePropriedade());
            }
        });

        viewModel.carregarAvicultores();
    }
}

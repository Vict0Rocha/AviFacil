package com.example.avifacil.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.avifacil.R;
import com.example.avifacil.data.local.entity.AvicultorEntity;
import com.example.avifacil.ui.viewmodel.AvicultorViewModel;
import com.example.avifacil.ui.lote.LotesActivity;

public class HomeActivity extends AppCompatActivity {

    private TextView txtBoasVindas, txtPropriedade;
    private Button btnIrParaLotes;
    private AvicultorViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Ajuste para bordas infinitas (Edge-to-Edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });

        txtBoasVindas = findViewById(R.id.txtBoasVindasHome);
        txtPropriedade = findViewById(R.id.txtNomePropriedadeHome);
        btnIrParaLotes = findViewById(R.id.btnIrParaLotes);

        btnIrParaLotes.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LotesActivity.class);
            startActivity(intent);
        });

        viewModel = new ViewModelProvider(this).get(AvicultorViewModel.class);

        viewModel.getAvicultorLogado().observe(this, avicultor -> {
            if (avicultor != null) {
                txtBoasVindas.setText(getString(R.string.welcome_message, avicultor.getNome()));
                txtPropriedade.setText(avicultor.getNomePropriedade());
            } else {
                // Se não encontrar o perfil do UUID logado, redireciona ou limpa
                txtBoasVindas.setText("Olá!");
                txtPropriedade.setText("");
            }
        });

        String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (currentUid != null) {
            viewModel.carregarAvicultorPorUuid(currentUid);
        }
    }
}

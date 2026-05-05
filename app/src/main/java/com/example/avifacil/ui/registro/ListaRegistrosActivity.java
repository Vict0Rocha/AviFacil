package com.example.avifacil.ui.registro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avifacil.R;
import com.example.avifacil.ui.viewmodel.RegistroViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ListaRegistrosActivity extends AppCompatActivity {

    private RegistroViewModel viewModel;
    private RegistroAdapter adapter;
    private TextView txtEmpty;
    private long loteId = -1;
    private String numeroLote = "";
    private boolean isLoteAtivo = true;
    private java.util.Date dataInicioLote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_registros);

        loteId = getIntent().getLongExtra("LOTE_ID", -1);
        numeroLote = getIntent().getStringExtra("NUMERO_LOTE");

        if (loteId == -1) {
            finish();
            return;
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerRegistros);
        txtEmpty = findViewById(R.id.txtEmptyRegistros);
        FloatingActionButton fab = findViewById(R.id.fabAddRegistro);

        adapter = new RegistroAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(RegistroViewModel.class);
        com.example.avifacil.ui.viewmodel.LoteViewModel loteViewModel = new ViewModelProvider(this).get(com.example.avifacil.ui.viewmodel.LoteViewModel.class);
        com.example.avifacil.ui.viewmodel.AvicultorViewModel avicultorViewModel = new ViewModelProvider(this).get(com.example.avifacil.ui.viewmodel.AvicultorViewModel.class);

        avicultorViewModel.getAvicultoresAtivos().observe(this, avicultores -> {
            if (avicultores != null && !avicultores.isEmpty()) {
                loteViewModel.carregarLotes(avicultores.get(0).getId());
            }
        });

        loteViewModel.getLotesAtivos().observe(this, lotes -> {
            if (lotes != null) {
                for (com.example.avifacil.data.local.entity.LoteEntity lote : lotes) {
                    if (lote.getId() == loteId) {
                        dataInicioLote = lote.getDataInicio();
                        isLoteAtivo = (lote.getStatus() == com.example.avifacil.data.local.entity.StatusLote.ATIVO);
                        fab.setVisibility(isLoteAtivo ? View.VISIBLE : View.GONE);
                        
                        // Atualizar lista para garantir que a idade seja calculada com a data certa
                        viewModel.carregarRegistros(loteId);
                        break;
                    }
                }
            }
        });

        viewModel.getRegistrosLote().observe(this, registros -> {
            if (registros == null || registros.isEmpty()) {
                txtEmpty.setVisibility(View.VISIBLE);
                adapter.setRegistros(new java.util.ArrayList<>(), dataInicioLote);
            } else {
                txtEmpty.setVisibility(View.GONE);
                adapter.setRegistros(registros, dataInicioLote);
            }
        });

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, CadastroRegistroActivity.class);
            intent.putExtra("LOTE_ID", loteId);
            startActivity(intent);
        });

        // Carregar dados iniciais
        avicultorViewModel.carregarAvicultores();
        viewModel.carregarRegistros(loteId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loteId != -1) {
            viewModel.carregarRegistros(loteId);
        }
    }
}
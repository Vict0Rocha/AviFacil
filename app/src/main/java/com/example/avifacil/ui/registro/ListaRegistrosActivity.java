package com.example.avifacil.ui.registro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

        Toolbar toolbar = findViewById(R.id.toolbarRegistros);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_lista_registros) + ": " + numeroLote);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        RecyclerView recyclerView = findViewById(R.id.recyclerRegistros);
        txtEmpty = findViewById(R.id.txtEmptyRegistros);
        FloatingActionButton fab = findViewById(R.id.fabAddRegistro);

        adapter = new RegistroAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(RegistroViewModel.class);
        
        viewModel.getRegistrosLote().observe(this, registros -> {
            if (registros == null || registros.isEmpty()) {
                txtEmpty.setVisibility(View.VISIBLE);
                adapter.setRegistros(new java.util.ArrayList<>());
            } else {
                txtEmpty.setVisibility(View.GONE);
                adapter.setRegistros(registros);
            }
        });

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, CadastroRegistroActivity.class);
            intent.putExtra("LOTE_ID", loteId);
            startActivity(intent);
        });

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
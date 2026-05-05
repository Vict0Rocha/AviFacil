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

        loteViewModel.getLoteAtual().observe(this, lote -> {
            if (lote != null) {
                dataInicioLote = lote.getDataInicio();
                isLoteAtivo = (lote.getStatus() == com.example.avifacil.data.local.entity.StatusLote.ATIVO);
                fab.setVisibility(isLoteAtivo ? View.VISIBLE : View.GONE);
                adapter.setLoteAtivo(isLoteAtivo);
                viewModel.carregarRegistros(loteId);
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

        adapter.setOnRegistroClickListener(new RegistroAdapter.OnRegistroClickListener() {
            @Override
            public void onEdit(com.example.avifacil.data.local.entity.RegistroEntity registro) {
                if (isLoteAtivo) {
                    Intent intent = new Intent(ListaRegistrosActivity.this, CadastroRegistroActivity.class);
                    intent.putExtra("REGISTRO_ID", registro.getId());
                    intent.putExtra("LOTE_ID", loteId);
                    startActivity(intent);
                }
            }

            @Override
            public void onDelete(com.example.avifacil.data.local.entity.RegistroEntity registro) {
                if (isLoteAtivo) {
                    mostrarDialogoExclusao(registro);
                }
            }
        });

        // Carregar dados iniciais
        loteViewModel.carregarLote(loteId);
    }

    private void mostrarDialogoExclusao(com.example.avifacil.data.local.entity.RegistroEntity registro) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.dialog_excluir_registro_title)
                .setMessage(R.string.dialog_excluir_registro_msg)
                .setPositiveButton(R.string.btn_confirmar, (dialog, which) -> {
                    viewModel.excluirRegistro(registro.getId());
                })
                .setNegativeButton(R.string.btn_cancelar, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (loteId != -1) {
            viewModel.carregarRegistros(loteId);
        }
    }
}

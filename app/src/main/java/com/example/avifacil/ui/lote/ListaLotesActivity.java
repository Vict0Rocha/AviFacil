package com.example.avifacil.ui.lote;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avifacil.R;
import com.example.avifacil.data.local.entity.AvicultorEntity;
import com.example.avifacil.data.local.entity.LoteEntity;
import com.example.avifacil.ui.registro.ListaRegistrosActivity;
import com.example.avifacil.ui.viewmodel.AvicultorViewModel;
import com.example.avifacil.ui.viewmodel.LoteViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ListaLotesActivity extends AppCompatActivity {

    private LoteViewModel loteViewModel;
    private AvicultorViewModel avicultorViewModel;
    private LoteAdapter adapter;
    private TextView txtEmpty;
    private long avicultorId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_lotes);

        RecyclerView recyclerView = findViewById(R.id.recyclerLotes);
        txtEmpty = findViewById(R.id.txtEmptyLotes);
        FloatingActionButton fab = findViewById(R.id.fabAddLote);

        adapter = new LoteAdapter();
        adapter.setOnLoteClickListener(lote -> {
            Intent intent = new Intent(this, ListaRegistrosActivity.class);
            intent.putExtra("LOTE_ID", lote.getId());
            intent.putExtra("NUMERO_LOTE", lote.getNumeroLote());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loteViewModel = new ViewModelProvider(this).get(LoteViewModel.class);
        avicultorViewModel = new ViewModelProvider(this).get(AvicultorViewModel.class);

        avicultorViewModel.getAvicultoresAtivos().observe(this, avicultores -> {
            if (avicultores != null && !avicultores.isEmpty()) {
                avicultorId = avicultores.get(0).getId();
                loteViewModel.carregarLotes(avicultorId);
            }
        });

        loteViewModel.getLotesAtivos().observe(this, lotes -> {
            if (lotes == null || lotes.isEmpty()) {
                txtEmpty.setVisibility(View.VISIBLE);
                adapter.setLotes(new java.util.ArrayList<>());
            } else {
                txtEmpty.setVisibility(View.GONE);
                adapter.setLotes(lotes);
            }
        });

        fab.setOnClickListener(v -> {
            if (avicultorId != -1) {
                Intent intent = new Intent(this, CadastroLoteActivity.class);
                intent.putExtra("AVICULTOR_ID", avicultorId);
                startActivity(intent);
            }
        });

        avicultorViewModel.carregarAvicultores();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (avicultorId != -1) {
            loteViewModel.carregarLotes(avicultorId);
        }
    }
}

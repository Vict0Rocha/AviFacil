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
import com.example.avifacil.ui.registro.ListaRegistrosActivity;
import com.example.avifacil.ui.viewmodel.AvicultorViewModel;
import com.example.avifacil.ui.viewmodel.LoteViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class LotesActivity extends AppCompatActivity {

    private LoteViewModel loteViewModel;
    private AvicultorViewModel avicultorViewModel;
    private LoteAdapter adapter;
    private TextView txtEmpty;
    private long avicultorId = -1;
    private String avicultorUuid = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lotes);

        RecyclerView recyclerView = findViewById(R.id.recyclerLotes);
        txtEmpty = findViewById(R.id.txtEmptyLotes);
        FloatingActionButton fab = findViewById(R.id.fabAddLote);

        adapter = new LoteAdapter();
        adapter.setOnLoteClickListener(new LoteAdapter.OnLoteClickListener() {
            @Override
            public void onLoteClick(com.example.avifacil.data.local.entity.LoteEntity lote) {
                Intent intent = new Intent(LotesActivity.this, DetalheLoteActivity.class);
                intent.putExtra("LOTE_ID", lote.getId());
                startActivity(intent);
            }

            @Override
            public void onLoteLongClick(com.example.avifacil.data.local.entity.LoteEntity lote) {
                if (lote.getStatus() == com.example.avifacil.data.local.entity.StatusLote.ATIVO) {
                    mostrarDialogoEncerrar(lote);
                }
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loteViewModel = new ViewModelProvider(this).get(LoteViewModel.class);
        avicultorViewModel = new ViewModelProvider(this).get(AvicultorViewModel.class);

        avicultorViewModel.getAvicultorLogado().observe(this, avicultor -> {
            if (avicultor != null) {
                avicultorId = avicultor.getId();
                avicultorUuid = avicultor.getUuid();
                loteViewModel.carregarLotes(avicultorId);
            } else {
                // Se o perfil sumiu ou trocou, volta pro login
                finish();
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
                intent.putExtra("AVICULTOR_UUID", avicultorUuid);
                startActivity(intent);
            }
        });

        String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (currentUid != null) {
            avicultorViewModel.carregarAvicultorPorUuid(currentUid);
        } else {
            finish();
        }
    }

    private void mostrarDialogoEncerrar(com.example.avifacil.data.local.entity.LoteEntity lote) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.dialog_encerrar_lote_title)
                .setMessage(R.string.dialog_encerrar_lote_msg)
                .setPositiveButton(R.string.btn_confirmar, (dialog, which) -> {
                    loteViewModel.encerrarLote(lote);
                })
                .setNegativeButton(R.string.btn_cancelar, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (avicultorId != -1) {
            loteViewModel.carregarLotes(avicultorId);
        }
    }
}

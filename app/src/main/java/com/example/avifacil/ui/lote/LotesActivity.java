package com.example.avifacil.ui.lote;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

        // Ajuste para bordas infinitas (Edge-to-Edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });

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
                mostrarOpcoesLote(lote);
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

    private void mostrarOpcoesLote(com.example.avifacil.data.local.entity.LoteEntity lote) {
        String[] opcoes;
        if (lote.getStatus() == com.example.avifacil.data.local.entity.StatusLote.ATIVO) {
            opcoes = new String[]{"Encerrar Lote", "Excluir Lote"};
        } else {
            opcoes = new String[]{"Excluir Lote"};
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Opções do Lote " + lote.getNumeroLote())
                .setItems(opcoes, (dialog, which) -> {
                    String opcaoSelecionada = opcoes[which];
                    if (opcaoSelecionada.equals("Encerrar Lote")) {
                        mostrarDialogoConfirmacaoDigitada(lote, "encerrar");
                    } else if (opcaoSelecionada.equals("Excluir Lote")) {
                        mostrarDialogoConfirmacaoDigitada(lote, "excluir");
                    }
                })
                .show();
    }

    private void mostrarDialogoConfirmacaoDigitada(com.example.avifacil.data.local.entity.LoteEntity lote, String acao) {
        String titulo = acao.equals("excluir") ? "Excluir Lote" : "Encerrar Lote";
        String mensagem = acao.equals("excluir") ? 
                "Você realmente deseja excluir esse lote? Essa ação não poderá ser desfeita. Digite \"excluir\" para confirmar:" :
                "Você realmente deseja encerrar esse lote? Digite \"encerrar\" para confirmar:";

        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint(acao);
        
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensagem)
                .setView(input)
                .setPositiveButton("Confirmar", null) // Definido depois para não fechar se estiver errado
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            android.widget.Button button = ((androidx.appcompat.app.AlertDialog) dialog).getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String textoDigitado = input.getText().toString().trim().toLowerCase();
                if (textoDigitado.equals(acao)) {
                    if (acao.equals("excluir")) {
                        loteViewModel.excluirLote(lote);
                    } else {
                        loteViewModel.encerrarLote(lote);
                    }
                    dialog.dismiss();
                } else {
                    input.setError("Digite \"" + acao + "\" corretamente para confirmar");
                }
            });
        });

        dialog.show();
    }

    private void mostrarDialogoEncerrar(com.example.avifacil.data.local.entity.LoteEntity lote) {
        mostrarDialogoConfirmacaoDigitada(lote, "encerrar");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (avicultorId != -1) {
            loteViewModel.carregarLotes(avicultorId);
        }
    }
}

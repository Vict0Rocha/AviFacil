package com.example.avifacil.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avifacil.R;
import com.example.avifacil.data.local.entity.AvicultorEntity;
import com.example.avifacil.ui.lote.DetalheLoteActivity;
import com.example.avifacil.ui.lote.LotesActivity;
import com.example.avifacil.ui.viewmodel.AvicultorViewModel;
import com.example.avifacil.ui.viewmodel.DashboardViewModel;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private DashboardViewModel dashboardViewModel;
    private AvicultorViewModel avicultorViewModel;
    private LoteResumoAdapter adapter;

    private TextView txtBoasVindas, txtPropriedade, txtTotalLotes, txtAvesAlojadas, txtMortalidade, txtAtivosEncerrados;
    private RecyclerView recyclerLotes;
    private TextView btnVerTodos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initViews();
        setupRecyclerView();
        setupViewModels();
    }

    private void initViews() {
        txtBoasVindas = findViewById(R.id.txtBoasVindasDash);
        txtPropriedade = findViewById(R.id.txtPropriedadeDash);
        txtTotalLotes = findViewById(R.id.txtTotalLotesDash);
        txtAvesAlojadas = findViewById(R.id.txtAvesAlojadasDash);
        txtMortalidade = findViewById(R.id.txtMortalidadeDash);
        txtAtivosEncerrados = findViewById(R.id.txtAtivosEncerradosDash);
        recyclerLotes = findViewById(R.id.recyclerLotesDash);
        btnVerTodos = findViewById(R.id.btnVerTodosLotes);

        btnVerTodos.setOnClickListener(v -> {
            startActivity(new Intent(this, LotesActivity.class));
        });
    }

    private void setupRecyclerView() {
        adapter = new LoteResumoAdapter();
        recyclerLotes.setLayoutManager(new LinearLayoutManager(this));
        recyclerLotes.setAdapter(adapter);

        adapter.setOnLoteClickListener(lote -> {
            Intent intent = new Intent(this, DetalheLoteActivity.class);
            intent.putExtra("LOTE_ID", lote.getId());
            startActivity(intent);
        });
    }

    private void setupViewModels() {
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        avicultorViewModel = new ViewModelProvider(this).get(AvicultorViewModel.class);

        avicultorViewModel.getAvicultoresAtivos().observe(this, avicultores -> {
            if (avicultores != null && !avicultores.isEmpty()) {
                AvicultorEntity avicultor = avicultores.get(0);
                txtBoasVindas.setText(getString(R.string.welcome_message, avicultor.getNome()));
                txtPropriedade.setText(avicultor.getNomePropriedade());
                
                // Carrega os dados do dashboard para este avicultor
                dashboardViewModel.carregarDados(avicultor.getId());
            }
        });

        dashboardViewModel.getDashboardData().observe(this, data -> {
            if (data != null) {
                txtTotalLotes.setText(String.valueOf(data.totalLotes));
                txtAvesAlojadas.setText(String.valueOf(data.avesAlojadas));
                txtMortalidade.setText(String.format(Locale.getDefault(), "%.2f%%", data.mortalidadeGeral));
                txtAtivosEncerrados.setText(String.format(Locale.getDefault(), "%d / %d", data.ativos, data.encerrados));
                
                if (data.lotes != null) {
                    // Exibe apenas os 5 lotes mais recentes no dashboard
                    if (data.lotes.size() > 5) {
                        adapter.setLotes(data.lotes.subList(0, 5));
                    } else {
                        adapter.setLotes(data.lotes);
                    }
                }
            }
        });

        avicultorViewModel.carregarAvicultores();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Atualiza os dados sempre que a tela voltar ao topo
        avicultorViewModel.carregarAvicultores();
    }
}

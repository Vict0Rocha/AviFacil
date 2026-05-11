package com.example.avifacil.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import com.example.avifacil.R;
import com.example.avifacil.data.local.entity.AvicultorEntity;
import com.example.avifacil.ui.lote.DetalheLoteActivity;
import com.example.avifacil.ui.lote.LotesActivity;
import com.example.avifacil.ui.viewmodel.AvicultorViewModel;
import com.example.avifacil.ui.viewmodel.DashboardViewModel;
import com.example.avifacil.worker.SyncWorker;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private DashboardViewModel dashboardViewModel;
    private AvicultorViewModel avicultorViewModel;
    private LoteResumoAdapter adapter;

    private TextView txtBoasVindas, txtPropriedade, txtTotalLotes, txtAvesAlojadas, txtMortalidade, txtAtivosEncerrados;
    private RecyclerView recyclerLotes;
    private TextView btnVerTodos, txtSyncStatus;
    private ImageView imgSyncStatus, btnSair;
    private Button btnSyncNow;
    private GoogleSignInClient mGoogleSignInClient;

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
        txtSyncStatus = findViewById(R.id.txtSyncStatus);
        imgSyncStatus = findViewById(R.id.imgSyncStatus);
        btnSyncNow = findViewById(R.id.btnSyncNow);
        btnSair = findViewById(R.id.btnSair);

        // Configurar Google Sign-In para possibilitar o logout completo
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnVerTodos.setOnClickListener(v -> {
            startActivity(new Intent(this, LotesActivity.class));
        });

        btnSyncNow.setOnClickListener(v -> iniciarSincronizacao());

        btnSair.setOnClickListener(v -> confirmarSaida());
        
        // Observar status do WorkManager se necessário ou apenas atualizar UI
        txtSyncStatus.setText("Sincronização configurada");
    }

    private void confirmarSaida() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Sair do AviFácil")
                .setMessage("Deseja realmente sair e trocar de conta?")
                .setPositiveButton("Sair", (dialog, which) -> deslogar())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deslogar() {
        // Logout do Firebase
        FirebaseAuth.getInstance().signOut();
        
        // Logout do Google (para permitir escolher outra conta na próxima vez)
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent intent = new Intent(DashboardActivity.this, com.example.avifacil.ui.auth.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void iniciarSincronizacao() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest syncRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueue(syncRequest);
        
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(syncRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null) {
                        WorkInfo.State state = workInfo.getState();
                        
                        if (state == WorkInfo.State.RUNNING) {
                            txtSyncStatus.setText("Sincronizando...");
                            btnSyncNow.setEnabled(false);
                        } else if (state == WorkInfo.State.ENQUEUED) {
                            txtSyncStatus.setText("Aguardando conexão...");
                            btnSyncNow.setEnabled(false);
                        } else if (state.isFinished()) {
                            btnSyncNow.setEnabled(true);
                            if (state == WorkInfo.State.SUCCEEDED) {
                                txtSyncStatus.setText("Sincronizado com sucesso");
                                Toast.makeText(this, "Sincronização concluída!", Toast.LENGTH_SHORT).show();
                                
                                // Recarregar dados após sync
                                AvicultorEntity av = avicultorViewModel.getAvicultorLogado().getValue();
                                if (av != null) {
                                    dashboardViewModel.carregarDados(av.getId());
                                }
                            } else {
                                txtSyncStatus.setText("Falha na sincronização");
                                Toast.makeText(this, "Erro ao sincronizar. Verifique sua internet.", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
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

        avicultorViewModel.getAvicultorLogado().observe(this, avicultor -> {
            if (avicultor != null) {
                txtBoasVindas.setText(getString(R.string.welcome_message, avicultor.getNome()));
                txtPropriedade.setText(avicultor.getNomePropriedade());
                
                // Carrega os dados do dashboard para este avicultor
                dashboardViewModel.carregarDados(avicultor.getId());
            } else {
                // Caso não encontre o avicultor (ex: UUID não bate mais), redireciona
                startActivity(new Intent(this, com.example.avifacil.ui.auth.LoginActivity.class));
                finish();
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

        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid != null) {
            avicultorViewModel.carregarAvicultorPorUuid(currentUid);
        } else {
            startActivity(new Intent(this, com.example.avifacil.ui.auth.LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Atualiza os dados sempre que a tela voltar ao topo
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid != null) {
            avicultorViewModel.carregarAvicultorPorUuid(currentUid);
        }
    }
}

package com.example.avifacil.ui.lote;

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
import com.example.avifacil.data.local.entity.LoteEntity;
import com.example.avifacil.data.local.entity.RegistroEntity;
import com.example.avifacil.ui.registro.ListaRegistrosActivity;
import com.example.avifacil.ui.viewmodel.LoteViewModel;
import com.example.avifacil.ui.viewmodel.RegistroViewModel;
import com.example.avifacil.ui.viewmodel.AvicultorViewModel;
import com.example.avifacil.util.ZootecniaCalculator;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DetalheLoteActivity extends AppCompatActivity {

    private TextView txtNumero, txtLinhagem, txtGalpao, txtData;
    private TextView txtAvesInicial, txtAvesVivas, txtAvesMortas, txtIdade, txtMortalidade, txtPesoMedio, txtViabilidade, txtConsumoTotal, txtCA, txtGPD, txtFatorProducao, txtCustoRacao, txtPesoInicial;
    private Button btnGerenciar;
    private LoteViewModel loteViewModel;
    private RegistroViewModel registroViewModel;
    private AvicultorViewModel avicultorViewModel;
    private long loteId;
    private LoteEntity currentLote;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_lote);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });

        loteId = getIntent().getLongExtra("LOTE_ID", -1);
        if (loteId == -1) {
            finish();
            return;
        }

        initViews();
        
        loteViewModel = new ViewModelProvider(this).get(LoteViewModel.class);
        registroViewModel = new ViewModelProvider(this).get(RegistroViewModel.class);
        avicultorViewModel = new ViewModelProvider(this).get(AvicultorViewModel.class);

        avicultorViewModel.getAvicultorLogado().observe(this, avicultor -> {
            if (avicultor != null) {
                loteViewModel.carregarLote(loteId, avicultor.getId());
            }
        });

        loteViewModel.getLoteAtual().observe(this, lote -> {
            if (lote != null) {
                currentLote = lote;
                preencherDadosLote();
                registroViewModel.carregarRegistros(loteId);
            }
        });

        registroViewModel.getRegistrosLote().observe(this, registros -> {
            if (registros != null && currentLote != null) {
                atualizarIndicadores(registros);
            }
        });

        btnGerenciar.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListaRegistrosActivity.class);
            intent.putExtra("LOTE_ID", loteId);
            intent.putExtra("NUMERO_LOTE", currentLote != null ? currentLote.getNumeroLote() : "");
            startActivity(intent);
        });
    }

    private void initViews() {
        txtNumero = findViewById(R.id.txtDetalheNumeroLote);
        txtLinhagem = findViewById(R.id.txtDetalheLinhagem);
        txtGalpao = findViewById(R.id.txtDetalheGalpao);
        txtData = findViewById(R.id.txtDetalheDataAlojamento);
        
        txtAvesInicial = findViewById(R.id.txtIndicadorAvesInicial);
        txtIdade = findViewById(R.id.txtIndicadorIdade);
        txtAvesVivas = findViewById(R.id.txtIndicadorAvesVivas);
        txtAvesMortas = findViewById(R.id.txtIndicadorAvesMortas);
        txtMortalidade = findViewById(R.id.txtIndicadorMortalidade);
        txtPesoMedio = findViewById(R.id.txtIndicadorPesoMedio);
        txtViabilidade = findViewById(R.id.txtIndicadorViabilidade);
        txtConsumoTotal = findViewById(R.id.txtIndicadorConsumoTotal);
        txtCA = findViewById(R.id.txtIndicadorCA);
        txtGPD = findViewById(R.id.txtIndicadorGPD);
        txtFatorProducao = findViewById(R.id.txtIndicadorFatorProducao);
        txtCustoRacao = findViewById(R.id.txtIndicadorCustoRacao);
        txtPesoInicial = findViewById(R.id.txtIndicadorPesoInicial);
        
        btnGerenciar = findViewById(R.id.btnGerenciarRegistros);
    }

    private void preencherDadosLote() {
        txtNumero.setText(getString(R.string.label_lote_numero, currentLote.getNumeroLote()));
        txtLinhagem.setText(currentLote.getLinhagem());
        txtGalpao.setText(currentLote.getGalpao());
        txtData.setText(dateFormat.format(currentLote.getDataInicio()));
        txtAvesInicial.setText(String.valueOf(currentLote.getQuantidadeAvesInicial()));
        // Peso inicial em GRAMAS
        txtPesoInicial.setText(String.format(Locale.getDefault(), "%.1f g", currentLote.getPesoInicial()));
    }

    private void atualizarIndicadores(List<RegistroEntity> registros) {
        int vivas = ZootecniaCalculator.calcularAvesVivas(currentLote, registros);
        int mortas = ZootecniaCalculator.calcularTotalMortas(registros);
        int idade = ZootecniaCalculator.calcularIdadeDias(currentLote, new java.util.Date());
        double mortalidade = ZootecniaCalculator.calcularMortalidade(currentLote, registros);
        double pesoMedioG = registros.isEmpty() ? 0 : registros.get(registros.size() - 1).getPesoAtualMedio();
        double viabilidade = ZootecniaCalculator.calcularViabilidade(currentLote, registros);
        double consumoTotalKg = ZootecniaCalculator.calcularTotalConsumoRacao(registros);
        double ca = ZootecniaCalculator.calcularConversaoAlimentar(currentLote, registros);
        double gpdG = ZootecniaCalculator.calcularGPD(currentLote, registros, new java.util.Date());
        double fatorProducao = ZootecniaCalculator.calcularFatorProducao(currentLote, registros, new java.util.Date());
        double custoTotal = ZootecniaCalculator.calcularCustoTotalInsumos(registros);

        txtAvesVivas.setText(String.valueOf(vivas));
        txtAvesMortas.setText(String.valueOf(mortas));
        txtIdade.setText(String.format(Locale.getDefault(), "%d dias", idade));
        txtMortalidade.setText(String.format(Locale.getDefault(), "%.2f%%", mortalidade));
        txtPesoMedio.setText(pesoMedioG > 0 ? String.format(Locale.getDefault(), "%.1f g", pesoMedioG) : "---");
        txtViabilidade.setText(String.format(Locale.getDefault(), "%.1f%%", viabilidade));
        txtConsumoTotal.setText(String.format(Locale.getDefault(), "%.2f kg", consumoTotalKg));
        txtCA.setText(ca > 0 ? String.format(Locale.getDefault(), "%.3f", ca) : "---");
        txtGPD.setText(gpdG > 0 ? String.format(Locale.getDefault(), "%.2f g/dia", gpdG) : "---");
        txtFatorProducao.setText(fatorProducao > 0 ? String.format(Locale.getDefault(), "%.2f", fatorProducao) : "---");
        txtCustoRacao.setText(String.format(Locale.getDefault(), "R$ %.2f", custoTotal));
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (currentUid != null) {
            avicultorViewModel.carregarAvicultorPorUuid(currentUid);
        }
    }
}

package com.example.avifacil.ui.lote;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.avifacil.R;
import com.example.avifacil.data.local.entity.LoteEntity;
import com.example.avifacil.data.local.entity.RegistroEntity;
import com.example.avifacil.ui.registro.ListaRegistrosActivity;
import com.example.avifacil.ui.viewmodel.LoteViewModel;
import com.example.avifacil.ui.viewmodel.RegistroViewModel;
import com.example.avifacil.util.ZootecniaCalculator;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class DetalheLoteActivity extends AppCompatActivity {

    private TextView txtNumero, txtLinhagem, txtGalpao, txtData;
    private TextView txtAvesInicial, txtAvesVivas, txtAvesMortas, txtIdade, txtMortalidade, txtPesoMedio, txtGMD, txtViabilidade, txtConsumoTotal, txtCA;
    private Button btnGerenciar;
    private LoteViewModel loteViewModel;
    private RegistroViewModel registroViewModel;
    private long loteId;
    private LoteEntity currentLote;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_lote);

        loteId = getIntent().getLongExtra("LOTE_ID", -1);
        if (loteId == -1) {
            finish();
            return;
        }

        initViews();
        
        loteViewModel = new ViewModelProvider(this).get(LoteViewModel.class);
        registroViewModel = new ViewModelProvider(this).get(RegistroViewModel.class);

        // Observa o lote para preencher as informações básicas
        loteViewModel.getLoteAtual().observe(this, lote -> {
            if (lote != null) {
                currentLote = lote;
                preencherDadosLote();
                // Após carregar o lote, carrega os registros para calcular indicadores
                registroViewModel.carregarRegistros(loteId);
            }
        });

        // Observa os registros para calcular os indicadores
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
        txtGMD = findViewById(R.id.txtIndicadorGMD);
        txtViabilidade = findViewById(R.id.txtIndicadorViabilidade);
        txtConsumoTotal = findViewById(R.id.txtIndicadorConsumoTotal);
        txtCA = findViewById(R.id.txtIndicadorCA);
        
        btnGerenciar = findViewById(R.id.btnGerenciarRegistros);
    }

    private void preencherDadosLote() {
        txtNumero.setText(getString(R.string.label_lote_numero, currentLote.getNumeroLote()));
        txtLinhagem.setText(currentLote.getLinhagem());
        txtGalpao.setText(currentLote.getGalpao());
        txtData.setText(dateFormat.format(currentLote.getDataInicio()));
        txtAvesInicial.setText(String.valueOf(currentLote.getQuantidadeAvesInicial()));
    }

    private void atualizarIndicadores(List<RegistroEntity> registros) {
        int vivas = ZootecniaCalculator.calcularAvesVivas(currentLote, registros);
        int mortas = ZootecniaCalculator.calcularTotalMortas(registros);
        int idade = ZootecniaCalculator.calcularIdadeDias(currentLote);
        double mortalidade = ZootecniaCalculator.calcularMortalidade(currentLote, registros);
        double pesoMedio = ZootecniaCalculator.calcularPesoMedioAtual(registros);
        double gmd = ZootecniaCalculator.calcularGanhoMedioDiario(currentLote, registros);
        double viabilidade = ZootecniaCalculator.calcularViabilidade(currentLote, registros);
        double consumoTotal = ZootecniaCalculator.calcularTotalConsumoRacao(registros);
        double ca = ZootecniaCalculator.calcularConversaoAlimentar(currentLote, registros);

        txtAvesVivas.setText(String.valueOf(vivas));
        txtAvesMortas.setText(String.valueOf(mortas));
        txtIdade.setText(String.format(Locale.getDefault(), "%d dias", idade));
        txtMortalidade.setText(String.format(Locale.getDefault(), "%.2f%%", mortalidade));
        txtPesoMedio.setText(pesoMedio > 0 ? String.format(Locale.getDefault(), "%.1f g", pesoMedio) : "---");
        txtGMD.setText(gmd > 0 ? String.format(Locale.getDefault(), "%.2f g", gmd) : "---");
        txtViabilidade.setText(String.format(Locale.getDefault(), "%.1f%%", viabilidade));
        txtConsumoTotal.setText(String.format(Locale.getDefault(), "%.2f kg", consumoTotal));
        txtCA.setText(ca > 0 ? String.format(Locale.getDefault(), "%.3f", ca) : "---");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega os dados para garantir que os indicadores estejam atualizados
        loteViewModel.carregarLote(loteId);
    }
}

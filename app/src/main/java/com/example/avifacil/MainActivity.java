package com.example.avifacil;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private TextView label;
    private AppDatabase db;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        label = new TextView(this);
        label.setText("Iniciando teste do Room...");
        label.setGravity(android.view.Gravity.CENTER);
        label.setTextSize(20);
        setContentView(label);

        db = AppDatabase.getInstance(this);

        testDatabase();
    }

    private void testDatabase() {
        executorService.execute(() -> {
            try {
                // 1. Inserir um Avicultor (Nova Entidade da Task 03)
                AvicultorEntity novoAvicultor = new AvicultorEntity("Victor", "victor@example.com");
                db.avicultorDao().insert(novoAvicultor);

                // 2. Buscar todos os avicultores ativos
                List<AvicultorEntity> lista = db.avicultorDao().getAllAtivos();

                // 3. Atualizar a UI
                StringBuilder sb = new StringBuilder("Avicultores no Room:\n");
                for (AvicultorEntity a : lista) {
                    sb.append("- ").append(a.getNome()).append(" (").append(a.getEmail()).append(")\n");
                }

                runOnUiThread(() -> label.setText(sb.toString()));
            } catch (Exception e) {
                runOnUiThread(() -> label.setText("Erro no banco: " + e.getMessage()));
            }
        });
    }
}

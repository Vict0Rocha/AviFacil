package com.example.avifacil;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView label = new TextView(this);
        label.setText("Teste do Projeto AviFacil");
        label.setGravity(android.view.Gravity.CENTER);
        label.setTextSize(24);

        setContentView(label);
    }
}

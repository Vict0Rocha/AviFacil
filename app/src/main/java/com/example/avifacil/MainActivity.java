package com.example.avifacil;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.avifacil.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Redireciona sempre para a LoginActivity que gerenciará o estado
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}

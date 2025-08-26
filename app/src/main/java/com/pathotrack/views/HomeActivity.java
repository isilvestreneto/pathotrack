package com.pathotrack.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.pathotrack.R;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }

    public void abrirSobre(View view) {
        Intent intentAbertura = new Intent(this, AutoriaActivity.class);
        startActivity(intentAbertura);
    }

    public void abrirAdicionar(View view) {
        Intent intentAbertura = new Intent(this, CriarCasoActivity.class);
        startActivity(intentAbertura);
    }
}

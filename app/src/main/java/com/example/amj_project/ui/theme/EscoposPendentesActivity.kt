package com.example.amj_project.ui.theme

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R

class EscoposPendentesActivity : AppCompatActivity(){
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.escopos_pendentes)

        val tvPendentes: TextView = findViewById(R.id.tvPendentes)

        // Recuperar dados da Intent
        val numeroEscopo = intent.getStringExtra("numeroEscopo")
        val empresa = intent.getStringExtra("empresa")
        val data = intent.getStringExtra("data")

        // Mostrar os dados no TextView
        tvPendentes.text = "Número: $numeroEscopo\nEmpresa: $empresa\nData: $data"

        val voltarMenuButton = findViewById<Button>(R.id.button4)

        voltarMenuButton.setOnClickListener {
            val intent = Intent(this, MenuPrincipalActivity::class.java)
            startActivity(intent)
            finish() // Finaliza a atividade atual para evitar acúmulo de pilha
        }
    }
}
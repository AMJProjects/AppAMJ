package com.example.amj_project.ui.theme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.MainActivity
import com.example.amj_project.R

class MenuPrincipalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_principal)

        // Botões principais
        val escoposPendentesButton = findViewById<Button>(R.id.button)
        val adicionarEscopoButton = findViewById<Button>(R.id.button3)
        val escoposConcluidosButton = findViewById<ImageButton>(R.id.concluidos)
        val tarefasButton = findViewById<ImageButton>(R.id.tarefas)
        val perfilButton = findViewById<ImageButton>(R.id.perfil)

        // Navegação para as atividades correspondentes
        escoposPendentesButton.setOnClickListener {
            val intent = Intent(this, EscoposPendentesActivity::class.java)
            startActivity(intent)
        }

        adicionarEscopoButton.setOnClickListener {
            val intent = Intent(this, AdicionarEscopoActivity::class.java)
            startActivity(intent)
        }

        escoposConcluidosButton.setOnClickListener {
            val intent = Intent(this, EscoposConcluidosActivity::class.java)
            startActivity(intent)
        }

        tarefasButton.setOnClickListener {
            val intent = Intent(this, TarefasActivity::class.java)
            startActivity(intent)
        }

        perfilButton.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }
    }
}

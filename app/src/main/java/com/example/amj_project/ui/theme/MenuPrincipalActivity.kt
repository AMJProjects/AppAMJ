package com.example.amj_project.ui.theme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.EsqueciSenhaActivity
import com.example.amj_project.MainActivity
import com.example.amj_project.R

class MenuPrincipalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_principal)

        // Botões principais
        val escoposPendentesButton = findViewById<Button>(R.id.btn_pendente)
        val adicionarEscopoButton = findViewById<Button>(R.id.btn_add_escopo)
        val escoposConcluidosButton = findViewById<Button>(R.id.btn_concluido)
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

        perfilButton.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            startActivity(intent)
        }
    }
}

package com.example.amj_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R
import com.example.amj_project.ui.theme.MenuPrincipalActivity

class EscoposConcluidosActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.escopos_concluidos)

        // Recebe o escopo passado pela Intent
        val escopoCriado = intent.getStringExtra("escopo")

        // Exibe o escopo na tela (por exemplo, em um TextView)
        val textViewEscopo: TextView = findViewById(R.id.textViewEscopo) // Crie esse TextView no seu layout XML
        textViewEscopo.text = escopoCriado

        // Configura o botão de voltar
        val voltarMenuButton = findViewById<Button>(R.id.button4)
        voltarMenuButton.setOnClickListener {
            val intent = Intent(this, MenuPrincipalActivity::class.java)
            startActivity(intent)
            finish() // Finaliza a atividade atual para evitar acúmulo de pilha
        }
    }
}

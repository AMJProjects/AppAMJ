package com.example.amj_project.ui.theme

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import com.example.amj_project.R
import com.google.firebase.firestore.FirebaseFirestore

class EscoposPendentesActivity : AppCompatActivity() {

    // Inicializa o Firestore
    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.escopos_pendentes)

        val layoutDinamico: LinearLayout = findViewById(R.id.layoutDinamico)
        val voltarMenuButton = findViewById<Button>(R.id.button4)

        // Inicializa o Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Busca todos os escopos com status "Integração pendente" ou "Realização pendente"
        db.collection("escopos")
            .whereIn("status", listOf("Integração pendente", "Realização pendente"))
            .get()
            .addOnSuccessListener { result ->

                // Itera sobre os documentos retornados e cria TextViews dinâmicos
                for (document in result) {
                    val numeroEscopo = document.getString("numeroEscopo") ?: "N/A"
                    val empresa = document.getString("empresa") ?: "N/A"
                    val dataEstimativa = document.getString("dataEstimativa") ?: "N/A"

                    val textoEscopo = "Número: $numeroEscopo\nEmpresa: $empresa\nData Estimada: $dataEstimativa"

                    // Adiciona o escopo como um TextView ao layout dinâmico
                    adicionarTextoDinamico(layoutDinamico, textoEscopo)
                }

                // Caso não haja escopos, exibe uma mensagem
                if (result.isEmpty) {
                    adicionarTextoDinamico(layoutDinamico, "Nenhum escopo pendente encontrado.")
                }
            }
            .addOnFailureListener { e ->
                adicionarTextoDinamico(layoutDinamico, "Erro ao carregar os escopos pendentes: ${e.message}")
            }

        voltarMenuButton.setOnClickListener {
            val intent = Intent(this, MenuPrincipalActivity::class.java)
            startActivity(intent)
            finish() // Finaliza a atividade atual para evitar acúmulo de pilha
        }
    }

    // Função para adicionar TextView dinâmico ao layout
    private fun adicionarTextoDinamico(layout: LinearLayout, texto: String) {
        val textView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 10  // Adicionando marginTop de 10dp
            }
            setText(texto)
            textSize = 16f
            setPadding(16, 16, 16, 16)
            setBackgroundResource(R.drawable.botaoredondo)

            // Corrigido: usando setBackgroundTintList com a cor adequada
            backgroundTintList = androidx.core.content.ContextCompat.getColorStateList(
                context, R.color.gray
            )  // Defina a cor como um recurso de cor no seu projeto
        }
        layout.addView(textView)
    }


}

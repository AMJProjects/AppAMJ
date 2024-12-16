package com.example.amj_project.ui.theme

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R
import com.google.firebase.firestore.FirebaseFirestore

class EscoposPendentesActivity : AppCompatActivity() {

    // Inicializa o Firestore
    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.escopos_pendentes)

        val tvPendentes: TextView = findViewById(R.id.tvPendentes)
        val voltarMenuButton = findViewById<Button>(R.id.button4)

        // Inicializa o Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Busca todos os escopos com status "Integração pendente" ou "Realização pendente"
        db.collection("escopos")
            .whereIn("status", listOf("Integração pendente", "Realização pendente"))
            .get()
            .addOnSuccessListener { result ->
                val stringBuilder = StringBuilder()

                // Itera sobre os documentos retornados e adiciona os dados ao StringBuilder
                for (document in result) {
                    val numeroEscopo = document.getString("numeroEscopo")
                    val empresa = document.getString("empresa")
                    val dataEstimativa = document.getString("dataEstimativa")

                    stringBuilder.append("Número: $numeroEscopo\nEmpresa: $empresa\nData Estimada: $dataEstimativa\n\n")
                }

                // Atualiza o TextView com os escopos pendentes
                tvPendentes.text = stringBuilder.toString()
            }
            .addOnFailureListener { e ->
                tvPendentes.text = "Erro ao carregar os escopos pendentes: ${e.message}"
            }

        voltarMenuButton.setOnClickListener {
            val intent = Intent(this, MenuPrincipalActivity::class.java)
            startActivity(intent)
            finish() // Finaliza a atividade atual para evitar acúmulo de pilha
        }
    }
}

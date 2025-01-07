package com.example.amj_project.ui.theme

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class EscoposConcluidosActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var containerConcluidos: LinearLayout
    private lateinit var containerPendentes: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.escopos_concluidos)

        db = FirebaseFirestore.getInstance()
        containerConcluidos = findViewById(R.id.layoutConcluidos)
        containerPendentes = findViewById(R.id.layoutPendentes)

        carregarEscopos("escoposConcluidos", containerConcluidos)
        carregarEscopos("escoposPendentes", containerPendentes)
    }

    private fun carregarEscopos(colecao: String, container: LinearLayout) {
        db.collection(colecao)
            .orderBy("numeroEscopo", Query.Direction.ASCENDING) // Ordena por número crescente
            .get()
            .addOnSuccessListener { documents ->
                var index = 1
                for (document in documents) {
                    val numeroEscopo = document.get("numeroEscopo").toString()
                    val empresa = document.get("empresa").toString()
                    val dataEstimativa = document.get("dataEstimativa").toString()
                    adicionarTextoDinamico(index, numeroEscopo, empresa, dataEstimativa, document.id, container)
                    index++
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar escopos da coleção $colecao.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun adicionarTextoDinamico(
        index: Int,
        numeroEscopo: String,
        empresa: String,
        dataEstimativa: String,
        escopoId: String,
        container: LinearLayout
    ) {
        val layoutEscopo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundResource(R.drawable.botaoredondo)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 16)
            }
        }

        val textoEscopo = "Escopo $index\nNúmero: $numeroEscopo\nEmpresa: $empresa\nData Estimada: $dataEstimativa"
        val textView = TextView(this).apply {
            text = textoEscopo
            textSize = 16f
        }

        val buttonVisualizar = Button(this).apply {
            text = "Visualizar"
            setOnClickListener {
                val intent = Intent(this@EscoposConcluidosActivity, DetalhesEscopoActivity::class.java)
                intent.putExtra("escopoId", escopoId)
                intent.putExtra("numeroEscopo", numeroEscopo)
                intent.putExtra("empresa", empresa)
                intent.putExtra("dataEstimativa", dataEstimativa)
                startActivity(intent)
            }
        }

        layoutEscopo.addView(textView)
        layoutEscopo.addView(buttonVisualizar)
        container.addView(layoutEscopo)
    }
}

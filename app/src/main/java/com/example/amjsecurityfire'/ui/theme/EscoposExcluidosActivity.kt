package com.amjsecurityfire.amjsecurityfire.ui.theme

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.amjsecurityfire.amjsecurityfire.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class EscoposExcluidosActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var containerExcluidos: LinearLayout
    private lateinit var buttonVoltarMenu: Button
    private val escoposList = mutableListOf<Map<String, String>>() // Lista para armazenar os escopos

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.escopos_excluidos)

        db = FirebaseFirestore.getInstance()
        containerExcluidos = findViewById(R.id.layoutDinamico)
        buttonVoltarMenu = findViewById(R.id.btnVoltarMenu)

        carregarEscoposExcluidos()

        buttonVoltarMenu.setOnClickListener {
            finish() // Voltar ao menu anterior
        }
    }

    private fun carregarEscoposExcluidos() {
        db.collection("escoposExcluidos")
            .orderBy("numeroEscopo", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this@EscoposExcluidosActivity, "Erro ao carregar escopos.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                escoposList.clear()
                containerExcluidos.removeAllViews()

                snapshots?.let {
                    for (document in it) {
                        val escopo = mapOf(
                            "numeroEscopo" to (document.getLong("numeroEscopo")?.toString() ?: ""),
                            "empresa" to document.getString("empresa").orEmpty(),
                            "dataEstimativa" to document.getString("dataEstimativa").orEmpty(),
                            "status" to document.getString("status").orEmpty(),
                            "motivoExclusao" to document.getString("motivoExclusao").orEmpty(),
                            "excluidoPor" to document.getString("excluidoPor").orEmpty(),
                            "escopoId" to document.id
                        )
                        escoposList.add(escopo)
                        adicionarTextoDinamico(escopo)
                    }
                }
            }
    }

    private fun adicionarTextoDinamico(escopo: Map<String, String>) {
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

        val textoEscopo = """
            Número: ${escopo["numeroEscopo"]}
            Empresa: ${escopo["empresa"]}
            Data Estimada: ${escopo["dataEstimativa"]}
            Status: ${escopo["status"]}
            Motivo da Exclusão: ${escopo["motivoExclusao"]}
            Excluído Por: ${escopo["excluidoPor"]}
        """.trimIndent()

        val textView = TextView(this).apply {
            text = textoEscopo
            textSize = 16f
        }

        val buttonVisualizar = Button(this).apply {
            text = "Visualizar Detalhes"
            setOnClickListener {
                val intent = Intent(this@EscoposExcluidosActivity, DetalhesEscopoActivity::class.java)

                intent.putExtra("numeroEscopo", escopo["numeroEscopo"])
                intent.putExtra("empresa", escopo["empresa"])
                intent.putExtra("dataEstimativa", escopo["dataEstimativa"])
                intent.putExtra("status", escopo["status"])
                intent.putExtra("motivoExclusao", escopo["motivoExclusao"])
                intent.putExtra("excluidoPor", escopo["excluidoPor"])
                intent.putExtra("escopoId", escopo["escopoId"])

                startActivity(intent)
            }
        }

        layoutEscopo.addView(textView)
        layoutEscopo.addView(buttonVisualizar)
        containerExcluidos.addView(layoutEscopo)
    }
}

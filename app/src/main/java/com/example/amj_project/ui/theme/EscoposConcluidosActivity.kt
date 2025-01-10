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
    private lateinit var buttonVoltarMenu: Button
    private lateinit var searchView: SearchView
    private val escoposList = mutableListOf<Escopo>() // Lista para armazenar os escopos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.escopos_concluidos)

        db = FirebaseFirestore.getInstance()
        containerConcluidos = findViewById(R.id.layoutDinamico)
        buttonVoltarMenu = findViewById(R.id.button4)
        searchView = findViewById(R.id.searchView)

        carregarEscoposConcluidos()

        // Configura o botão "Voltar ao Menu"
        buttonVoltarMenu.setOnClickListener {
            val intent = Intent(this, MenuPrincipalActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Adiciona o listener para a SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filtrarEscopos(newText)
                return true
            }
        })
    }

    private fun carregarEscoposConcluidos() {
        db.collection("escoposConcluidos")
            .orderBy("numeroEscopo", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                escoposList.clear() // Limpa a lista antes de carregar novos dados
                var index = 1
                for (document in documents) {
                    val numeroEscopo = document.get("numeroEscopo").toString()
                    val empresa = document.get("empresa").toString()
                    val dataEstimativa = document.get("dataEstimativa").toString()
                    val status = document.get("status").toString()
                    val tipoServico = document.get("tipoServico").toString()
                    val resumoEscopo = document.get("resumoEscopo").toString()
                    val numeroPedidoCompra = document.get("numeroPedidoCompra").toString()

                    val escopo = Escopo(
                        numeroEscopo, empresa, dataEstimativa, status, tipoServico,
                        resumoEscopo, numeroPedidoCompra, document.id
                    )
                    escoposList.add(escopo)
                    adicionarTextoDinamico(escopo)
                    index++
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar escopos concluídos.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filtrarEscopos(query: String?) {
        val filtro = query?.toLowerCase()?.trim()
        containerConcluidos.removeAllViews()

        for (escopo in escoposList) {
            if (escopo.numeroEscopo.toLowerCase().contains(filtro ?: "") || escopo.empresa.toLowerCase().contains(filtro ?: "")) {
                adicionarTextoDinamico(escopo)
            }
        }
    }

    private fun adicionarTextoDinamico(escopo: Escopo) {
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

        val textoEscopo = "Número: ${escopo.numeroEscopo}\nEmpresa: ${escopo.empresa}\nData Estimada: ${escopo.dataEstimativa}\nStatus: ${escopo.status}"
        val textView = TextView(this).apply {
            text = textoEscopo
            textSize = 16f
        }

        val buttonVisualizar = Button(this).apply {
            text = "Visualizar"
            setOnClickListener {
                val intent = Intent(this@EscoposConcluidosActivity, DetalhesEscopoActivity::class.java)
                intent.putExtra("escopoId", escopo.escopoId)
                startActivity(intent)
            }
        }

        layoutEscopo.addView(textView)
        layoutEscopo.addView(buttonVisualizar)
        containerConcluidos.addView(layoutEscopo)
    }
}

data class Escopo(
    val numeroEscopo: String,
    val empresa: String,
    val dataEstimativa: String,
    val status: String,
    val tipoServico: String,
    val resumoEscopo: String,
    val numeroPedidoCompra: String,
    val escopoId: String
)


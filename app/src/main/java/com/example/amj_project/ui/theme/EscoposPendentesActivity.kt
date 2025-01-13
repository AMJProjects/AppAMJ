package com.example.amj_project.ui.theme

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class EscoposPendentesActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var containerPendentes: LinearLayout
    private lateinit var buttonVoltarMenu: Button
    private lateinit var searchView: SearchView
    private val escoposList = mutableListOf<Map<String, String>>() // Lista para armazenar os escopos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.escopos_pendentes)

        db = FirebaseFirestore.getInstance()
        containerPendentes = findViewById(R.id.layoutDinamico) // ID do layout para os escopos
        buttonVoltarMenu = findViewById(R.id.button4) // ID do botão "Voltar ao Menu"
        searchView = findViewById(R.id.searchView) // ID do SearchView

        carregarEscoposPendentes()

        // Configura o botão "Voltar ao Menu" para retornar à tela principal
        buttonVoltarMenu.setOnClickListener {
            val intent = Intent(this, MenuPrincipalActivity::class.java)
            startActivity(intent)
            finish() // Finaliza a activity atual para evitar acúmulo no stack
        }

        // Configuração do SearchView para evitar fechamento automático do teclado
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // A pesquisa é feita aqui, quando o usuário pressionar "Enter"
                filtrarEscopos(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Não faz nada enquanto o texto estiver mudando
                return false
            }
        })
    }

    private fun carregarEscoposPendentes() {
        db.collection("escoposPendentes")
            .orderBy("numeroEscopo", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                escoposList.clear() // Limpa a lista antes de carregar novos dados
                for (document in documents) {
                    val escopo = mapOf(
                        "numeroEscopo" to document.get("numeroEscopo").toString(),
                        "empresa" to document.get("empresa").toString(),
                        "dataEstimativa" to document.get("dataEstimativa").toString(),
                        "status" to document.get("status").toString(),
                        "tipoServico" to document.get("tipoServico").toString(),
                        "resumoEscopo" to document.get("resumoEscopo").toString(),
                        "numeroPedidoCompra" to document.get("numeroPedidoCompra").toString(),
                        "escopoId" to document.id
                    )
                    escoposList.add(escopo)
                    adicionarTextoDinamico(escopo)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar escopos pendentes.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filtrarEscopos(query: String?) {
        val filtro = query?.toLowerCase()?.trim()
        containerPendentes.removeAllViews() // Remove todas as views do layout

        // Filtra os escopos de acordo com o texto digitado
        for (escopo in escoposList) {
            if (escopo["numeroEscopo"]?.toLowerCase()?.contains(filtro ?: "") == true ||
                escopo["empresa"]?.toLowerCase()?.contains(filtro ?: "") == true) {
                adicionarTextoDinamico(escopo) // Adiciona o escopo filtrado no layout
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
        """.trimIndent()

        val textView = TextView(this).apply {
            text = textoEscopo
            textSize = 16f
        }

        val buttonVisualizar = Button(this).apply {
            text = "Visualizar"
            setOnClickListener {
                val intent = Intent(this@EscoposPendentesActivity, DetalhesEscopoActivity::class.java)

                // Passa os dados do escopo para a próxima activity
                intent.putExtra("numeroEscopo", escopo["numeroEscopo"])
                intent.putExtra("empresa", escopo["empresa"])
                intent.putExtra("dataEstimativa", escopo["dataEstimativa"])
                intent.putExtra("status", escopo["status"])
                intent.putExtra("tipoServico", escopo["tipoServico"])
                intent.putExtra("resumoEscopo", escopo["resumoEscopo"])
                intent.putExtra("numeroPedidoCompra", escopo["numeroPedidoCompra"])
                intent.putExtra("escopoId", escopo["escopoId"])

                startActivity(intent)
            }
        }

        layoutEscopo.addView(textView)
        layoutEscopo.addView(buttonVisualizar)
        containerPendentes.addView(layoutEscopo)
    }
}

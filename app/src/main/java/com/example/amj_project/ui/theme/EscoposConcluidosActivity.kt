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
    private val escoposList = mutableListOf<Map<String, String>>() // Lista para armazenar os escopos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.escopos_concluidos)

        db = FirebaseFirestore.getInstance()
        containerConcluidos = findViewById(R.id.layoutDinamico)
        buttonVoltarMenu = findViewById(R.id.button4)
        searchView = findViewById(R.id.searchView)

        carregarEscoposConcluidos()

        buttonVoltarMenu.setOnClickListener {
            val intent = Intent(this, MenuPrincipalActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Configuração do SearchView
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

    private fun carregarEscoposConcluidos() {
        db.collection("escoposConcluidos")
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
                        "escopoId" to document.id,
                        "pdfUrl" to document.get("pdfUrl").toString() // Adicionando o pdfUrl
                    )
                    escoposList.add(escopo)
                    adicionarTextoDinamico(escopo)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar escopos concluídos.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filtrarEscopos(query: String?) {
        val filtro = query?.toLowerCase()?.trim()
        containerConcluidos.removeAllViews() // Remove todas as views do layout

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
                val intent = Intent(this@EscoposConcluidosActivity, DetalhesEscopoActivity::class.java)

                // Passa os dados do escopo para a próxima activity
                intent.putExtra("numeroEscopo", escopo["numeroEscopo"])
                intent.putExtra("empresa", escopo["empresa"])
                intent.putExtra("dataEstimativa", escopo["dataEstimativa"])
                intent.putExtra("status", escopo["status"])
                intent.putExtra("tipoServico", escopo["tipoServico"])
                intent.putExtra("resumoEscopo", escopo["resumoEscopo"])
                intent.putExtra("numeroPedidoCompra", escopo["numeroPedidoCompra"])
                intent.putExtra("escopoId", escopo["escopoId"])

                // Passa o pdfUrl para a DetalhesEscopoActivity
                intent.putExtra("pdfUrl", escopo["pdfUrl"])

                startActivity(intent)
            }
        }

        // Botão para alterar o status e mover para a coleção 'escoposPendentes'
        val buttonAlterarStatus = Button(this).apply {
            text = "Marcar como Pendente"
            setOnClickListener {
                val escopoId = escopo["escopoId"] ?: return@setOnClickListener

                // Atualizar o status para "Pendente"
                db.collection("escoposConcluidos").document(escopoId).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val dadosAtualizados = document.data?.toMutableMap() ?: return@addOnSuccessListener

                            dadosAtualizados["status"] = "Pendente"  // Mudando o status
                            // Mover o documento para a coleção 'escoposPendentes'
                            db.collection("escoposPendentes").document(escopoId)
                                .set(dadosAtualizados)
                                .addOnSuccessListener {
                                    // Após mover para 'escoposPendentes', exclui da coleção de concluídos
                                    db.collection("escoposConcluidos").document(escopoId).delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(this@EscoposConcluidosActivity, "Escopo movido para Pendente!", Toast.LENGTH_SHORT).show()
                                            carregarEscoposConcluidos()  // Recarregar a lista de escopos concluídos
                                            carregarEscoposPendentes() // Recarregar a lista de escopos pendentes
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this@EscoposConcluidosActivity, "Erro ao remover escopo da coleção concluída: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this@EscoposConcluidosActivity, "Erro ao mover escopo para Pendente: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
            }
        }

        layoutEscopo.addView(textView)
        layoutEscopo.addView(buttonVisualizar)
        layoutEscopo.addView(buttonAlterarStatus) // Adiciona o botão de alteração de status
        containerConcluidos.addView(layoutEscopo)
    }

    // Função para carregar os escopos da coleção "escoposPendentes"
    private fun carregarEscoposPendentes() {
        db.collection("escoposPendentes")
            .orderBy("numeroEscopo", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                // Processa e exibe os escopos pendentes
                for (document in documents) {
                    val escopo = mapOf(
                        "numeroEscopo" to document.get("numeroEscopo").toString(),
                        "empresa" to document.get("empresa").toString(),
                        "dataEstimativa" to document.get("dataEstimativa").toString(),
                        "status" to document.get("status").toString(),
                        "tipoServico" to document.get("tipoServico").toString(),
                        "resumoEscopo" to document.get("resumoEscopo").toString(),
                        "numeroPedidoCompra" to document.get("numeroPedidoCompra").toString(),
                        "escopoId" to document.id,
                        "pdfUrl" to document.get("pdfUrl").toString()
                    )
                    // Adiciona os escopos pendentes a um layout (pode ser feito de forma similar ao método adicionarTextoDinamico)
                    // Aqui você pode adicionar a lógica para exibir os escopos pendentes.
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar escopos pendentes.", Toast.LENGTH_SHORT).show()
            }
    }
}

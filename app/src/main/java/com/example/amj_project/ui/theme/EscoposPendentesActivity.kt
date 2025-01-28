package com.example.amj_project.ui.theme

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
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
        containerPendentes = findViewById(R.id.layoutDinamico)
        buttonVoltarMenu = findViewById(R.id.button4)
        searchView = findViewById(R.id.searchView)

        carregarEscoposPendentes()

        buttonVoltarMenu.setOnClickListener {
            finish() // Voltar ao menu anterior
        }

        // Configuração do SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filtrarEscopos(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
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
                containerPendentes.removeAllViews() // Remove todas as views do layout
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
                Toast.makeText(this@EscoposPendentesActivity, "Erro ao carregar escopos pendentes.", Toast.LENGTH_SHORT).show()
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
                intent.putExtra("pdfUrl", escopo["pdfUrl"]) // Passa o pdfUrl para a DetalhesEscopoActivity

                startActivity(intent)
            }
        }

        val buttonAlterarStatus = Button(this).apply {
            text = "Marcar como Concluído"
            setOnClickListener {
                val escopoId = escopo["escopoId"] ?: return@setOnClickListener

                // Exibe o AlertDialog para confirmação
                val alertDialog = AlertDialog.Builder(this@EscoposPendentesActivity)
                    .setTitle("Confirmar Alteração de Status")
                    .setMessage("Você tem certeza de que deseja marcar este escopo como Concluído?")
                    .setPositiveButton("Sim") { dialog, _ ->
                        // Atualizar o status para "Concluído"
                        db.collection("escoposPendentes").document(escopoId).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val dadosAtualizados = document.data?.toMutableMap() ?: return@addOnSuccessListener

                                    dadosAtualizados["status"] = "Concluído"  // Mudando o status
                                    db.collection("escoposConcluidos").document(escopoId)
                                        .set(dadosAtualizados)
                                        .addOnSuccessListener {
                                            // Após mover para 'escoposConcluidos', exclui da coleção de pendentes
                                            db.collection("escoposPendentes").document(escopoId).delete()
                                                .addOnSuccessListener {
                                                    Toast.makeText(this@EscoposPendentesActivity, "Escopo movido para Concluído!", Toast.LENGTH_SHORT).show()

                                                    // Atualiza a lista de escopos pendentes sem redirecionar
                                                    carregarEscoposPendentes() // Recarrega os escopos pendentes
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(this@EscoposPendentesActivity, "Erro ao remover escopo da coleção pendente: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this@EscoposPendentesActivity, "Erro ao mover escopo para Concluído: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        dialog.dismiss() // Fecha o diálogo após a ação
                    }
                    .setNegativeButton("Cancelar") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()

                alertDialog.show()
            }
        }

        val buttonExcluir = Button(this).apply {
            text = "Excluir"
            setOnClickListener {
                val escopoId = escopo["escopoId"] ?: return@setOnClickListener

                // Confirmação antes de excluir
                AlertDialog.Builder(this@EscoposPendentesActivity)
                    .setTitle("Excluir Escopo")
                    .setMessage("Tem certeza de que deseja excluir este escopo permanentemente?")
                    .setPositiveButton("Sim") { _, _ ->
                        db.collection("escoposPendentes").document(escopoId).delete()
                            .addOnSuccessListener {
                                Toast.makeText(this@EscoposPendentesActivity, "Escopo excluído com sucesso!", Toast.LENGTH_SHORT).show()
                                carregarEscoposPendentes() // Recarrega a lista após exclusão
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@EscoposPendentesActivity, "Erro ao excluir escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("Não", null)
                    .show()
            }
        }

        layoutEscopo.addView(textView)
        layoutEscopo.addView(buttonVisualizar)
        layoutEscopo.addView(buttonAlterarStatus) // Adiciona o botão de alteração de status
        layoutEscopo.addView(buttonExcluir) // Adiciona o botão de exclusão
        containerPendentes.addView(layoutEscopo)
    }
}

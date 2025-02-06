package com.amjsecurityfire.amjsecurityfire.ui.theme

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.amjsecurityfire.amjsecurityfire.R
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
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this@EscoposPendentesActivity, "Erro ao carregar escopos pendentes.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                escoposList.clear()
                containerPendentes.removeAllViews()

                snapshots?.let {
                    for (document in it) {
                        val escopo = mapOf(
                            "numeroEscopo" to (document.getLong("numeroEscopo")?.toString() ?: ""),
                            "empresa" to document.getString("empresa").orEmpty(),
                            "dataEstimativa" to document.getString("dataEstimativa").orEmpty(),
                            "status" to document.getString("status").orEmpty(),
                            "tipoServico" to document.getString("tipoServico").orEmpty(),
                            "resumoEscopo" to document.getString("resumoEscopo").orEmpty(),
                            "numeroPedidoCompra" to document.getString("numeroPedidoCompra").orEmpty(),
                            "escopoId" to document.id,
                            "pdfUrl" to document.getString("pdfUrl").orEmpty()
                        )
                        escoposList.add(escopo)
                        adicionarTextoDinamico(escopo)
                    }
                }
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

                // Caixa de diálogo para obter o motivo da exclusão
                val input = EditText(this@EscoposPendentesActivity).apply {
                    hint = "Digite o motivo da exclusão"
                    setPadding(20, 0, 0, 25)
                }

                AlertDialog.Builder(this@EscoposPendentesActivity)
                    .setTitle("Excluir Escopo")
                    .setMessage("Tem certeza de que deseja excluir este escopo permanentemente?")
                    .setView(input)
                    .setPositiveButton("Sim") { _, _ ->
                        val motivo = input.text.toString().trim()

                        if (motivo.isEmpty()) {
                            Toast.makeText(this@EscoposPendentesActivity, "Por favor, informe o motivo da exclusão.", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        // Busca os dados atuais do escopo para salvar na coleção de excluídos
                        db.collection("escoposPendentes").document(escopoId).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val escopoData = document.data?.toMutableMap() ?: return@addOnSuccessListener
                                    escopoData["motivoExclusao"] = motivo
                                    escopoData["dataExclusao"] = System.currentTimeMillis()

                                    // Salvar na coleção de escopos excluídos
                                    db.collection("escoposExcluidos").document(escopoId)
                                        .set(escopoData)
                                        .addOnSuccessListener {
                                            // Após salvar, exclui o documento da coleção atual
                                            db.collection("escoposPendentes").document(escopoId).delete()
                                                .addOnSuccessListener {
                                                    Toast.makeText(this@EscoposPendentesActivity, "Escopo excluído com sucesso!", Toast.LENGTH_SHORT).show()
                                                    carregarEscoposPendentes()
                                                }
                                                .addOnFailureListener { e ->
                                                    Toast.makeText(this@EscoposPendentesActivity, "Erro ao excluir escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this@EscoposPendentesActivity, "Erro ao salvar escopo excluído: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this@EscoposPendentesActivity, "Erro ao buscar escopo: ${e.message}", Toast.LENGTH_SHORT).show()
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

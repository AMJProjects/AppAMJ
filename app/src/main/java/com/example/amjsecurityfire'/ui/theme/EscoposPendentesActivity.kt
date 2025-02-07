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
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Toast.makeText(this@EscoposPendentesActivity, "Erro ao carregar escopos.", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                escoposList.clear()
                containerPendentes.removeAllViews()

                snapshots?.let {
                    for (document in it) {
                        val escopo = mapOf(
                            "numeroEscopo" to (document.getLong("numeroEscopo")?.toString() ?: ""), // Conversão segura
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
        Criado por: ${escopo["criador"] ?: "Desconhecido"}
    """.trimIndent()

        val textView = TextView(this).apply {
            text = textoEscopo
            textSize = 16f
        }

        val buttonVisualizar = criarBotao("Visualizar") {
            navegarParaDetalhesEscopo(escopo)
        }

        val buttonAlterarStatus = criarBotao("Marcar como Pendente") {
            alterarStatusEscopo(escopo, "escoposPendentes", "escoposPendentes", "Pendente")
        }

        val buttonExcluir = criarBotao("Excluir") {
            excluirEscopo(escopo)
        }

        layoutEscopo.apply {
            addView(textView)
            addView(buttonVisualizar)
            addView(buttonAlterarStatus)
            addView(buttonExcluir)
        }
        containerPendentes.addView(layoutEscopo)
    }

    private fun criarBotao(texto: String, acao: () -> Unit): Button {
        return Button(this).apply {
            text = texto
            setOnClickListener { acao() }
        }
    }

    private fun navegarParaDetalhesEscopo(escopo: Map<String, String>) {
        val intent = Intent(this, DetalhesEscopoActivity::class.java).apply {
            escopo.forEach { putExtra(it.key, it.value) }
            putExtra("colecaoOrigem", "escoposPendentes")
        }
        startActivity(intent)
    }

    private fun alterarStatusEscopo(
        escopo: Map<String, String>,
        colecaoAtual: String,
        novaColecao: String,
        novoStatus: String
    ) {
        val escopoId = escopo["escopoId"] ?: return
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Confirmar Alteração de Status")
            .setMessage("Você tem certeza de que deseja marcar este escopo como $novoStatus?")
            .setPositiveButton("Sim") { dialog, _ ->
                db.collection(colecaoAtual).document(escopoId).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val dadosAtualizados = document.data?.toMutableMap() ?: return@addOnSuccessListener
                            dadosAtualizados["status"] = novoStatus
                            moverDocumentoParaColecao(dadosAtualizados, escopoId, novaColecao, colecaoAtual)
                        }
                    }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }
            .create()
        alertDialog.show()
    }

    private fun excluirEscopo(escopo: Map<String, String>) {
        val escopoId = escopo["escopoId"] ?: return
        val input = EditText(this).apply {
            hint = "Digite o motivo da exclusão"
            setPadding(20, 0, 0, 25)
        }
        AlertDialog.Builder(this)
            .setTitle("Excluir Escopo")
            .setMessage("Tem certeza de que deseja excluir este escopo permanentemente?")
            .setView(input)
            .setPositiveButton("Sim") { _, _ ->
                val motivo = input.text.toString().trim()
                if (motivo.isEmpty()) {
                    Toast.makeText(this, "Por favor, informe o motivo da exclusão.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                db.collection("escoposPendentes").document(escopoId).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            val escopoData = document.data?.toMutableMap() ?: return@addOnSuccessListener
                            escopoData["motivoExclusao"] = motivo
                            escopoData["dataExclusao"] = System.currentTimeMillis()
                            moverDocumentoParaColecao(escopoData, escopoId, "escoposExcluidos", "escoposPendentes")
                        }
                    }
            }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun moverDocumentoParaColecao(
        dadosAtualizados: MutableMap<String, Any>,
        escopoId: String,
        novaColecao: String,
        colecaoAtual: String
    ) {
        db.collection(novaColecao).document(escopoId)
            .set(dadosAtualizados)
            .addOnSuccessListener {
                db.collection(colecaoAtual).document(escopoId).delete()
                    .addOnSuccessListener {
                        Toast.makeText(this, "Operação concluída com sucesso!", Toast.LENGTH_SHORT).show()
                        carregarEscoposPendentes()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao remover escopo da coleção atual: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao mover escopo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
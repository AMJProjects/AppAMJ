package com.amjsecurityfire.amjsecurityfire.ui.theme

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.amjsecurityfire.amjsecurityfire.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

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
                    Toast.makeText(this@EscoposPendentesActivity, "Erro ao carregar escopos.", Toast.LENGTH_SHORT).show()
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
        val filtro = query?.toLowerCase(Locale.getDefault())?.trim()
        containerPendentes.removeAllViews() // Remove todas as views do layout

        // Filtra os escopos de acordo com o texto digitado
        for (escopo in escoposList) {
            if (escopo["numeroEscopo"]?.toLowerCase(Locale.getDefault())?.contains(filtro ?: "") == true ||
                escopo["empresa"]?.toLowerCase(Locale.getDefault())?.contains(filtro ?: "") == true
            ) {
                adicionarTextoDinamico(escopo)
            }
        }
    }

    private fun adicionarTextoDinamico(escopo: Map<String, String>) {
        // Cria o layout para o item (escopo)
        val layoutEscopo = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 0, 16)
            }
        }

        // Calcula a cor da borda com base na data estimada (formato "dd/MM/yy")
        val dataEstimativaStr = escopo["dataEstimativa"].orEmpty()
        var borderColor = Color.GRAY  // Cor padrão se não houver data ou ocorrer erro
        if (dataEstimativaStr.isNotEmpty()) {
            try {
                val sdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                val dataEstimativa = sdf.parse(dataEstimativaStr)
                val diffMillis = dataEstimativa.time - Date().time
                val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

                borderColor = when {
                    diffDays < 0 -> Color.parseColor("#FF0000")    // Vencido: vermelho
                    diffDays <= 3 -> Color.parseColor("#FFA500")   // Até 3 dias: laranja
                    diffDays <= 7 -> Color.parseColor("#FFFF00")   // Até 7 dias: amarelo
                    else -> Color.parseColor("#00FF00")            // Mais de 7 dias: verde
                }
            } catch (e: Exception) {
                e.printStackTrace()
                borderColor = Color.GRAY
            }
        }

        // Cria um GradientDrawable para definir apenas a borda colorida
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.WHITE)       // Cor de fundo (pode ser alterada para transparente)
            cornerRadius = 28f          // Arredondamento dos cantos (ajuste conforme necessário)
            setStroke(6, borderColor)   // Largura da borda e cor dinâmica
        }
        layoutEscopo.background = drawable

        // Monta o texto com os detalhes do escopo
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
            // Neste exemplo, o status permanece pendente, mas você pode ajustar se necessário
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
            this.text = texto
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

package com.example.amj_project.ui.theme

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.amj_project.R
import com.google.firebase.firestore.FirebaseFirestore

class DetalhesEscopoActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detalhe_escopos)

        val voltarMenuButton = findViewById<Button>(R.id.btnVoltarMenu)
        val voltarEscopo = findViewById<ImageButton>(R.id.voltarEscopo)
        val textViewDetalhes = findViewById<TextView>(R.id.textViewDetalhes)
        val editBtn: ImageButton = findViewById(R.id.editBtn)
        val pdfDownloadButton: Button = findViewById(R.id.btnDownloadPdf)

        val escopoId = intent.getStringExtra("escopoId") ?: ""
        val numeroEscopo = intent.getStringExtra("numeroEscopo") ?: "N/A"
        val empresa = intent.getStringExtra("empresa") ?: "N/A"
        val dataEstimativa = intent.getStringExtra("dataEstimativa") ?: "N/A"
        val tipoServico = intent.getStringExtra("tipoServico") ?: "N/A"
        val status = intent.getStringExtra("status") ?: "N/A"
        val resumoEscopo = intent.getStringExtra("resumoEscopo") ?: "N/A"
        val numeroPedidoCompra = intent.getStringExtra("numeroPedidoCompra") ?: "N/A"
        val pdfUrl = intent.getStringExtra("pdfUrl") ?: ""

        textViewDetalhes.text = """
            Número: $numeroEscopo
            Empresa: $empresa
            Data Estimada: $dataEstimativa
            Tipo de Serviço: $tipoServico
            Status: $status
            Resumo: $resumoEscopo
            Número do Pedido de Compra: $numeroPedidoCompra
        """.trimIndent()

        // Botão para editar escopo
        editBtn.setOnClickListener {
            val intent = Intent(this, EditarEscopoActivity::class.java).apply {
                putExtra("editMode", true)
                putExtra("escopoId", escopoId)
                putExtra("numeroEscopo", numeroEscopo)
                putExtra("empresa", empresa)
                putExtra("dataEstimativa", dataEstimativa)
                putExtra("tipoServico", tipoServico)
                putExtra("status", status)
                putExtra("resumoEscopo", resumoEscopo)
                putExtra("numeroPedidoCompra", numeroPedidoCompra)
                putExtra("pdfUrl", pdfUrl)
            }
            startActivity(intent)
        }

        // Botão para abrir PDF
        pdfDownloadButton.setOnClickListener {
            if (pdfUrl.isNotEmpty()) {
                abrirPdf(pdfUrl)
            } else {
                Toast.makeText(this, "PDF não disponível para visualização.", Toast.LENGTH_SHORT).show()
            }
        }

        voltarMenuButton.setOnClickListener { finish() }
        voltarEscopo.setOnClickListener { finish() }
    }

    // Função para abrir o PDF usando um aplicativo no dispositivo
    private fun abrirPdf(pdfUrl: String) {
        try {
            if (pdfUrl.isEmpty()) {
                Toast.makeText(this, "PDF não disponível para visualização.", Toast.LENGTH_SHORT).show()
                return
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(pdfUrl)
                setDataAndType(Uri.parse(pdfUrl), "application/pdf")
                flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            }

            // Verifica se há aplicativos que podem abrir o PDF
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(intent, "Escolha um aplicativo para abrir o PDF"))
            } else {
                Toast.makeText(this, "Nenhum aplicativo encontrado para abrir o PDF.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("DetalhesEscopo", "Erro ao tentar abrir o PDF: ${e.message}")
            Toast.makeText(this, "Erro ao tentar abrir o PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Função para atualizar os detalhes do escopo quando a tela for retomada
    override fun onResume() {
        super.onResume()

        // Atualiza os dados do escopo ao retornar para esta tela
        val textViewDetalhes = findViewById<TextView>(R.id.textViewDetalhes)
        val escopoId = intent.getStringExtra("escopoId") ?: ""
        val status = intent.getStringExtra("status") ?: "N/A" // Obtendo o status do escopo

        if (escopoId.isNotEmpty()) {
            // Definir a coleção com base no status do escopo
            val colecaoEscopo = when (status) {
                "Pendente" -> "escoposPendentes"
                "Em Andamento" -> "escoposAndamento"
                "Concluído" -> "escoposConcluidos"
                else -> "escoposPendentes"
            }

            // Buscar os dados atualizados do Firestore com base na coleção correta
            FirebaseFirestore.getInstance().collection(colecaoEscopo).document(escopoId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val numeroEscopo = document.get("numeroEscopo")?.toString() ?: "N/A"
                        val empresa = document.getString("empresa") ?: "N/A"
                        val dataEstimativa = document.getString("dataEstimativa") ?: "N/A"
                        val tipoServico = document.getString("tipoServico") ?: "N/A"
                        val status = document.getString("status") ?: "N/A"
                        val resumoEscopo = document.getString("resumoEscopo") ?: "N/A"
                        val numeroPedidoCompra = document.getString("numeroPedidoCompra") ?: "N/A"

                        // Atualiza o texto da tela com os dados mais recentes
                        textViewDetalhes.text = """
                            Número: $numeroEscopo
                            Empresa: $empresa
                            Data Estimada: $dataEstimativa
                            Tipo de Serviço: $tipoServico
                            Status: $status
                            Resumo: $resumoEscopo
                            Número do Pedido de Compra: $numeroPedidoCompra
                        """.trimIndent()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DetalhesEscopo", "Erro ao buscar os detalhes atualizados: ${e.message}")
                    Toast.makeText(this, "Erro ao buscar os detalhes atualizados.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

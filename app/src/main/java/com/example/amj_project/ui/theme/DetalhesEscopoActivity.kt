package com.example.amj_project.ui.theme

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class DetalhesEscopoActivity : AppCompatActivity() {

    private val storage = FirebaseStorage.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detalhe_escopos)

        val voltarMenuButton = findViewById<Button>(R.id.btnVoltarMenu)
        val voltarEscopo = findViewById<ImageButton>(R.id.voltarEscopo)
        val textViewDetalhes = findViewById<TextView>(R.id.textViewDetalhes)
        val editBtn: ImageButton = findViewById(R.id.editBtn)
        val pdfViewButton: Button = findViewById(R.id.btnViewPdf)
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
            val intent = Intent(this, AdicionarEscopoActivity::class.java).apply {
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

        // Botão para visualizar PDF
        pdfViewButton.setOnClickListener {
            if (pdfUrl.isNotEmpty()) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfUrl))
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Erro ao abrir o PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "URL do PDF não disponível.", Toast.LENGTH_SHORT).show()
            }
        }

        // Botão para baixar PDF
        pdfDownloadButton.setOnClickListener {
            if (pdfUrl.isNotEmpty()) {
                baixarPdf(pdfUrl)
            } else {
                Toast.makeText(this, "PDF não disponível para download.", Toast.LENGTH_SHORT).show()
            }
        }

        voltarMenuButton.setOnClickListener { finish() }
        voltarEscopo.setOnClickListener { finish() }
    }

    private fun baixarPdf(pdfUrl: String) {
        try {
            val storageRef = storage.getReferenceFromUrl(pdfUrl)
            val localFile = File(
                getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "escopo_${System.currentTimeMillis()}.pdf"
            )

            storageRef.getFile(localFile).addOnSuccessListener {
                Toast.makeText(
                    this,
                    "PDF baixado com sucesso em: ${localFile.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Erro ao baixar o PDF: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "URL inválida ou erro: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

package com.example.amj_project.ui.theme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R

class DetalhesEscopoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detalhe_escopos)

        val voltarMenuButton = findViewById<Button>(R.id.btnVoltarMenu)
        val voltarEscopo = findViewById<ImageButton>(R.id.voltarEscopo)
        val textViewDetalhes = findViewById<TextView>(R.id.textViewDetalhes)
        val editBtn: ImageButton = findViewById(R.id.editBtn)

        val escopoId = intent.getStringExtra("escopoId") ?: ""
        val numeroEscopo = intent.getStringExtra("numeroEscopo") ?: "N/A"
        val empresa = intent.getStringExtra("empresa") ?: "N/A"
        val dataEstimativa = intent.getStringExtra("dataEstimativa") ?: "N/A"
        val tipoServico = intent.getStringExtra("tipoServico") ?: "N/A"
        val status = intent.getStringExtra("status") ?: "N/A"
        val resumoEscopo = intent.getStringExtra("resumoEscopo") ?: "N/A"
        val numeroPedidoCompra = intent.getStringExtra("numeroPedidoCompra") ?: "N/A"

        textViewDetalhes.text = """
            Número: $numeroEscopo
            Empresa: $empresa
            Data Estimada: $dataEstimativa
            Tipo de Serviço: $tipoServico
            Status: $status
            Resumo: $resumoEscopo
            Número do Pedido de Compra: $numeroPedidoCompra
        """.trimIndent()

        textViewDetalhes.setLineSpacing(8f, 1.0f)

        editBtn.setOnClickListener {
            val intent = Intent(this, EditarEscopoActivity::class.java).apply {
                putExtra("escopoId", escopoId)
                putExtra("numeroEscopo", numeroEscopo)
                putExtra("empresa", empresa)
                putExtra("dataEstimativa", dataEstimativa)
                putExtra("tipoServico", tipoServico)
                putExtra("status", status)
                putExtra("resumoEscopo", resumoEscopo)
                putExtra("numeroPedidoCompra", numeroPedidoCompra)
            }
            startActivity(intent)
        }

        voltarMenuButton.setOnClickListener { finish() }

        voltarEscopo.setOnClickListener { finish() }
    }
}

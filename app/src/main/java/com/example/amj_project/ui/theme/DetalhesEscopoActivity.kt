package com.example.amj_project.ui.theme

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R

class DetalhesEscopoActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detalhe_escopos)

        // Recupera os botões e os componentes da tela
        val voltarMenuButton = findViewById<Button>(R.id.btnVoltarMenu)
        val voltarEscopo = findViewById<ImageButton>(R.id.voltarEscopo)
        val textViewDetalhes = findViewById<TextView>(R.id.textViewDetalhes)

        // Recupera os dados passados pela Intent
        val escopoId = intent.getStringExtra("escopoId") // ID do escopo passado para detalhes
        val numeroEscopo = intent.getStringExtra("numeroEscopo") ?: "N/A"
        val empresa = intent.getStringExtra("empresa") ?: "N/A"
        val dataEstimativa = intent.getStringExtra("dataEstimativa") ?: "N/A"
        val tipoServico = intent.getStringExtra("tipoServico") ?: "N/A"
        val status = intent.getStringExtra("status") ?: "N/A"
        val resumoEscopo = intent.getStringExtra("resumoEscopo") ?: "N/A"
        val numeroPedidoCompra = intent.getStringExtra("numeroPedidoCompra") ?: "N/A"

        // Logs para depuração
        Log.d("DetalhesEscopo", "escopoId: $escopoId")
        Log.d("DetalhesEscopo", "numeroEscopo: $numeroEscopo")
        Log.d("DetalhesEscopo", "empresa: $empresa")
        Log.d("DetalhesEscopo", "dataEstimativa: $dataEstimativa")
        Log.d("DetalhesEscopo", "tipoServico: $tipoServico")
        Log.d("DetalhesEscopo", "status: $status")
        Log.d("DetalhesEscopo", "resumoEscopo: $resumoEscopo")
        Log.d("DetalhesEscopo", "numeroPedidoCompra: $numeroPedidoCompra")

        // Configura o botão de edição para navegar para EditarEscopoActivity
        val editBtn: ImageButton = findViewById(R.id.editBtn)
        editBtn.setOnClickListener {
            if (escopoId != null) {
                val intent = Intent(this, EditarEscopoActivity::class.java).apply {
                    putExtra("escopoId", escopoId) // Envia o ID do escopo para edição
                    putExtra("empresa", empresa)  // Envia dados adicionais para preencher os campos
                    putExtra("numeroEscopo", numeroEscopo)
                    putExtra("dataEstimativa", dataEstimativa)
                    putExtra("tipoServico", tipoServico)
                    putExtra("status", status)
                    putExtra("resumoEscopo", resumoEscopo)
                    putExtra("numeroPedidoCompra", numeroPedidoCompra)
                }
                startActivity(intent)
            } else {
                // Exibe uma mensagem caso o escopoId não tenha sido encontrado
                Toast.makeText(this, "Erro: Escopo não encontrado!", Toast.LENGTH_SHORT).show()
            }
        }

        // Configura o botão de voltar ao menu principal
        voltarMenuButton.setOnClickListener {
            finish() // Finaliza a atividade atual e volta para o MenuPrincipalActivity
        }

        // Configura o clique na setinha branca para voltar à tela anterior
        voltarEscopo.setOnClickListener {
            finish() // Finaliza a atividade atual e retorna para EscoposConcluidosActivity
        }

        // Exibe os detalhes do escopo na tela
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

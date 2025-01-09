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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.escopos_concluidos)

        db = FirebaseFirestore.getInstance()
        containerConcluidos = findViewById(R.id.layoutDinamico) // ID do layout para os escopos
        buttonVoltarMenu = findViewById(R.id.button4) // ID do botão "Voltar ao Menu"

        carregarEscoposConcluidos()

        // Configura o botão "Voltar ao Menu" para retornar à tela principal
        buttonVoltarMenu.setOnClickListener {
            val intent = Intent(this, MenuPrincipalActivity::class.java)
            startActivity(intent)
            finish() // Finaliza a activity atual para evitar acúmulo no stack
        }
    }

    private fun carregarEscoposConcluidos() {
        db.collection("escoposConcluidos")
            .orderBy("numeroEscopo", Query.Direction.ASCENDING) // Ordena por número crescente
            .get()
            .addOnSuccessListener { documents ->
                var index = 1
                for (document in documents) {
                    val numeroEscopo = document.get("numeroEscopo").toString()
                    val empresa = document.get("empresa").toString()
                    val dataEstimativa = document.get("dataEstimativa").toString()
                    val status = document.get("status").toString() // Adicionando status
                    val tipoServico = document.get("tipoServico").toString() // Adicionando tipo de serviço
                    val resumoEscopo = document.get("resumoEscopo").toString() // Adicionando resumo do escopo
                    val numeroPedidoCompra = document.get("numeroPedidoCompra").toString() // Adicionando número do pedido de compra

                    adicionarTextoDinamico(
                        numeroEscopo, empresa, dataEstimativa, status, tipoServico,
                        resumoEscopo, numeroPedidoCompra, document.id
                    )
                    index++
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao carregar escopos concluídos.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun adicionarTextoDinamico(
        numeroEscopo: String,
        empresa: String,
        dataEstimativa: String,
        status: String,
        tipoServico: String,
        resumoEscopo: String,
        numeroPedidoCompra: String,
        escopoId: String
    ) {
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

        // Alteração aqui para exibir apenas "Número" e Status
        val textoEscopo = "Número: $numeroEscopo\nEmpresa: $empresa\nData Estimada: $dataEstimativa\nStatus: $status"
        val textView = TextView(this).apply {
            text = textoEscopo
            textSize = 16f
        }

        val buttonVisualizar = Button(this).apply {
            text = "Visualizar"
            setOnClickListener {
                val intent = Intent(this@EscoposConcluidosActivity, DetalhesEscopoActivity::class.java)
                intent.putExtra("escopoId", escopoId)
                intent.putExtra("numeroEscopo", numeroEscopo)
                intent.putExtra("empresa", empresa)
                intent.putExtra("dataEstimativa", dataEstimativa)
                intent.putExtra("status", status)
                intent.putExtra("tipoServico", tipoServico)
                intent.putExtra("resumoEscopo", resumoEscopo)
                intent.putExtra("numeroPedidoCompra", numeroPedidoCompra)
                startActivity(intent)
            }
        }

        layoutEscopo.addView(textView)
        layoutEscopo.addView(buttonVisualizar)
        containerConcluidos.addView(layoutEscopo)
    }
}

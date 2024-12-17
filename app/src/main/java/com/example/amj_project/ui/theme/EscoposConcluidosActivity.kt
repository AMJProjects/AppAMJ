package com.example.amj_project

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.ui.theme.DetalhesEscopoActivity
import com.example.amj_project.ui.theme.MenuPrincipalActivity
import com.google.firebase.firestore.FirebaseFirestore

class EscoposConcluidosActivity : AppCompatActivity() {

    // Inicializa o Firestore
    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.escopos_concluidos)

        val layoutDinamico: LinearLayout = findViewById(R.id.layoutDinamico)
        val voltarMenuButton = findViewById<Button>(R.id.button4)

        // Inicializa o Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Busca todos os escopos com status "Concluído"
        db.collection("escopos")
            .whereIn("status", listOf("Concluído"))
            .get()
            .addOnSuccessListener { result ->
                // Itera sobre os documentos retornados e cria TextViews dinâmicos
                for (document in result) {
                    val numeroEscopo = document.getString("numeroEscopo") ?: "N/A"
                    val empresa = document.getString("empresa") ?: "N/A"
                    val dataEstimativa = document.getString("dataEstimativa") ?: "N/A"
                    val tipoServico = document.getString("tipoServico") ?: "N/A"
                    val status = document.getString("status") ?: "N/A"
                    val resumoEscopo = document.getString("resumoEscopo") ?: "N/A"
                    val numeroPedidoCompra = document.getString("numeroPedidoCompra") ?: "N/A"

                    val textoEscopo = "Número: $numeroEscopo\nEmpresa: $empresa\nData Estimada: $dataEstimativa"

                    // Adiciona o escopo como um TextView ao layout dinâmico
                    adicionarTextoDinamico(
                        layoutDinamico,
                        textoEscopo,
                        numeroEscopo,
                        empresa,
                        dataEstimativa,
                        tipoServico,
                        status,
                        resumoEscopo,
                        numeroPedidoCompra
                    )
                }

                // Caso não haja escopos, exibe uma mensagem
                if (result.isEmpty) {
                    adicionarTextoDinamico(
                        layoutDinamico,
                        "Nenhum escopo concluído encontrado.",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                    )
                }
            }
            .addOnFailureListener { e ->
                adicionarTextoDinamico(
                    layoutDinamico,
                    "Erro ao carregar os escopos pendentes: ${e.message}",
                    "", "", "", "", "", "", ""
                )
            }

        // Configura o botão de voltar
        voltarMenuButton.setOnClickListener {
            val intent = Intent(this, MenuPrincipalActivity::class.java)
            startActivity(intent)
            finish() // Finaliza a atividade atual para evitar acúmulo de pilha
        }
    }

    // Função para adicionar TextView e ImageButtons dinâmicos ao layout
    private fun adicionarTextoDinamico(
        layout: LinearLayout,
        texto: String,
        numeroEscopo: String,
        empresa: String,
        dataEstimativa: String,
        tipoServico: String,
        status: String,
        resumoEscopo: String,
        numeroPedidoCompra: String
    ) {
        // Criando um container principal com RelativeLayout para posicionamento
        val containerLayout = RelativeLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400  // Aumentei um pouco a altura do container
            ).apply {
                topMargin = 10  // Margem superior
            }
            setPadding(16, 16, 16, 16)
            setBackgroundResource(R.drawable.botaoredondo)  // Background arredondado
            backgroundTintList = androidx.core.content.ContextCompat.getColorStateList(
                context, R.color.gray
            )
        }

        // Criando o TextView dinâmico
        val textView = TextView(this).apply {
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_TOP)  // Alinha o TextView no topo
            }
            setText(texto)
            textSize = 20f
        }

        // Gerando IDs para os botões dinamicamente
        val button1Id = View.generateViewId()

        // Criando o primeiro ImageButton dinâmico
        val imageButton1 = ImageButton(this).apply {
            id = button1Id  // Definindo o ID do botão
            layoutParams = RelativeLayout.LayoutParams(
                200, 150  // Definindo o tamanho do botão
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)  // Alinha na parte inferior
                addRule(RelativeLayout.ALIGN_PARENT_END)     // Alinha no canto direito
                bottomMargin = 0  // Remove qualquer margem inferior
                marginEnd = 16
            }
            setImageResource(R.drawable.view)
            scaleType = ImageView.ScaleType.FIT_CENTER
            background = null
        }

        // Criando o segundo ImageButton dinâmico
        val imageButton2 = ImageButton(this).apply {
            layoutParams = RelativeLayout.LayoutParams(
                200, 150
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)  // Alinha na parte inferior
                addRule(RelativeLayout.START_OF, imageButton1.id)  // Alinha à esquerda do primeiro botão
                bottomMargin = 0  // Remove qualquer margem inferior
                marginEnd = 16
            }
            setImageResource(R.drawable.feito)
            scaleType = ImageView.ScaleType.FIT_CENTER
            background = null
        }

        // Configura o clique no botão de "Visualizar"
        imageButton1.setOnClickListener {
            val intent = Intent(this, DetalhesEscopoActivity::class.java).apply {
                putExtra("numeroEscopo", numeroEscopo)  // Passa o número do escopo para a nova tela
                putExtra("empresa", empresa)
                putExtra("dataEstimativa", dataEstimativa)
                putExtra("tipoServico", tipoServico)
                putExtra("status", status)
                putExtra("resumoEscopo", resumoEscopo)
                putExtra("numeroPedidoCompra", numeroPedidoCompra)
            }
            startActivity(intent)
        }

        // Adicionando as views ao container principal
        containerLayout.addView(textView)
        containerLayout.addView(imageButton1)
        containerLayout.addView(imageButton2)

        // Adicionando o container ao layout principal
        layout.addView(containerLayout)
    }
}

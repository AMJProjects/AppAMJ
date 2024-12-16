package com.example.amj_project

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R
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

        // Busca todos os escopos com status "Integração pendente" ou "Realização pendente"
        db.collection("escopos")
            .whereIn("status", listOf("Concluído"))
            .get()
            .addOnSuccessListener { result ->

                // Itera sobre os documentos retornados e cria TextViews dinâmicos
                for (document in result) {
                    val numeroEscopo = document.getString("numeroEscopo") ?: "N/A"
                    val empresa = document.getString("empresa") ?: "N/A"
                    val dataEstimativa = document.getString("dataEstimativa") ?: "N/A"

                    val textoEscopo = "Número: $numeroEscopo\nEmpresa: $empresa\nData Estimada: $dataEstimativa"

                    // Adiciona o escopo como um TextView ao layout dinâmico
                    adicionarTextoDinamico(layoutDinamico, textoEscopo)
                }

                // Caso não haja escopos, exibe uma mensagem
                if (result.isEmpty) {
                    adicionarTextoDinamico(layoutDinamico, "Nenhum escopo pendente encontrado.")
                }
            }
            .addOnFailureListener { e ->
                adicionarTextoDinamico(layoutDinamico, "Erro ao carregar os escopos pendentes: ${e.message}")
            }

        voltarMenuButton.setOnClickListener {
            val intent = Intent(this, MenuPrincipalActivity::class.java)
            startActivity(intent)
            finish() // Finaliza a atividade atual para evitar acúmulo de pilha
        }
    }

    // Função para adicionar TextView e ImageButtons dinâmicos ao layout
    private fun adicionarTextoDinamico(layout: LinearLayout, texto: String) {
        // Criando o TextView dinâmico
        val textView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 10  // Adicionando marginTop de 10dp
            }
            setText(texto)
            textSize = 20f  // Aumentando o tamanho da fonte
            setPadding(16, 16, 16, 16)
            setBackgroundResource(R.drawable.botaoredondo)

            // Definindo a cor de fundo com tint
            backgroundTintList = androidx.core.content.ContextCompat.getColorStateList(
                context, R.color.gray
            )
        }

        // Adicionando o TextView ao layout
        layout.addView(textView)

        // Criando um LinearLayout para os botões (horizontal)
        val buttonLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 10  // Adicionando margem superior
            }
            orientation = LinearLayout.HORIZONTAL  // Definindo a orientação como horizontal
            gravity = Gravity.CENTER_HORIZONTAL  // Centralizando os botões
        }

        // Criando o primeiro ImageButton dinâmico
        val imageButton1 = ImageButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                100,  // Largura do botão
                100   // Altura do botão
            ).apply {
                marginEnd = 20  // Adicionando margem entre os botões
            }
            setImageResource(R.drawable.view)  // Defina sua imagem aqui
            background = null  // Remover o fundo do botão
        }

        // Criando o segundo ImageButton dinâmico
        val imageButton2 = ImageButton(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                100,  // Largura do botão
                100   // Altura do botão
            )
            setImageResource(R.drawable.feito)  // Defina sua imagem aqui
            background = null  // Remover o fundo do botão
        }

        // Adicionando os botões ao LinearLayout horizontal
        buttonLayout.addView(imageButton1)
        buttonLayout.addView(imageButton2)

        // Adicionando o LinearLayout com os botões ao layout principal
        layout.addView(buttonLayout)
    }



}

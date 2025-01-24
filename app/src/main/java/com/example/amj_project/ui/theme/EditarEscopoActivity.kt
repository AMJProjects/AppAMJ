package com.example.amj_project.ui.theme

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class EditarEscopoActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editar_escopo)

        db = FirebaseFirestore.getInstance()

        // Recebe o ID do escopo pela Intent
        val escopoId = intent.getStringExtra("escopoId")

        // Verifica se o escopoId é nulo ou vazio
        if (escopoId.isNullOrEmpty()) {
            Toast.makeText(this, "Erro: ID do escopo não encontrado!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Referências para os campos de entrada
        val empresaEditText = findViewById<EditText>(R.id.editTextText3)
        val dataEstimativaEditText = findViewById<EditText>(R.id.editTextDate)
        val resumoEditText = findViewById<EditText>(R.id.textInputEditText)
        val numeroPedidoCompraEditText = findViewById<EditText>(R.id.editTextNumber2)
        val tipoServicoSpinner = findViewById<Spinner>(R.id.spinnerTipoManutencao)
        val salvarButton = findViewById<Button>(R.id.button3)
        val cancelarButton = findViewById<Button>(R.id.button5)

        // Dados para o Spinner
        val tiposServicos = listOf("Preventiva", "Corretiva", "Obra")

        // Configurar o Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposServicos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tipoServicoSpinner.adapter = adapter

        // Função para carregar os dados mais recentes do Firestore
        fun carregarDadosDoFirestore() {
            db.collection("escoposPendentes").document(escopoId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val empresa = document.getString("empresa") ?: ""
                        val dataEstimativa = document.getString("dataEstimativa") ?: ""
                        val resumoEscopo = document.getString("resumoEscopo") ?: ""
                        val tipoServico = document.getString("tipoServico") ?: ""
                        val numeroPedidoCompra = document.getString("numeroPedidoCompra") ?: ""

                        // Preencher os campos com os dados do Firestore
                        empresaEditText.setText(empresa)
                        dataEstimativaEditText.setText(dataEstimativa)
                        resumoEditText.setText(resumoEscopo)
                        numeroPedidoCompraEditText.setText(numeroPedidoCompra)

                        // Configurar o Spinner para selecionar o valor correto
                        val tipoServicoIndex = tiposServicos.indexOf(tipoServico)
                        if (tipoServicoIndex != -1) {
                            tipoServicoSpinner.setSelection(tipoServicoIndex)
                        }
                    } else {
                        Toast.makeText(this, "Erro: Documento não encontrado!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao carregar dados: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Carrega os dados ao abrir a tela
        carregarDadosDoFirestore()

        salvarButton.setOnClickListener {
            // Captura os dados atualizados do formulário
            val dadosAtualizados = hashMapOf(
                "empresa" to empresaEditText.text.toString(),
                "dataEstimativa" to dataEstimativaEditText.text.toString(),
                "resumoEscopo" to resumoEditText.text.toString(),
                "tipoServico" to tipoServicoSpinner.selectedItem.toString(),
                "numeroPedidoCompra" to numeroPedidoCompraEditText.text.toString()
            )

            // Atualiza o documento no Firestore
            db.collection("escoposPendentes").document(escopoId)
                .set(dadosAtualizados, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("EditarEscopo", "Escopo atualizado com sucesso: $dadosAtualizados")
                    Toast.makeText(this, "Escopo atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish() // Voltar para a tela anterior
                }
                .addOnFailureListener { e ->
                    Log.e("EditarEscopo", "Erro ao atualizar escopo", e)
                    Toast.makeText(this, "Erro ao atualizar escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        cancelarButton.setOnClickListener {
            finish() // Volta à tela anterior (DetalhesEscopoActivity)
        }
    }
}

package com.example.amj_project.ui.theme

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R
import com.google.firebase.firestore.FirebaseFirestore

class EditarEscopoActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editar_escopo)

        db = FirebaseFirestore.getInstance()

        // Recebe os dados da Intent
        val escopoId = intent.getStringExtra("escopoId")
        val empresa = intent.getStringExtra("empresa")
        val dataEstimativa = intent.getStringExtra("dataEstimativa")
        val resumoEscopo = intent.getStringExtra("resumoEscopo")
        val tipoServico = intent.getStringExtra("tipoServico")
        val numeroPedidoCompra = intent.getStringExtra("numeroPedidoCompra")

        // Referências para os campos de entrada
        val empresaEditText = findViewById<EditText>(R.id.editTextText3)
        val dataEstimativaEditText = findViewById<EditText>(R.id.editTextDate)
        val resumoEditText = findViewById<EditText>(R.id.textInputEditText)
        val numeroPedidoCompraEditText = findViewById<EditText>(R.id.editTextNumber2)
        val tipoServicoSpinner = findViewById<Spinner>(R.id.spinnerTipoManutencao)
        val salvarButton = findViewById<Button>(R.id.button3)
        val cancelarButton = findViewById<Button>(R.id.button5)

        // Configurar ação do botão Cancelar
                cancelarButton.setOnClickListener {
                    // Fecha a tela atual e volta para a anterior
                    finish()
                }

        // Dados para o Spinner (valores fixos definidos na aplicação)
        val tiposServicos = listOf("Preventiva", "Corretiva", "Obra")

        // Configurar o Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposServicos)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tipoServicoSpinner.adapter = adapter

        // Preencher os campos recebidos via Intent
        empresaEditText.setText(empresa)
        dataEstimativaEditText.setText(dataEstimativa)
        resumoEditText.setText(resumoEscopo)
        numeroPedidoCompraEditText.setText(numeroPedidoCompra)

        // Configurar o Spinner para selecionar o valor correto
        val tipoServicoIndex = tiposServicos.indexOf(tipoServico)
        if (tipoServicoIndex != -1) {
            tipoServicoSpinner.setSelection(tipoServicoIndex)
        }

        salvarButton.setOnClickListener {
            if (escopoId != null) {
                // Captura os dados atualizados do formulário
                val dadosAtualizados = hashMapOf(
                    "empresa" to empresaEditText.text.toString(),
                    "dataEstimativa" to dataEstimativaEditText.text.toString(),
                    "resumoEscopo" to resumoEditText.text.toString(),
                    "tipoServico" to tipoServicoSpinner.selectedItem.toString(),
                    "numeroPedidoCompra" to numeroPedidoCompraEditText.text.toString()
                )

                val colecaoAtual = "escoposPendentes" // A coleção de destino no Firebase

                // Atualiza o documento no Firebase
                db.collection(colecaoAtual).document(escopoId)
                    .update(dadosAtualizados as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Escopo atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao atualizar escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Erro: ID do escopo não encontrado!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
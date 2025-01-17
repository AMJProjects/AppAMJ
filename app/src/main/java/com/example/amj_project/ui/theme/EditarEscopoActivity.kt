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
        val empresaEditText = findViewById<EditText>(R.id.editarEmpresa)
        val dataEstimativaEditText = findViewById<EditText>(R.id.editarDataEstimativa)
        val resumoEditText = findViewById<EditText>(R.id.editarResumo)
        val numeroPedidoCompraEditText = findViewById<EditText>(R.id.editTextNumber2) // Usando o ID correto
        val salvarButton = findViewById<Button>(R.id.salvarEdicaoBtn)

        // Preencher os campos recebidos via Intent
        empresaEditText.setText(empresa)
        dataEstimativaEditText.setText(dataEstimativa)
        resumoEditText.setText(resumoEscopo)
        numeroPedidoCompraEditText.setText(numeroPedidoCompra) // Preenche o número do pedido de compra

        salvarButton.setOnClickListener {
            if (escopoId != null) {
                val dadosAtualizados = hashMapOf(
                    "empresa" to empresaEditText.text.toString(),
                    "dataEstimativa" to dataEstimativaEditText.text.toString(),
                    "resumoEscopo" to resumoEditText.text.toString(),
                    "tipoServico" to tipoServico,
                    "numeroPedidoCompra" to numeroPedidoCompraEditText.text.toString()
                )

                val colecaoAtual = "escoposPendentes" // A coleção de destino agora é fixada em "escoposPendentes"

                // Verifique se o documento existe na coleção de origem
                db.collection(colecaoAtual).document(escopoId).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // Apenas atualize os dados na mesma coleção
                            db.collection(colecaoAtual).document(escopoId)
                                .update(dadosAtualizados as Map<String, Any>)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Escopo atualizado com sucesso!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        "Erro ao atualizar escopo: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            Toast.makeText(
                                this,
                                "Erro: Documento não encontrado na coleção atual!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Erro ao verificar existência do documento: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(this, "Erro: Escopo não encontrado!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

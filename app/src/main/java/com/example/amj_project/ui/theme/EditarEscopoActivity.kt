package com.example.amj_project.ui.theme

import android.os.Bundle
import android.util.Log
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
        val status = intent.getStringExtra("status")
        val numeroPedidoCompra = intent.getStringExtra("numeroPedidoCompra")

        // Referências para os campos de entrada
        val empresaEditText = findViewById<EditText>(R.id.editarEmpresa)
        val dataEstimativaEditText = findViewById<EditText>(R.id.editarDataEstimativa)
        val resumoEditText = findViewById<EditText>(R.id.editarResumo)
        val tipoServicoSpinner = findViewById<Spinner>(R.id.spinnerTipoServico)
        val statusSpinner = findViewById<Spinner>(R.id.spinnerStatus)
        val numeroPedidoCompraEditText = findViewById<EditText>(R.id.editTextNumber2) // Usando o ID correto
        val salvarButton = findViewById<Button>(R.id.salvarEdicaoBtn)

        // Adicionar adaptadores para os spinners
        val tiposManutencao = listOf("Preventiva", "Corretiva", "Preditiva")
        val statusManutencao = listOf("Pendente", "Em Andamento", "Concluído")

        tipoServicoSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposManutencao).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

        statusSpinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, statusManutencao).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

        // Preencher os campos recebidos via Intent
        empresaEditText.setText(empresa)
        dataEstimativaEditText.setText(dataEstimativa)
        resumoEditText.setText(resumoEscopo)
        tipoServicoSpinner.setSelection(obterIndiceSpinner(tipoServicoSpinner, tipoServico))
        statusSpinner.setSelection(obterIndiceSpinner(statusSpinner, status))
        numeroPedidoCompraEditText.setText(numeroPedidoCompra) // Preenche o número do pedido de compra

        salvarButton.setOnClickListener {
            val novoStatus = statusSpinner.selectedItem.toString()

            if (escopoId != null) {
                val dadosAtualizados = hashMapOf(
                    "empresa" to empresaEditText.text.toString(),
                    "dataEstimativa" to dataEstimativaEditText.text.toString(),
                    "resumoEscopo" to resumoEditText.text.toString(),
                    "tipoServico" to tipoServicoSpinner.selectedItem.toString(),
                    "status" to novoStatus,
                    "numeroPedidoCompra" to numeroPedidoCompraEditText.text.toString()
                )

                val colecaoAtual =
                    if (status == "Concluído") "escoposConcluidos" else "escoposPendentes"
                val novaColecao = "escoposConcluidos"

                // Verifique se o documento existe na coleção de origem
                db.collection(colecaoAtual).document(escopoId).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // Se o status foi alterado para "Concluído", mova para a nova coleção
                            if (novoStatus == "Concluído") {
                                // Adicionar o escopo na coleção nova (escoposConcluidos)
                                db.collection(novaColecao).document(escopoId)
                                    .set(dadosAtualizados)
                                    .addOnSuccessListener {
                                        // Após adicionar o escopo na nova coleção, remova o escopo da coleção de origem
                                        db.collection(colecaoAtual).document(escopoId).delete()
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    this,
                                                    "Status atualizado e escopo movido para Concluído!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                finish() // Finaliza a Activity após a movimentação
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(
                                                    this,
                                                    "Erro ao remover escopo da coleção atual: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            this,
                                            "Erro ao mover escopo para Concluído: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            } else {
                                // Se o status não mudou para "Concluído", apenas atualize os dados na mesma coleção
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

    private fun obterIndiceSpinner(spinner: Spinner, valor: String?): Int {
        val adapter = spinner.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString().equals(valor, ignoreCase = true)) return i
        }
        return 0
    }
}

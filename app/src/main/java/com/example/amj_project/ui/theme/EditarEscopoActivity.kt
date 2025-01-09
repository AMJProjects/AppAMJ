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

        val escopoId = intent.getStringExtra("escopoId")
        val empresa = intent.getStringExtra("empresa")
        val dataEstimativa = intent.getStringExtra("dataEstimativa")
        val resumoEscopo = intent.getStringExtra("resumoEscopo")
        val tipoServico = intent.getStringExtra("tipoServico")
        val status = intent.getStringExtra("status")
        val numeroPedidoCompra = intent.getStringExtra("numeroPedidoCompra")

        val empresaEditText = findViewById<EditText>(R.id.editarEmpresa)
        val dataEstimativaEditText = findViewById<EditText>(R.id.editarDataEstimativa)
        val resumoEditText = findViewById<EditText>(R.id.editarResumo)
        val tipoServicoSpinner = findViewById<Spinner>(R.id.spinnerTipoServico)
        val statusSpinner = findViewById<Spinner>(R.id.spinnerStatus)
        val salvarButton = findViewById<Button>(R.id.salvarEdicaoBtn)

        // Adicionar adaptadores para os spinners
        val tiposManutencao = listOf("Preventiva", "Corretiva", "Preditiva")
        val statusManutencao = listOf("Pendente", "Em Andamento", "Concluído")

        tipoServicoSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposManutencao).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        statusSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusManutencao).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Preencher os campos recebidos via Intent
        empresaEditText.setText(empresa)
        dataEstimativaEditText.setText(dataEstimativa)
        resumoEditText.setText(resumoEscopo)
        tipoServicoSpinner.setSelection(obterIndiceSpinner(tipoServicoSpinner, tipoServico))
        statusSpinner.setSelection(obterIndiceSpinner(statusSpinner, status))

        salvarButton.setOnClickListener {
            val dadosAtualizados: HashMap<String, String?> = hashMapOf(
                "empresa" to empresaEditText.text.toString(),
                "dataEstimativa" to dataEstimativaEditText.text.toString(),
                "resumoEscopo" to resumoEditText.text.toString(),
                "tipoServico" to tipoServicoSpinner.selectedItem.toString(),
                "status" to statusSpinner.selectedItem.toString(),
                "numeroPedidoCompra" to numeroPedidoCompra
            )

            if (escopoId != null) {
                db.collection("escopos").document(escopoId)
                    .update(dadosAtualizados as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Escopo atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao atualizar escopo: ${e.message}", Toast.LENGTH_SHORT).show()
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

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
        val empresaEditText = findViewById<EditText>(R.id.editarEmpresa)
        val dataEstimativaEditText = findViewById<EditText>(R.id.editarDataEstimativa)
        val resumoEditText = findViewById<EditText>(R.id.editarResumo)
        val tipoServicoSpinner = findViewById<Spinner>(R.id.spinnerTipoServico)
        val statusSpinner = findViewById<Spinner>(R.id.spinnerStatus)
        val salvarButton = findViewById<Button>(R.id.salvarEdicaoBtn)

        if (escopoId != null) {
            buscarDadosEscopo(escopoId) { dados ->
                empresaEditText.setText(dados["empresa"] as String?)
                dataEstimativaEditText.setText(dados["dataEstimativa"] as String?)
                resumoEditText.setText(dados["resumoEscopo"] as String?)
                tipoServicoSpinner.setSelection(
                    obterIndiceSpinner(
                        tipoServicoSpinner,
                        dados["tipoServico"] as String?
                    )
                )
                statusSpinner.setSelection(
                    obterIndiceSpinner(
                        statusSpinner,
                        dados["status"] as String?
                    )
                )
            }
        } else {
            Toast.makeText(this, "Erro ao carregar o escopo para edição.", Toast.LENGTH_SHORT)
                .show()
            finish()
        }

        salvarButton.setOnClickListener {
            val dadosAtualizados = hashMapOf(
                "empresa" to empresaEditText.text.toString(),
                "dataEstimativa" to dataEstimativaEditText.text.toString(),
                "resumoEscopo" to resumoEditText.text.toString(),
                "tipoServico" to tipoServicoSpinner.selectedItem.toString(),
                "status" to statusSpinner.selectedItem.toString()
            )

            db.collection("escopos").document(escopoId!!)
                .update(dadosAtualizados as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(this, "Escopo atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao atualizar escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun buscarDadosEscopo(
        escopoId: String,
        callback: (dados: Map<String, Any>) -> Unit
    ) {
        db.collection("escopos").document(escopoId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    callback(document.data ?: emptyMap())
                } else {
                    Toast.makeText(this, "Escopo não encontrado!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao buscar escopo: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
    }

    private fun obterIndiceSpinner(spinner: Spinner, valor: String?): Int {
        val adapter = spinner.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString() == valor) return i
        }
        return 0
    }
}

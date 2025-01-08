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

        val empresaEditText = findViewById<EditText>(R.id.editarEmpresa)
        val dataEstimativaEditText = findViewById<EditText>(R.id.editarDataEstimativa)
        val resumoEditText = findViewById<EditText>(R.id.editarResumo)
        val tipoServicoSpinner = findViewById<Spinner>(R.id.spinnerTipoServico)
        val statusSpinner = findViewById<Spinner>(R.id.spinnerStatus)
        val salvarButton = findViewById<Button>(R.id.salvarEdicaoBtn)

        try {
            // Preenche os campos com os dados recebidos
            empresaEditText.setText(empresa)
            dataEstimativaEditText.setText(dataEstimativa)
            resumoEditText.setText(resumoEscopo)
            tipoServicoSpinner.setSelection(obterIndiceSpinner(tipoServicoSpinner, tipoServico))
            statusSpinner.setSelection(obterIndiceSpinner(statusSpinner, status))
        } catch (e: Exception) {
            Log.e("EditarEscopoActivity", "Erro ao preencher campos: ${e.message}")
            Toast.makeText(this, "Erro ao carregar dados para edição.", Toast.LENGTH_SHORT).show()
        }

        // Configura o botão salvar para atualizar os dados no Firestore
        salvarButton.setOnClickListener {
            try {
                val dadosAtualizados: MutableMap<String, Any> = hashMapOf(
                    "empresa" to empresaEditText.text.toString(),
                    "dataEstimativa" to dataEstimativaEditText.text.toString(),
                    "resumoEscopo" to resumoEditText.text.toString(),
                    "tipoServico" to tipoServicoSpinner.selectedItem.toString(),
                    "status" to statusSpinner.selectedItem.toString()
                )

                if (escopoId != null) {
                    if (escopoId.isNotEmpty()) {
                        db.collection("escopos").document(escopoId!!)
                            .update(dadosAtualizados)  // Atualiza os dados no Firestore
                            .addOnSuccessListener {
                                Toast.makeText(this, "Escopo atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e("EditarEscopoActivity", "Erro ao atualizar escopo: ${e.message}")
                                Toast.makeText(this, "Erro ao atualizar escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Erro: Escopo não encontrado!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("EditarEscopoActivity", "Erro ao salvar dados: ${e.message}")
                Toast.makeText(this, "Erro ao salvar dados.", Toast.LENGTH_SHORT).show()
            }
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

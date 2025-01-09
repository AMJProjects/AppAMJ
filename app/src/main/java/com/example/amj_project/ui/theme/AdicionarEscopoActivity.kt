package com.example.amj_project.ui.theme

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdicionarEscopoActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var ultimoNumeroEscopo: Int = 0

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_escopo)

        db = FirebaseFirestore.getInstance()

        // Recuperar dados enviados via Intent
        val editMode = intent.getBooleanExtra("editMode", false)
        val escopoId = intent.getStringExtra("escopoId")
        val empresaEdit = intent.getStringExtra("empresa")
        val numeroEscopoEdit = intent.getStringExtra("numeroEscopo")
        val dataEstimativaEdit = intent.getStringExtra("dataEstimativa")
        val tipoServicoEdit = intent.getStringExtra("tipoServico")
        val statusEdit = intent.getStringExtra("status")
        val resumoEscopoEdit = intent.getStringExtra("resumoEscopo")
        val numeroPedidoCompraEdit = intent.getStringExtra("numeroPedidoCompra")

        // Referenciar os campos do layout
        val empresaField = findViewById<EditText>(R.id.editTextText3)
        val dataEstimativaField = findViewById<EditText>(R.id.editTextDate)
        val tipoServicoSpinner = findViewById<Spinner>(R.id.spinnerTipoManutencao)
        val statusSpinner = findViewById<Spinner>(R.id.spinnerTipoManutencao2)
        val resumoField = findViewById<EditText>(R.id.textInputEditText)
        val numeroPedidoField = findViewById<EditText>(R.id.editTextNumber2)
        val salvarButton: Button = findViewById(R.id.button3)

        // Dados para os Spinners
        val tiposManutencao = listOf("Preventiva", "Corretiva", "Preditiva")
        val statusManutencao = listOf("Pendente", "Em Andamento", "Concluído")

        // Configurar os Spinners
        tipoServicoSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposManutencao).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        statusSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusManutencao).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Caso seja modo de edição, preencher os campos com os valores recebidos
        if (editMode) {
            empresaField.setText(empresaEdit)
            dataEstimativaField.setText(dataEstimativaEdit)
            resumoField.setText(resumoEscopoEdit)
            numeroPedidoField.setText(numeroPedidoCompraEdit)

            tipoServicoSpinner.setSelection(getSpinnerIndex(tipoServicoSpinner, tipoServicoEdit))
            statusSpinner.setSelection(getSpinnerIndex(statusSpinner, statusEdit))
        } else {
            // Buscar último número de escopo inicial
            buscarUltimoNumeroEscopo(statusSpinner.selectedItem.toString())
        }

        // Listener para atualizar o número do escopo ao mudar o status
        statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val status = statusSpinner.selectedItem.toString()
                buscarUltimoNumeroEscopo(status)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Configuração do botão de salvar
        salvarButton.setOnClickListener {
            val empresa = empresaField.text.toString()
            val dataEstimativa = dataEstimativaField.text.toString()
            val tipoServico = tipoServicoSpinner.selectedItem.toString()
            val status = statusSpinner.selectedItem.toString()
            val resumo = resumoField.text.toString()
            val numeroPedidoCompra = numeroPedidoField.text.toString()

            if (empresa.isEmpty() || dataEstimativa.isEmpty() || resumo.isEmpty() || numeroPedidoCompra.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val novoEscopo = mapOf(
                "numeroEscopo" to (ultimoNumeroEscopo + 1),
                "empresa" to empresa,
                "dataEstimativa" to dataEstimativa,
                "tipoServico" to tipoServico,
                "status" to status,
                "resumoEscopo" to resumo,
                "numeroPedidoCompra" to numeroPedidoCompra
            )

            val collection = if (status == "Concluído") {
                "escoposConcluidos"
            } else {
                "escoposPendentes"
            }

            if (editMode && escopoId != null) {
                db.collection(collection).document(escopoId).update(novoEscopo)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Escopo atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                        finish()
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao atualizar escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                db.collection(collection).add(novoEscopo).addOnSuccessListener {
                    Toast.makeText(this, "Escopo criado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao salvar escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun buscarUltimoNumeroEscopo(status: String) {
        val collection = if (status == "Concluído") {
            "escoposConcluidos"
        } else {
            "escoposPendentes"
        }

        db.collection(collection)
            .orderBy("numeroEscopo", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    ultimoNumeroEscopo = documents.first().get("numeroEscopo").toString().toInt()
                } else {
                    ultimoNumeroEscopo = 0
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao buscar número de escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                ultimoNumeroEscopo = 0
            }
    }

    private fun getSpinnerIndex(spinner: Spinner, value: String?): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == value) {
                return i
            }
        }
        return 0
    }
}

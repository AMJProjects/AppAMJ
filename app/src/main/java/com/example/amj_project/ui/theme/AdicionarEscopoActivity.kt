package com.example.amj_project.ui.theme

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdicionarEscopoActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var ultimoNumeroEscopo: Int = 0 // Armazena o último número de escopo
    private lateinit var layoutDinamico: LinearLayout // Layout para adicionar TextViews dinamicamente

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_escopo)

        // Inicializa o Firestore
        db = FirebaseFirestore.getInstance()

        // Referência ao layout dinâmico
        layoutDinamico = findViewById(R.id.layoutDinamico)

        // Busca o último número de escopo no Firestore
        buscarUltimoNumeroEscopo()

        val spinnerTipoManutencao: Spinner = findViewById(R.id.spinnerTipoManutencao)
        val tiposManutencao = listOf("Corretiva", "Preventiva", "Obras")
        val adapterManutencao = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            tiposManutencao
        )
        adapterManutencao.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoManutencao.adapter = adapterManutencao

        val spinnerTipoManutencao2: Spinner = findViewById(R.id.spinnerTipoManutencao2)
        val tiposManutencao2 = listOf("Concluído", "Integração pendente", "Realização pendente")
        val adapterStatus = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            tiposManutencao2
        )
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoManutencao2.adapter = adapterStatus

        val salvarButton: Button = findViewById(R.id.button3)
        salvarButton.setOnClickListener {
            val empresa = findViewById<EditText>(R.id.editTextText3).text.toString()
            val dataEstimativa = findViewById<EditText>(R.id.editTextDate).text.toString()
            val tipoServico = spinnerTipoManutencao.selectedItem.toString()
            val status = spinnerTipoManutencao2.selectedItem.toString()
            val resumoEscopo = findViewById<EditText>(R.id.textInputEditText).text.toString()
            val numeroPedidoCompra = findViewById<EditText>(R.id.editTextNumber2).text.toString()

            // Validação básica dos campos
            if (empresa.isEmpty() || dataEstimativa.isEmpty() || resumoEscopo.isEmpty() || numeroPedidoCompra.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gera o número do novo escopo
            val novoNumeroEscopo = ultimoNumeroEscopo + 1

            // Cria o escopo com os dados inseridos
            val escopo = hashMapOf(
                "numeroEscopo" to novoNumeroEscopo,
                "empresa" to empresa,
                "dataEstimativa" to dataEstimativa,
                "tipoServico" to tipoServico,
                "status" to status,
                "resumoEscopo" to resumoEscopo,
                "numeroPedidoCompra" to numeroPedidoCompra
            )

            // Salva no Firestore
            db.collection("escopos")
                .add(escopo)
                .addOnSuccessListener {
                    ultimoNumeroEscopo = novoNumeroEscopo // Atualiza o número do último escopo
                    adicionarTextoDinamico("Escopo $novoNumeroEscopo: $empresa - $dataEstimativa ($status)")
                    Toast.makeText(this, "Escopo salvo com sucesso!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao salvar escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun buscarUltimoNumeroEscopo() {
        db.collection("escopos")
            .orderBy("numeroEscopo", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val ultimoEscopo = documents.first()
                    ultimoNumeroEscopo = (ultimoEscopo.get("numeroEscopo") as? Long)?.toInt() ?: 0
                } else {
                    ultimoNumeroEscopo = 0 // Se não houver nenhum escopo, começa do zero
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao buscar número do escopo.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun adicionarTextoDinamico(texto: String) {
        val textView = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setText(texto)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setPadding(16, 16, 16, 16)
        }
        layoutDinamico.addView(textView)
    }
}

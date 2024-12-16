package com.example.amj_project.ui.theme

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.EscoposConcluidosActivity
import com.example.amj_project.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdicionarEscopoActivity : AppCompatActivity() {

    // Inicializa o Firestore
    private lateinit var db: FirebaseFirestore
    private var ultimoNumeroEscopo: Int = 0 // Armazena o último número de escopo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_escopo)

        // Inicializa o Firebase Firestore
        db = FirebaseFirestore.getInstance()

        // Chama a função para buscar o último número de escopo salvo
        buscarUltimoNumeroEscopo()

        // Configuração do Spinner de Tipos de Manutenção
        val spinnerTipoManutencao: Spinner = findViewById(R.id.spinnerTipoManutencao)
        val tiposManutencao = listOf("Corretiva", "Preventiva", "Obras")

        val adapterManutencao = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            tiposManutencao
        )
        adapterManutencao.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoManutencao.adapter = adapterManutencao

        spinnerTipoManutencao.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {}
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Configuração do Spinner de Status
        val spinnerTipoManutencao2: Spinner = findViewById(R.id.spinnerTipoManutencao2)
        val tiposManutencao2 = listOf("Concluído", "Integração pendente", "Realização pendente")

        val adapterStatus = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            tiposManutencao2
        )
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTipoManutencao2.adapter = adapterStatus

        spinnerTipoManutencao2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {}
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Configuração do botão Salvar
        val salvarButton: Button = findViewById(R.id.button3)
        salvarButton.setOnClickListener {
            // Captura dos dados dos campos
            val empresa = findViewById<EditText>(R.id.editTextText3).text.toString()
            val dataEstimativa = findViewById<EditText>(R.id.editTextDate).text.toString()
            val status = spinnerTipoManutencao2.selectedItem.toString()

            // Verificação de campos obrigatórios
            if (empresa.isEmpty() || dataEstimativa.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Incrementa o último número de escopo para gerar o novo número
            val novoNumeroEscopo = ultimoNumeroEscopo + 1

            // Criação do escopo como um mapa de dados
            val escopo = hashMapOf(
                "numeroEscopo" to novoNumeroEscopo.toString(), // Agora o número é gerado automaticamente
                "empresa" to empresa,
                "dataEstimativa" to dataEstimativa,
                "status" to status
            )

            // Armazenar o escopo no Firestore
            db.collection("escopos")
                .add(escopo)
                .addOnSuccessListener {
                    Toast.makeText(this, "Escopo salvo com sucesso!", Toast.LENGTH_SHORT).show()

                    // Redirecionar para a tela apropriada
                    val intent: Intent
                    if (status == "Concluído") {
                        intent = Intent(this, EscoposConcluidosActivity::class.java)
                    } else {
                        intent = Intent(this, EscoposPendentesActivity::class.java)
                    }
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao salvar escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Função para buscar o último número de escopo salvo no Firestore
    private fun buscarUltimoNumeroEscopo() {
        db.collection("escopos")
            .orderBy("numeroEscopo", Query.Direction.DESCENDING) // Ordena pelo número de escopo em ordem decrescente
            .limit(1) // Limita o resultado a 1 (último escopo)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val ultimoEscopo = documents.first()
                    ultimoNumeroEscopo = ultimoEscopo.getString("numeroEscopo")?.toInt() ?: 0
                } else {
                    ultimoNumeroEscopo = 0 // Se não houver escopos, começa do 0
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao buscar número do escopo.", Toast.LENGTH_SHORT).show()
            }
    }
}

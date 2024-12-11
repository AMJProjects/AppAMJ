package com.example.amj_project.ui.theme

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.EscoposConcluidosActivity
import com.example.amj_project.R

class AdicionarEscopoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_escopo)

        // Configuração do primeiro Spinner (Tipos de Manutenção)
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

        // Configuração do segundo Spinner (Status)
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
            val numeroEscopo = findViewById<EditText>(R.id.editTextNumber).text.toString()
            val empresa = findViewById<EditText>(R.id.editTextText3).text.toString()
            val dataEstimativa = findViewById<EditText>(R.id.editTextDate).text.toString()
            val tipoServico = spinnerTipoManutencao.selectedItem.toString()
            val status = spinnerTipoManutencao2.selectedItem.toString()
            val resumoEscopo = findViewById<EditText>(R.id.textInputEditText).text.toString()

            // Verificação de campos obrigatórios
            if (numeroEscopo.isEmpty() || empresa.isEmpty() || dataEstimativa.isEmpty() || resumoEscopo.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Criação do escopo
            val escopoCriado = "Escopo n°: $numeroEscopo\nEmpresa: $empresa\nData Estimada: $dataEstimativa\nTipo de Serviço: $tipoServico\nResumo: $resumoEscopo"

            // Determina a tela para redirecionar com base no status
            val intent: Intent
            if (status == "Concluído") {
                intent = Intent(this, EscoposConcluidosActivity::class.java)
                // Você pode passar o escopo criado via Intent, se necessário
                intent.putExtra("escopo", escopoCriado)
            } else {
                intent = Intent(this, EscoposPendentesActivity::class.java)
                // Também pode passar o escopo aqui
                intent.putExtra("escopo", escopoCriado)
            }
            startActivity(intent)
            finish() // Finaliza a atividade atual
        }
    }
}

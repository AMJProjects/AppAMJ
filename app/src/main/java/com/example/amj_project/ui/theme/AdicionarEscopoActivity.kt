package com.example.amj_project.ui.theme

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R

class AdicionarEscopoActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
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
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Nenhuma ação ao selecionar
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Ação quando nada é selecionado (opcional)
            }
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
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Nenhuma ação ao selecionar
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Ação quando nada é selecionado (opcional)
            }
        }
    }
}

package com.example.amj_project.ui.theme

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R
import com.google.firebase.firestore.FirebaseFirestore

class EditarEscopoActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editar_escopo)

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance()

        // Referenciar componentes do layout
        val editarEmpresa: EditText = findViewById(R.id.editarEmpresa)
        val salvarEdicaoBtn: Button = findViewById(R.id.salvarEdicaoBtn)

        // Receber dados do escopo enviados pela Intent
        val escopoId = intent.getStringExtra("escopoId")
        val empresa = intent.getStringExtra("empresa")

        if (escopoId == null) {
            Toast.makeText(this, "Erro: ID do escopo não encontrado!", Toast.LENGTH_SHORT).show()
            finish() // Fecha a atividade caso o ID seja inválido
            return
        }

        // Preencher campo com dados existentes
        editarEmpresa.setText(empresa ?: "")

        // Configurar ação do botão de salvar
        salvarEdicaoBtn.setOnClickListener {
            val novaEmpresa = editarEmpresa.text.toString()

            if (novaEmpresa.isNotEmpty()) {
                // Atualizar o documento no Firestore
                db.collection("escopos").document(escopoId)
                    .update("empresa", novaEmpresa)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Alterações salvas com sucesso!", Toast.LENGTH_SHORT)
                            .show()
                        finish() // Fecha a atividade e retorna
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Erro ao salvar alterações: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

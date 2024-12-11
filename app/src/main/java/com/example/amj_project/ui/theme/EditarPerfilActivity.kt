package com.example.amj_project.ui.theme

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R
import com.google.firebase.database.FirebaseDatabase

class EditarPerfilActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editar_perfil)

        // Referências dos campos
        val etNome = findViewById<EditText>(R.id.etNome)
        val etCargo = findViewById<EditText>(R.id.etCargo)
        val btnSalvar = findViewById<Button>(R.id.btn_salvar)

        // Preenchendo campos com dados recebidos
        etNome.setText(intent.getStringExtra("nome"))
        etCargo.setText(intent.getStringExtra("cargo"))

        // ID do usuário
        val userId = intent.getStringExtra("userId") ?: ""

        // Botão para salvar os dados
        btnSalvar.setOnClickListener {
            val nomeAtualizado = etNome.text.toString().trim()
            val cargoAtualizado = etCargo.text.toString().trim()

            if (nomeAtualizado.isNotEmpty() && cargoAtualizado.isNotEmpty() && userId.isNotEmpty()) {
                val database = FirebaseDatabase.getInstance().getReference("usuarios").child(userId)

                val usuarioAtualizado = mapOf(
                    "nome" to nomeAtualizado,
                    "cargo" to cargoAtualizado,
                )

                database.updateChildren(usuarioAtualizado).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show()
                        finish()  // Fecha a tela de edição e volta para a tela de perfil
                    } else {
                        Toast.makeText(this, "Erro ao atualizar dados!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos corretamente!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

package com.example.amj_project  // Certifique-se de que o pacote está correto

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class EsqueciSenhaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.esqueci_senha)  // Certifique-se de que o layout está correto

        // Configuração do botão de voltar
        val backButton = findViewById<ImageButton>(R.id.backButton)  // Encontrando o botão de voltar pelo ID
        backButton.setOnClickListener {
            // Criando o Intent para voltar para a LoginActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)  // Inicia a LoginActivity
            finish()  // Finaliza a EsqueciSenhaActivity para evitar empilhamento desnecessário
        }
    }
}

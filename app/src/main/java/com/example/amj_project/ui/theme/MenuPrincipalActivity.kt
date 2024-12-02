package com.example.amj_project.ui.theme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.MainActivity
import com.example.amj_project.R

class MenuPrincipalActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_principal)

        val logoutButton = findViewById<Button>(R.id.logoutButton) // Botão para sair

        // Ao clicar no botão de logout, redireciona para a tela de login
        logoutButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Finaliza a tela do menu para evitar voltar a ela
        }
    }
}

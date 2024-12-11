package com.example.amj_project.ui.theme

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.MainActivity
import com.example.amj_project.R

class PerfilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil)

        // Dados recebidos
        val nome = intent.getStringExtra("nome")
        val cargo = intent.getStringExtra("cargo")
        val userId = intent.getStringExtra("userId")  // ID do usuário

        // Exibindo dados
        findViewById<TextView>(R.id.tvNome).text = nome
        findViewById<TextView>(R.id.tvCargo).text = cargo

        // Botão Editar Perfil
        findViewById<Button>(R.id.btn_editar_perfil).setOnClickListener {
            val intent = Intent(this, EditarPerfilActivity::class.java)
            intent.putExtra("nome", nome)
            intent.putExtra("cargo", cargo)
            intent.putExtra("userId", userId)  // Passando o ID do usuário para a tela de edição
            startActivity(intent)
        }

        // Botão Voltar para a tela principal
        findViewById<ImageButton>(R.id.btn_voltar).setOnClickListener {
            val intent = Intent(this, MenuPrincipalActivity::class.java)
            startActivity(intent)
        }

        // Botão Logout para a tela de login
        findViewById<ImageButton>(R.id.btn_logout).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}

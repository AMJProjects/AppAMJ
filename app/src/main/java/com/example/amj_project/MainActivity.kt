package com.example.amj_project

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private var isPasswordVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_screen)

        // Encontrando os componentes
        val senhaEditText = findViewById<EditText>(R.id.senhaEditText)
        val eyeIcon = findViewById<ImageView>(R.id.eyeIcon)
        val forgotPasswordTextView = findViewById<TextView>(R.id.forgotPasswordTextView)
        val entrarButton = findViewById<Button>(R.id.entrarButton)

        // Configura o clique no ícone de olho
        eyeIcon.setOnClickListener {
            if (isPasswordVisible) {
                // Se a senha estiver visível, oculta a senha
                senhaEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                eyeIcon.setImageResource(R.drawable.closed_eye_icon) // Ícone de olho fechado
            } else {
                // Se a senha estiver oculta, mostra a senha
                senhaEditText.inputType = InputType.TYPE_CLASS_TEXT
                eyeIcon.setImageResource(R.drawable.eye_icon) // Ícone de olho aberto
            }
            isPasswordVisible = !isPasswordVisible

            // Coloca o cursor no final do texto ao alternar a visibilidade
            senhaEditText.setSelection(senhaEditText.text.length)
        }

        // Navegação para a tela EsqueciSenhaActivity
        forgotPasswordTextView.setOnClickListener {
            val intent = Intent(this, EsqueciSenhaActivity::class.java)
            startActivity(intent)
        }

        // Configuração do botão Entrar
        entrarButton.setOnClickListener {
            // Aqui você pode adicionar a lógica de validação do login (email e senha)

            // Se o login for bem-sucedido, redireciona para o MenuPrincipalActivity
            val intent = Intent(this, MenuPrincipalActivity::class.java)
            startActivity(intent)
            finish() // Finaliza a MainActivity para não voltar para a tela de login
        }
    }
}

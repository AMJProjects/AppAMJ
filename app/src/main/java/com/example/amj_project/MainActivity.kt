package com.example.amj_project

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.ui.theme.MenuPrincipalActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private var isPasswordVisible: Boolean = false
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_screen)

        auth = FirebaseAuth.getInstance()

        // Encontrando os componentes
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val senhaEditText = findViewById<EditText>(R.id.senhaEditText)
        val eyeIcon = findViewById<ImageView>(R.id.eyeIcon)
        val forgotPasswordTextView = findViewById<TextView>(R.id.forgotPasswordTextView)
        val entrarButton = findViewById<Button>(R.id.entrarButton)
        val registerButton = findViewById<Button>(R.id.registerButton)  // Referência do botão "Registrar"

        // Configuração do ícone de olho para mostrar/ocultar a senha
        eyeIcon.setOnClickListener {
            if (isPasswordVisible) {
                senhaEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                eyeIcon.setImageResource(R.drawable.closed_eye_icon) // Ícone de olho fechado
            } else {
                senhaEditText.inputType = InputType.TYPE_CLASS_TEXT
                eyeIcon.setImageResource(R.drawable.olho) // Ícone de olho aberto
            }
            isPasswordVisible = !isPasswordVisible
            senhaEditText.setSelection(senhaEditText.text.length)
        }

        // Navegação para a tela EsqueciSenhaActivity
        forgotPasswordTextView.setOnClickListener {
            val intent = Intent(this, EsqueciSenhaActivity::class.java)
            startActivity(intent)
        }

        // Configuração do botão Entrar
        entrarButton.setOnClickListener {
            val email = emailEditText.text.toString().trim() // Remove espaços extras
            val senha = senhaEditText.text.toString().trim() // Remove espaços extras

            // Verifica se os campos não estão vazios
            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                signInWithEmailAndPassword(email, senha)
            }
        }

        // Configuração do botão Registrar
        registerButton.setOnClickListener {
            val intent = Intent(this, com.example.amj_project.ui.theme.RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmailAndPassword: Success")
                    val user = auth.currentUser
                    val intent = Intent(this, MenuPrincipalActivity::class.java)
                    startActivity(intent)
                    finish() // Finaliza a MainActivity para não voltar para a tela de login
                } else {
                    Log.w(TAG, "signInWithEmailAndPassword: Failure", task.exception)
                    Toast.makeText(baseContext, "Falha na autenticação", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private var TAG = "EmailAndPassword"
    }
}

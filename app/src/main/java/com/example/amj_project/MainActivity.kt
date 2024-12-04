package com.example.amj_project

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.ui.theme.MenuPrincipalActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var isPasswordVisible: Boolean = false
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_screen)

        // Encontrando os componentes
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val senhaEditText = findViewById<EditText>(R.id.senhaEditText)
        val eyeIcon = findViewById<ImageView>(R.id.eyeIcon)
        val forgotPasswordTextView = findViewById<TextView>(R.id.forgotPasswordTextView)
        val entrarButton = findViewById<Button>(R.id.entrarButton)

        // Configura o clique no ícone de olho para mostrar/ocultar senha
        eyeIcon.setOnClickListener {
            if (isPasswordVisible) {
                senhaEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                eyeIcon.setImageResource(R.drawable.closed_eye_icon) // Ícone de olho fechado
            } else {
                senhaEditText.inputType = InputType.TYPE_CLASS_TEXT
                eyeIcon.setImageResource(R.drawable.eye_icon) // Ícone de olho aberto
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
            val email = emailEditText.text.toString()
            val senha = senhaEditText.text.toString()

            // Verifica se os campos não estão vazios
            if (email.isNotEmpty() && senha.isNotEmpty()) {
                login(email, senha)
            } else {
                Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Função para fazer a requisição de login
    private fun login(email: String, senha: String) {
        val url = "http://localhost:3000/login"  // URL do servidor

        // Criação do JSON para enviar os dados
        val json = JSONObject()
        json.put("email", email)
        json.put("senha", senha)

        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        // Monta a requisição POST
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Faz a requisição assíncrona
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Erro na conexão com a API", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val message = jsonResponse.getString("message") // Usando "message" para verificar a resposta

                        runOnUiThread {
                            if (message == "Login bem-sucedido!") {
                                // Login bem-sucedido, redireciona para o MenuPrincipalActivity
                                val intent = Intent(this@MainActivity, MenuPrincipalActivity::class.java)
                                startActivity(intent)
                                finish() // Finaliza a MainActivity para não voltar para a tela de login
                            } else {
                                // Exibe mensagem de erro de login
                                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Erro na resposta da API", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Erro no login. Tente novamente.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}

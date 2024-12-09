package com.example.amj_project.ui.theme

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.MainActivity
import com.example.amj_project.databinding.RegistroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

class RegistroActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: RegistroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializando o binding
        binding = RegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            val email: String = binding.etEmail.text.toString().trim()
            val password: String = binding.etPassword.text.toString().trim()
            val confirmPassword: String = binding.etConfirmPassword.text.toString().trim()

            // Verificando se todos os campos estão preenchidos
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this@RegistroActivity, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                // Verificando se as senhas coincidem
                if (password == confirmPassword) {
                    createUserEmailAndPassword(email, password)
                } else {
                    Toast.makeText(this@RegistroActivity, "Senhas não são iguais!", Toast.LENGTH_SHORT).show()
                }
            }

        }
        // Ação do botão "Voltar"
        binding.btnVoltarMenu.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java) // Substitua MainActivity pela sua tela de login, se necessário.
            startActivity(intent)
            finish() // Finaliza a atividade atual para evitar que o usuário volte
        }
    }

    private fun createUserEmailAndPassword(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "createUserEmailAndPassword: Success")
                val user = auth.currentUser

                // Mensagem de sucesso
                Toast.makeText(this@RegistroActivity, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()

                // Navega para a tela de login ou uma tela principal
                val intent = Intent(this@RegistroActivity, MainActivity::class.java) // Ou outra Activity de sua escolha
                startActivity(intent)
                finish() // Finaliza a tela de registro para evitar que o usuário volte para ela
            } else {
                val exception = task.exception
                Log.w(TAG, "createUserEmailAndPassword: Failure", exception)

                // Exibe o erro detalhado
                exception?.let { e ->
                    if (e is FirebaseAuthWeakPasswordException) {
                        Toast.makeText(this@RegistroActivity, "Senha muito fraca. A senha deve ter pelo menos 6 caracteres.", Toast.LENGTH_SHORT).show()
                    } else if (e is FirebaseAuthUserCollisionException) {
                        Toast.makeText(this@RegistroActivity, "Este e-mail já está registrado.", Toast.LENGTH_SHORT).show()
                    } else if (e is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this@RegistroActivity, "O e-mail fornecido é inválido.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RegistroActivity, "Falha na autenticação: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "EmailAndPassword"
    }
}

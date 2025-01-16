package com.example.amj_project.ui.theme

import android.content.Intent
import android.graphics.*
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.MainActivity
import com.example.amj_project.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class PerfilActivity : AppCompatActivity() {

    private val REQUEST_CODE_EDITAR_PERFIL = 1  // Código de requisição para identificar a atividade de edição
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var profileImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil)

        // Inicializa o FirebaseAuth e FirebaseDatabase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val tvNome = findViewById<TextView>(R.id.tvNome)
        val tvCargo = findViewById<TextView>(R.id.tvCargo)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        profileImageView = findViewById(R.id.imageView4)

        // Obtém o usuário atual autenticado
        val user = auth.currentUser

        if (user != null) {
            // Exibe o email do usuário
            tvEmail.text = user.email

            // Recupera o nome e o cargo do Firebase Realtime Database usando o UID do usuário
            val userId = user.uid
            val userRef = database.child("users").child(userId)

            // Adiciona um listener para buscar as informações do usuário
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Verifica se os campos 'nome' e 'cargo' existem no banco de dados
                        val nome = snapshot.child("nome").getValue(String::class.java) ?: "Nome não disponível"
                        val cargo = snapshot.child("cargo").getValue(String::class.java) ?: "Cargo não disponível"

                        // Atualiza a interface com os dados do usuário
                        tvNome.text = nome
                        tvCargo.text = cargo

                        // Gera a imagem de perfil com a primeira letra do nome
                        val firstLetter = nome.firstOrNull() ?: 'N'
                        val profileImage = generateProfileImage(firstLetter, Color.BLUE, Color.WHITE, 200)
                        profileImageView.setImageBitmap(profileImage)
                    } else {
                        // Caso o snapshot não exista, define mensagens padrão
                        tvNome.text = "Nome não disponível"
                        tvCargo.text = "Cargo não disponível"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Trata o erro e exibe mensagens de erro apropriadas
                    tvNome.text = "Erro ao carregar nome"
                    tvCargo.text = "Erro ao carregar cargo"
                }
            })
        }

        // Botão Editar Perfil
        findViewById<Button>(R.id.btn_editar_perfil).setOnClickListener {
            val intent = Intent(this, EditarPerfilActivity::class.java)
            intent.putExtra("nome", tvNome.text.toString())
            intent.putExtra("cargo", tvCargo.text.toString())
            startActivityForResult(intent, REQUEST_CODE_EDITAR_PERFIL)
        }

        // Botão Voltar para a tela principal
        findViewById<ImageButton>(R.id.btn_voltar).setOnClickListener {
            val intent = Intent(this, MenuPrincipalActivity::class.java)
            startActivity(intent)
        }

        // Botão Logout para a tela de login
        findViewById<ImageButton>(R.id.btn_logout).setOnClickListener {
            auth.signOut()  // Realiza o logout do Firebase
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val nomeAtualizado = data.getStringExtra("nome")
            val cargoAtualizado = data.getStringExtra("cargo")

            // Atualiza a tela de perfil com os dados recebidos
            findViewById<TextView>(R.id.tvNome).text = nomeAtualizado
            findViewById<TextView>(R.id.tvCargo).text = cargoAtualizado
        }
    }

    // Função para gerar a imagem de perfil com a primeira letra do nome
    private fun generateProfileImage(firstLetter: Char, backgroundColor: Int, textColor: Int, imageSize: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Preenche o fundo com a cor de fundo
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = backgroundColor
        canvas.drawRect(0f, 0f, imageSize.toFloat(), imageSize.toFloat(), paint)

        // Define a cor do texto e seu tamanho
        paint.color = textColor
        paint.textSize = imageSize * 0.5f
        paint.textAlign = Paint.Align.CENTER

        // Calcula as coordenadas do texto
        val bounds = Rect()
        paint.getTextBounds(firstLetter.toString(), 0, 1, bounds)
        val x = imageSize / 2f
        val y = imageSize / 2f - (bounds.top + bounds.bottom) / 2f

        // Desenha a primeira letra no centro da imagem
        canvas.drawText(firstLetter.toString(), x, y, paint)

        return bitmap
    }
}

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

    private val REQUEST_CODE_EDITAR_PERFIL = 1
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var profileImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val tvNome = findViewById<TextView>(R.id.tvNome)
        val tvCargo = findViewById<TextView>(R.id.tvCargo)
        val tvEmail = findViewById<TextView>(R.id.tvEmail)
        profileImageView = findViewById(R.id.imageView4)

        val user = auth.currentUser
        if (user != null) {
            tvEmail.text = user.email
            val userId = user.uid
            val userRef = database.child("users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val nome = snapshot.child("nome").getValue(String::class.java) ?: "Nome não disponível"
                        val cargo = snapshot.child("cargo").getValue(String::class.java) ?: "Cargo não disponível"

                        tvNome.text = nome
                        tvCargo.text = cargo

                        val firstLetter = nome.firstOrNull() ?: 'N'
                        val profileImage = generateProfileImage(firstLetter, Color.BLUE, Color.WHITE, 200)
                        profileImageView.setImageBitmap(profileImage)
                    } else {
                        tvNome.text = "Nome não disponível"
                        tvCargo.text = "Cargo não disponível"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    tvNome.text = "Erro ao carregar nome"
                    tvCargo.text = "Erro ao carregar cargo"
                }
            })
        }

        findViewById<Button>(R.id.btn_editar_perfil).setOnClickListener {
            val intent = Intent(this, EditarPerfilActivity::class.java)
            intent.putExtra("nome", tvNome.text.toString())
            intent.putExtra("cargo", tvCargo.text.toString())
            intent.putExtra("userId", user?.uid)
            startActivityForResult(intent, REQUEST_CODE_EDITAR_PERFIL)
        }

        findViewById<ImageButton>(R.id.btn_voltar).setOnClickListener {
            startActivity(Intent(this, MenuPrincipalActivity::class.java))
        }

        findViewById<ImageButton>(R.id.btn_logout).setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_EDITAR_PERFIL && resultCode == RESULT_OK && data != null) {
            findViewById<TextView>(R.id.tvNome).text = data.getStringExtra("nome")
            findViewById<TextView>(R.id.tvCargo).text = data.getStringExtra("cargo")
        }
    }

    private fun generateProfileImage(firstLetter: Char, backgroundColor: Int, textColor: Int, imageSize: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(imageSize, imageSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint().apply {
            style = Paint.Style.FILL
            color = backgroundColor
        }
        canvas.drawRect(0f, 0f, imageSize.toFloat(), imageSize.toFloat(), paint)
        paint.color = textColor
        paint.textSize = imageSize * 0.5f
        paint.textAlign = Paint.Align.CENTER
        val bounds = Rect()
        paint.getTextBounds(firstLetter.toString(), 0, 1, bounds)
        val x = imageSize / 2f
        val y = imageSize / 2f - (bounds.top + bounds.bottom) / 2f
        canvas.drawText(firstLetter.toString(), x, y, paint)
        return bitmap
    }
}

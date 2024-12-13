package com.example.amj_project.ui.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class EditarPerfilActivity : AppCompatActivity() {

    private var imageUri: Uri? = null // Para armazenar a URI da foto
    private lateinit var storageReference: StorageReference

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editar_perfil)

        // Inicializar a referência de armazenamento
        storageReference = FirebaseStorage.getInstance().reference

        // Referências dos campos
        val etNome = findViewById<EditText>(R.id.etNome)
        val etCargo = findViewById<EditText>(R.id.etCargo)
        val btnSalvar = findViewById<Button>(R.id.btn_salvar)
        val btnAlterarFoto = findViewById<Button>(R.id.btn_foto)

        // Preenchendo campos com dados recebidos
        etNome.setText(intent.getStringExtra("nome"))
        etCargo.setText(intent.getStringExtra("cargo"))

        // ID do usuário
        val userId = intent.getStringExtra("userId") ?: ""

        // Ação do botão de salvar
        btnSalvar.setOnClickListener {
            val nomeAtualizado = etNome.text.toString().trim()
            val cargoAtualizado = etCargo.text.toString().trim()

            // Verificação para garantir que os campos não estão vazios
            if (nomeAtualizado.isNotEmpty() && cargoAtualizado.isNotEmpty() && userId.isNotEmpty()) {
                val database = FirebaseDatabase.getInstance().getReference("usuarios").child(userId)

                // Atualização no Firebase
                val usuarioAtualizado = mutableMapOf<String, Any>(
                    "nome" to nomeAtualizado,
                    "cargo" to cargoAtualizado
                )

                // Se a foto foi alterada, fazer upload da nova foto
                imageUri?.let { uri ->
                    val fotoRef = storageReference.child("perfil/$userId.jpg")
                    fotoRef.putFile(uri).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            fotoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                                usuarioAtualizado["fotoPerfil"] = downloadUrl.toString()

                                // Atualiza os dados no Firebase
                                database.updateChildren(usuarioAtualizado).addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        Toast.makeText(this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show()

                                        // Enviar os dados atualizados para a PerfilActivity
                                        val resultIntent = Intent()
                                        resultIntent.putExtra("nome", nomeAtualizado)
                                        resultIntent.putExtra("cargo", cargoAtualizado)
                                        setResult(RESULT_OK, resultIntent)
                                        finish()  // Fecha a tela de edição
                                    } else {
                                        Toast.makeText(this, "Erro ao atualizar dados!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this, "Erro ao fazer upload da foto!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } ?: run {
                    // Se não tiver foto nova, apenas atualiza os outros dados
                    database.updateChildren(usuarioAtualizado).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            Toast.makeText(this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show()

                            // Enviar os dados atualizados para a PerfilActivity
                            val resultIntent = Intent()
                            resultIntent.putExtra("nome", nomeAtualizado)
                            resultIntent.putExtra("cargo", cargoAtualizado)
                            setResult(RESULT_OK, resultIntent)
                            finish()  // Fecha a tela de edição
                        } else {
                            Toast.makeText(this, "Erro ao atualizar dados!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos corretamente!", Toast.LENGTH_SHORT).show()
            }
        }

        // Ação do botão de alterar foto
        btnAlterarFoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            galleryResult.launch(intent)  // Alteração do nome do launcher
        }
    }

    // Lançar a Activity para escolher a foto
    private val galleryResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                data?.data?.let { uri ->
                    imageUri = uri
                    val imageView = findViewById<ImageView>(R.id.imageView)
                    imageView.setImageURI(uri)  // Exibe a foto escolhida
                }
            }
        }
}

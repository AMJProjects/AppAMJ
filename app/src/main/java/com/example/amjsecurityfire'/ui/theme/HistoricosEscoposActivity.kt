package com.amjsecurityfire.amjsecurityfire.ui.theme

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amjsecurityfire.amjsecurityfire.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HistoricosEscoposActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var listView: ListView
    private val escoposList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.historicos_escopos)

        listView = findViewById(R.id.listViewEscopos)
        db = FirebaseFirestore.getInstance()

        // Buscar escopos
        db.collection("escoposPendentes")
            .orderBy("dataCriacao", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val numeroEscopo = document.getLong("numeroEscopo") ?: 0
                    val empresa = document.getString("empresa") ?: "Desconhecida"
                    val dataCriacao = document.getString("dataCriacao") ?: "Data desconhecida"
                    val usuarioNome = document.getString("usuarioNome") ?: "Usuário desconhecido"

                    val escopoInfo = "Escopo $numeroEscopo - $empresa\nCriado em $dataCriacao por $usuarioNome"
                    escoposList.add(escopoInfo)
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, escoposList)
                listView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao carregar escopos: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
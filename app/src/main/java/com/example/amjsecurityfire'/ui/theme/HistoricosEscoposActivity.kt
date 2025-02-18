package com.amjsecurityfire.amjsecurityfire.ui.theme

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amjsecurityfire.amjsecurityfire.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class HistoricosEscoposActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var listView: ListView
    private lateinit var auth: FirebaseAuth
    private val historicoList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.historicos_escopos)

        val buttonVoltarMenu = findViewById<Button>(R.id.button4)
        listView = findViewById(R.id.listViewHistorico)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Verificar se existe um usuário autenticado
        val usuarioAtual = auth.currentUser
        if (usuarioAtual == null) {
            Toast.makeText(this, "Usuário não autenticado!", Toast.LENGTH_SHORT).show()
            return // Impede que continue caso não haja um usuário autenticado
        }

        // Buscar histórico dos escopos
        db.collection("historicoEscopos")
            .orderBy("data", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {

                    // número do escopo
                    val numeroEscopoVal = document.get("numeroEscopo")
                    val numeroEscopo = numeroEscopoVal?.toString() ?: "Desconhecido"

                    // Ação (espera-se "Criado", mas pode ser outro valor)
                    val acao = document.getString("acao") ?: "Ação desconhecida"

                    // Nome do usuário
                    val usuario = document.getString("usuario") ?: "Usuário desconhecido"

                    // Data (somente data, sem hora)
                    val dataValue = document.get("data")
                    val dataString = when (dataValue) {
                        is Number -> {
                            // Se for Number, convertemos para data legível
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // Formato sem horas
                            sdf.format(Date(dataValue.toLong()))
                        }
                        is String -> {
                            // Se for String, usamos diretamente
                            dataValue
                        }
                        else -> {
                            // Caso seja nulo ou de outro tipo
                            "Data desconhecida"
                        }
                    }

                    // Construindo a string a ser exibida
                    val historicoInfo = "Escopo $numeroEscopo - $acao\nRealizado por $usuario em $dataString"
                    historicoList.add(historicoInfo)
                }

                // Configurar o adapter para exibir a lista de históricos
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, historicoList)
                listView.adapter = adapter
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Erro ao carregar histórico: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

        // Configuração do botão de voltar
        buttonVoltarMenu.setOnClickListener {
            finish()
        }
    }
}

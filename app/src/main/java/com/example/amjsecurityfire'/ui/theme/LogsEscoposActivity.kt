package com.amjsecurityfire.amjsecurityfire.ui.theme

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.amjsecurityfire.amjsecurityfire.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class LogsEscoposActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance() // Instância do Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.logs_escopos)

        val textViewLogs = findViewById<TextView>(R.id.textViewLogs)

        // Coletar logs do Firestore
        fetchLogs(textViewLogs)
    }

    private fun fetchLogs(textViewLogs: TextView) {
        // Referência à coleção de logs
        val logsRef = db.collection("logs_escopos")
            .orderBy("timestamp", Query.Direction.DESCENDING)  // Ordenar pelos mais recentes

        logsRef.get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    textViewLogs.text = "Nenhum log disponível."
                } else {
                    val logs = StringBuilder()  // Usamos StringBuilder para juntar os logs
                    var editedLogs = StringBuilder()  // Logs de edições
                    var otherLogs = StringBuilder()  // Outros logs, como movido para pendente e concluído

                    for (document in documents) {
                        val logText = document.getString("logText") ?: "Sem texto"
                        val timestamp = document.getString("timestamp") ?: "Sem data"
                        val user = document.getString("user") ?: "Desconhecido"
                        val logType = document.getString("logType") ?: "sem tipo"

                        // Dependendo do tipo de log, adicionar no lugar correto
                        when (logType) {
                            "editado" -> {
                                editedLogs.append("Editado por $user em $timestamp: $logText\n\n")
                            }
                            "movido_para_pendente" -> {
                                otherLogs.append("Escopo movido para pendente por $user em $timestamp\n\n")
                            }
                            "concluido" -> {
                                otherLogs.append("Escopo marcado como concluído por $user em $timestamp\n\n")
                            }
                            else -> {
                                otherLogs.append("Log desconhecido por $user em $timestamp: $logText\n\n")
                            }
                        }
                    }

                    // Concatenar os logs de edição e outros logs
                    logs.append("Logs de Edições:\n\n$editedLogs")
                    logs.append("Outros Logs:\n\n$otherLogs")

                    // Exibir todos os logs no TextView
                    textViewLogs.text = logs.toString()
                }
            }
            .addOnFailureListener { exception ->
                textViewLogs.text = "Erro ao carregar logs: ${exception.message}"
            }
    }
}

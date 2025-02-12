package com.amjsecurityfire.amjsecurityfire.ui.theme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class RegistrarHistoricoActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()  // Instância do Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Exemplo de chamada de registrar histórico
        val numeroEscopo = 123456L  // Exemplo de número de escopo
        val acao = "Alteração de status"  // Exemplo de ação
        val status = "Concluído"  // Exemplo de status
        val usuarioNome = "João Silva"  // Exemplo de nome do usuário
        val dataEvento = "12/02/2025"  // Exemplo de data
        val detalhesEdicao = "Escopo concluído com sucesso."  // Detalhes da edição (opcional)

        // Chama o método para registrar o histórico no Firestore
        registrarHistoricoEscopo(
            numeroEscopo, acao, status, usuarioNome, dataEvento, detalhesEdicao
        )
    }

    private fun registrarHistoricoEscopo(
        numeroEscopo: Long,
        acao: String,
        status: String,
        usuarioNome: String,
        dataEvento: String,
        detalhesEdicao: String?
    ) {
        // Criação do documento de histórico com dados fornecidos
        val historico = hashMapOf(
            "numeroEscopo" to numeroEscopo,
            "acao" to acao,
            "status" to status,
            "usuarioNome" to usuarioNome,
            "dataEvento" to dataEvento,
            "detalhesEdicao" to detalhesEdicao
        )

        // Salva o histórico na coleção "historicoEscopos" do Firestore
        db.collection("historicoEscopos")
            .add(historico)
            .addOnSuccessListener { documentReference ->
                // Sucesso ao salvar
                println("Histórico registrado com sucesso! ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                // Falha ao salvar
                println("Erro ao registrar histórico: $e")
            }
    }
}

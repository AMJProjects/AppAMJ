package com.example.amj_project.ui.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.appcheck.FirebaseAppCheck

class AdicionarEscopoActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var ultimoNumeroEscopo: Int = 0
    private var pdfUri: Uri? = null
    private val PDF_REQUEST_CODE = 100

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_escopo)

        db = FirebaseFirestore.getInstance()

        // Inicializar o Firebase App Check
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        // Recuperar dados enviados via Intent
        val editMode = intent.getBooleanExtra("editMode", false)
        val escopoId = intent.getStringExtra("escopoId")
        val empresaEdit = intent.getStringExtra("empresa")
        val numeroEscopoEdit = intent.getStringExtra("numeroEscopo")
        val dataEstimativaEdit = intent.getStringExtra("dataEstimativa")
        val tipoServicoEdit = intent.getStringExtra("tipoServico")
        val statusEdit = intent.getStringExtra("status")
        val resumoEscopoEdit = intent.getStringExtra("resumoEscopo")
        val numeroPedidoCompraEdit = intent.getStringExtra("numeroPedidoCompra")

        // Referenciar os campos do layout
        val empresaField = findViewById<EditText>(R.id.editTextText3)
        val dataEstimativaField = findViewById<EditText>(R.id.editTextDate)
        val tipoServicoSpinner = findViewById<Spinner>(R.id.spinnerTipoManutencao)
        val statusSpinner = findViewById<Spinner>(R.id.spinnerTipoManutencao2)
        val resumoField = findViewById<EditText>(R.id.textInputEditText)
        val numeroPedidoField = findViewById<EditText>(R.id.editTextNumber2)
        val salvarButton: Button = findViewById(R.id.button3)
        val cancelarButton: Button = findViewById(R.id.button5)

        val attachPdfButton = findViewById<Button>(R.id.buttonAttachPdf)
        val pdfStatusTextView = findViewById<TextView>(R.id.textViewPdfStatus)

        // Configurar botão de anexar PDF
        attachPdfButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/pdf"
            }
            startActivityForResult(intent, PDF_REQUEST_CODE)
        }

        // Dados para os Spinners
        val tiposManutencao = listOf("Preventiva", "Corretiva", "Preditiva")
        val statusManutencao = listOf("Pendente", "Em Andamento", "Concluído")

        tipoServicoSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tiposManutencao).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        statusSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusManutencao).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        if (editMode) {
            empresaField.setText(empresaEdit)
            dataEstimativaField.setText(dataEstimativaEdit)
            resumoField.setText(resumoEscopoEdit)
            numeroPedidoField.setText(numeroPedidoCompraEdit)

            tipoServicoSpinner.setSelection(getSpinnerIndex(tipoServicoSpinner, tipoServicoEdit))
            statusSpinner.setSelection(getSpinnerIndex(statusSpinner, statusEdit))
        } else {
            buscarUltimoNumeroEscopo(statusSpinner.selectedItem.toString())
        }

        statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val status = statusSpinner.selectedItem.toString()
                buscarUltimoNumeroEscopo(status)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        salvarButton.setOnClickListener {
            val empresa = empresaField.text.toString()
            val dataEstimativa = dataEstimativaField.text.toString()
            val tipoServico = tipoServicoSpinner.selectedItem.toString()
            val status = statusSpinner.selectedItem.toString()
            val resumo = resumoField.text.toString()
            val numeroPedidoCompra = numeroPedidoField.text.toString()

            if (empresa.isEmpty() || dataEstimativa.isEmpty() || resumo.isEmpty() || numeroPedidoCompra.isEmpty() || pdfUri == null) {
                Toast.makeText(this, "Por favor, preencha todos os campos e anexe um arquivo PDF.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("AdicionarEscopo", "Iniciando o upload do PDF...") // Adicionando log para depuração

            uploadPdfToStorage(
                onSuccess = { pdfDownloadUrl ->
                    Log.d("AdicionarEscopo", "PDF carregado com sucesso. URL: $pdfDownloadUrl") // Verifique o log aqui

                    val numeroEscopoAtual = if (editMode && numeroEscopoEdit != null) {
                        numeroEscopoEdit.toInt()
                    } else {
                        ultimoNumeroEscopo + 1
                    }

                    val novoEscopo = mapOf(
                        "numeroEscopo" to numeroEscopoAtual,
                        "empresa" to empresa,
                        "dataEstimativa" to dataEstimativa,
                        "tipoServico" to tipoServico,
                        "status" to status,
                        "resumoEscopo" to resumo,
                        "numeroPedidoCompra" to numeroPedidoCompra,
                        "pdfUrl" to pdfDownloadUrl
                    )

                    val collection = if (status == "Concluído") "escoposConcluidos" else "escoposPendentes"

                    Log.d("AdicionarEscopo", "Salvando escopo no Firestore...") // Verifique se chega aqui

                    if (editMode && escopoId != null) {
                        db.collection(collection).document(escopoId).update(novoEscopo).addOnSuccessListener {
                            Toast.makeText(this, "Escopo atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                            voltarParaLista(status)
                        }.addOnFailureListener { e ->
                            Toast.makeText(this, "Erro ao atualizar escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        db.collection(collection).add(novoEscopo).addOnSuccessListener {
                            Toast.makeText(this, "Escopo criado com sucesso!", Toast.LENGTH_SHORT).show()
                            voltarParaLista(status)
                        }.addOnFailureListener { e ->
                            Toast.makeText(this, "Erro ao salvar escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onFailure = { exception ->
                    Toast.makeText(this, "Erro ao fazer upload do PDF: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        cancelarButton.setOnClickListener {
            val intent = Intent(this, MenuPrincipalActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PDF_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            pdfUri = data?.data
            val fileName = pdfUri?.lastPathSegment ?: "PDF selecionado"
            findViewById<TextView>(R.id.textViewPdfStatus).apply {
                text = fileName
                setTextColor(getColor(R.color.teal_700))
            }
        }
    }

    private fun buscarUltimoNumeroEscopo(status: String) {
        val collection = if (status == "Concluído") "escoposConcluidos" else "escoposPendentes"

        db.collection(collection)
            .orderBy("numeroEscopo", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    ultimoNumeroEscopo = documents.first().get("numeroEscopo").toString().toInt()
                } else {
                    ultimoNumeroEscopo = 0
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao buscar número de escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                ultimoNumeroEscopo = 0
            }
    }

    private fun voltarParaLista(status: String) {
        val intent = if (status == "Concluído") {
            Intent(this, EscoposConcluidosActivity::class.java)
        } else {
            Intent(this, EscoposPendentesActivity::class.java)
        }
        startActivity(intent)
        finish() // Finaliza a Activity atual
    }


    private fun getSpinnerIndex(spinner: Spinner, value: String?): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == value) {
                return i
            }
        }
        return 0
    }

    private fun uploadPdfToStorage(onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        if (pdfUri == null) {
            Toast.makeText(this, "Nenhum PDF selecionado para upload.", Toast.LENGTH_SHORT).show()
            return
        }

        // Verificar o PDF URI
        Log.d("PDFUri", pdfUri.toString()) // Adicione esta linha para depurar o URI

        // Tente usar um caminho fixo para verificar se o problema está no caminho dinâmico
        val storageRef = FirebaseStorage.getInstance().reference
            .child("pdfs/${System.currentTimeMillis()}.pdf")

        storageRef.putFile(pdfUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onSuccess(uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

}
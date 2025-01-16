package com.example.amj_project.ui.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.amj_project.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.appcheck.FirebaseAppCheck
import android.net.ConnectivityManager

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

        // Recuperar dados do Intent
        val editMode = intent.getBooleanExtra("editMode", false)
        val escopoId = intent.getStringExtra("escopoId")
        val empresaEdit = intent.getStringExtra("empresa") ?: ""
        val numeroEscopoEdit = intent.getStringExtra("numeroEscopo")
        val dataEstimativaEdit = intent.getStringExtra("dataEstimativa") ?: ""
        val tipoServicoEdit = intent.getStringExtra("tipoServico") ?: ""
        val statusEdit = intent.getStringExtra("status") ?: ""
        val resumoEscopoEdit = intent.getStringExtra("resumoEscopo") ?: ""
        val numeroPedidoCompraEdit = intent.getStringExtra("numeroPedidoCompra") ?: ""

        // Referenciar os campos do layout
        val empresaField = findViewById<EditText>(R.id.editTextText3)
        val dataEstimativaField = findViewById<EditText>(R.id.editTextDate)
        val tipoServicoSpinner = findViewById<Spinner>(R.id.spinnerTipoManutencao)
        val statusSpinner = findViewById<Spinner>(R.id.spinnerTipoManutencao2)
        val resumoField = findViewById<EditText>(R.id.textInputEditText)
        val numeroPedidoField = findViewById<EditText>(R.id.editTextNumber2)
        val salvarButton = findViewById<Button>(R.id.button3)
        val cancelarButton = findViewById<Button>(R.id.button5)
        val attachPdfButton = findViewById<Button>(R.id.buttonAttachPdf)
        val pdfStatusTextView = findViewById<TextView>(R.id.textViewPdfStatus)

        // Dados para os Spinners
        val tiposManutencao = listOf("Preventiva", "Corretiva", "Preditiva")
        val statusManutencao = listOf("Pendente", "Em Andamento", "Concluído")

        // Configurar Spinners
        setupSpinner(tipoServicoSpinner, tiposManutencao, tipoServicoEdit)
        setupSpinner(statusSpinner, statusManutencao, statusEdit)

        // Preencher campos no modo de edição
        if (editMode) {
            empresaField.setText(empresaEdit)
            dataEstimativaField.setText(dataEstimativaEdit)
            resumoField.setText(resumoEscopoEdit)
            numeroPedidoField.setText(numeroPedidoCompraEdit)
        } else {
            buscarUltimoNumeroEscopo(statusSpinner.selectedItem.toString())
        }

        // Atualizar último número de escopo ao mudar o status
        statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                buscarUltimoNumeroEscopo(statusSpinner.selectedItem.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Botão de anexar PDF
        attachPdfButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "application/pdf"
            }
            startActivityForResult(intent, PDF_REQUEST_CODE)
        }

        // Botão de salvar
        salvarButton.setOnClickListener {
            val empresa = empresaField.text.toString().trim()
            val dataEstimativa = dataEstimativaField.text.toString().trim()
            val tipoServico = tipoServicoSpinner.selectedItem.toString()
            val status = statusSpinner.selectedItem.toString()
            val resumo = resumoField.text.toString().trim()
            val numeroPedidoCompra = numeroPedidoField.text.toString().trim()

            // Verificação de campos obrigatórios
            if (empresa.isEmpty() || dataEstimativa.isEmpty() || resumo.isEmpty() || numeroPedidoCompra.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verificação de conexão com a internet
            if (!isInternetAvailable()) {
                Toast.makeText(this, "Sem conexão com a internet.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Iniciar o upload do PDF
            uploadPdfToStorage(
                onSuccess = { pdfDownloadUrl ->
                    // Garantir que a variável pdfDownloadUrl nunca seja null
                    val finalPdfDownloadUrl = pdfDownloadUrl ?: ""

                    // Determinar o número do escopo
                    val numeroEscopoAtual = if (editMode && numeroEscopoEdit != null) {
                        numeroEscopoEdit?.toInt() ?: 0
                    } else {
                        ultimoNumeroEscopo + 1
                    }

                    // Criar o mapa para salvar o escopo
                    val novoEscopo = mapOf(
                        "numeroEscopo" to numeroEscopoAtual,
                        "empresa" to empresa,
                        "dataEstimativa" to dataEstimativa,
                        "tipoServico" to tipoServico,
                        "status" to status,
                        "resumoEscopo" to resumo,
                        "numeroPedidoCompra" to numeroPedidoCompra,
                        "pdfUrl" to finalPdfDownloadUrl
                    )

                    // Salvar no Firestore
                    salvarNoFirestore(status, novoEscopo, editMode, escopoId)
                },
                onFailure = { exception ->
                    Toast.makeText(this, "Erro ao fazer upload do PDF: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Função para verificar a conexão com a internet
        fun isInternetAvailable(): Boolean {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetworkInfo
            return activeNetwork?.isConnected == true
        }


        fun salvarNoFirestore(
            status: String,
            novoEscopo: Map<String, Any>, // Garantindo que o tipo é Map<String, Any>
            editMode: Boolean,
            escopoId: String?
        ) {
            // Definir a coleção de acordo com o status do escopo
            val collection = if (status == "Concluído") "escoposConcluidos" else "escoposPendentes"

            // Verificar se estamos editando um escopo existente
            if (editMode && !escopoId.isNullOrEmpty()) {
                // Atualizar o escopo existente no Firestore
                db.collection(collection).document(escopoId).update(novoEscopo)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Escopo atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                        voltarParaLista(status) // Voltar para a lista de escopos
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao atualizar escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Criar um novo escopo no Firestore
                db.collection(collection).add(novoEscopo)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Escopo criado com sucesso!", Toast.LENGTH_SHORT).show()
                        voltarParaLista(status) // Voltar para a lista de escopos
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Erro ao criar escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        // Botão de cancelar
        cancelarButton.setOnClickListener {
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

    private fun setupSpinner(spinner: Spinner, items: List<String>, defaultValue: String?) {
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        defaultValue?.let {
            spinner.setSelection(getSpinnerIndex(spinner, it))
        }
    }

    private fun getSpinnerIndex(spinner: Spinner, value: String?): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == value) {
                return i
            }
        }
        return 0
    }

    private fun buscarUltimoNumeroEscopo(status: String) {
        val collection = if (status == "Concluído") "escoposConcluidos" else "escoposPendentes"

        db.collection(collection)
            .orderBy("numeroEscopo", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                ultimoNumeroEscopo = if (!documents.isEmpty) {
                    documents.first().get("numeroEscopo").toString().toInt()
                } else {
                    0
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Erro ao buscar número de escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                ultimoNumeroEscopo = 0
            }
    }

    private fun uploadPdfToStorage(onSuccess: (String?) -> Unit, onFailure: (Exception) -> Unit) {
        if (pdfUri == null) {
            onSuccess(null)
            return
        }

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

    private fun salvarNoFirestore(
        status: String,
        novoEscopo: Map<String, Any>,
        editMode: Boolean,
        escopoId: String?
    ) {
        val collection = if (status == "Concluído") "escoposConcluidos" else "escoposPendentes"

        if (editMode && escopoId != null) {
            db.collection(collection).document(escopoId).update(novoEscopo)
                .addOnSuccessListener {
                    Toast.makeText(this, "Escopo atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    voltarParaLista(status)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao atualizar escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            db.collection(collection).add(novoEscopo)
                .addOnSuccessListener {
                    Toast.makeText(this, "Escopo criado com sucesso!", Toast.LENGTH_SHORT).show()
                    voltarParaLista(status)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao criar escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun voltarParaLista(status: String) {
        val intent = if (status == "Concluído") {
            Intent(this, EscoposConcluidosActivity::class.java)
        } else {
            Intent(this, EscoposPendentesActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo?.isConnected == true
    }
}

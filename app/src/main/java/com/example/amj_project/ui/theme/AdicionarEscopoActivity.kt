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
import android.net.NetworkCapabilities
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts

class AdicionarEscopoActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var progressBarContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private var ultimoNumeroEscopo: Int = 0
    private var pdfUri: Uri? = null
    private val PDF_REQUEST_CODE = 100

    private lateinit var salvarButton: Button
    private lateinit var cancelarButton: Button
    private lateinit var attachPdfButton: Button
    private lateinit var pdfStatusTextView: TextView

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

        // Inicializar ProgressBar e seu container
        progressBarContainer = findViewById(R.id.progressBarContainer)
        progressBar = findViewById(R.id.progressBar)

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

        salvarButton = findViewById(R.id.button3)
        cancelarButton = findViewById(R.id.button5)
        attachPdfButton = findViewById(R.id.buttonAttachPdf)
        pdfStatusTextView = findViewById(R.id.textViewPdfStatus)

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

        // Botão de anexar PDF com ActivityResultContracts
        val getPdfLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                pdfUri = uri
                val fileName = uri.lastPathSegment ?: "PDF selecionado"
                pdfStatusTextView.text = fileName
                pdfStatusTextView.setTextColor(getColor(R.color.teal_700))
            }
        }

        attachPdfButton.setOnClickListener {
            getPdfLauncher.launch("application/pdf")
        }

        // Botão de salvar
        salvarButton.setOnClickListener {
            // Mostrar ProgressBar ao iniciar a tarefa
            progressBarContainer.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE

            val empresa = empresaField.text.toString().trim()
            val dataEstimativa = dataEstimativaField.text.toString().trim()
            val tipoServico = tipoServicoSpinner.selectedItem.toString()
            val status = statusSpinner.selectedItem.toString()
            val resumo = resumoField.text.toString().trim()
            val numeroPedidoCompra = numeroPedidoField.text.toString().trim()

            // Verificar campos obrigatórios
            if (empresa.isEmpty() || dataEstimativa.isEmpty() || resumo.isEmpty() || numeroPedidoCompra.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show()

                // Esconder ProgressBar se houver erro
                progressBarContainer.visibility = View.GONE
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            // Verificar conexão com a internet
            if (!isInternetAvailable()) {
                Toast.makeText(this, "Sem conexão com a internet.", Toast.LENGTH_SHORT).show()

                // Esconder ProgressBar se não houver conexão
                progressBarContainer.visibility = View.GONE
                progressBar.visibility = View.GONE
                return@setOnClickListener
            }

            // Iniciar o upload do PDF
            uploadPdfToStorage(
                onSuccess = { pdfDownloadUrl ->
                    val finalPdfDownloadUrl = pdfDownloadUrl ?: ""

                    // Determinar o número do escopo
                    val numeroEscopoAtual = if (editMode && numeroEscopoEdit != null) {
                        numeroEscopoEdit.toInt()
                    } else {
                        ultimoNumeroEscopo + 1
                    }

                    // Criar o mapa de dados para o Firestore
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

                    // Esconder ProgressBar após a tarefa
                    progressBarContainer.visibility = View.GONE
                    progressBar.visibility = View.GONE
                },
                onFailure = { exception ->
                    Toast.makeText(this, "Erro ao fazer upload do PDF: ${exception.message}", Toast.LENGTH_SHORT).show()

                    // Esconder ProgressBar após erro
                    progressBarContainer.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }
            )
        }

        // Botão de cancelar
        cancelarButton.setOnClickListener {
            finish()
        }
    }

    private fun toggleProgress(isLoading: Boolean) {
        // Mostrar ou esconder o container da ProgressBar
        if (isLoading) {
            progressBarContainer.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
        } else {
            progressBarContainer.visibility = View.GONE
            progressBar.visibility = View.GONE
        }

        // Desabilitar botões enquanto está carregando
        salvarButton.isEnabled = !isLoading
        cancelarButton.isEnabled = !isLoading
        attachPdfButton.isEnabled = !isLoading
    }

    private fun salvarNoFirestore(status: String, novoEscopo: Map<String, Any>, editMode: Boolean, escopoId: String?) {
        val escopoCollection = if (status == "Concluído") {
            db.collection("escoposConcluidos")
        } else {
            db.collection("escoposPendentes")
        }

        if (editMode && escopoId != null) {
            // Atualizar o escopo existente
            escopoCollection.document(escopoId)
                .set(novoEscopo)
                .addOnSuccessListener {
                    Toast.makeText(this, "Escopo atualizado com sucesso.", Toast.LENGTH_SHORT).show()
                    finish() // Voltar à tela anterior
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao atualizar escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Adicionar um novo escopo
            escopoCollection.add(novoEscopo)
                .addOnSuccessListener {
                    Toast.makeText(this, "Escopo adicionado com sucesso.", Toast.LENGTH_SHORT).show()
                    finish() // Voltar à tela anterior
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Erro ao adicionar escopo: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>, defaultValue: String?) {
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        defaultValue?.let {
            val index = items.indexOf(it)
            if (index != -1) {
                spinner.setSelection(index)
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
        val pdfRef = storageRef.child("pdfs/${System.currentTimeMillis()}.pdf")

        pdfRef.putFile(pdfUri!!)
            .addOnSuccessListener {
                pdfRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("Storage", "PDF upload bem-sucedido: $uri")
                    onSuccess(uri.toString())
                }.addOnFailureListener { e ->
                    Log.e("Storage", "Erro ao obter URL do PDF: ${e.message}", e)
                    onFailure(e)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Storage", "Erro ao fazer upload do PDF: ${e.message}", e)
                onFailure(e)
            }
    }

    // Função de verificação de conexão com a internet
    private fun isInternetAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}

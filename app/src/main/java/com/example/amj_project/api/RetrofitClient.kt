package com.example.amj_project.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://20.206.204.44:3000" // Substitua pelo seu endereço de servidor

    // Configuração do Retrofit
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)  // URL base da sua API
        .addConverterFactory(GsonConverterFactory.create())  // Converte as respostas para objetos Gson
        .build()

    // Instância do ApiService que será utilizada nas requisições
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

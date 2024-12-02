package com.example.amj_project.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Interface para comunicação com a API
interface ApiService {

    @POST("/login")
    fun login(@Body loginRequest: ApiLoginRequest): Call<ApiResponse>

    @POST("/recuperar-senha")
    fun recuperarSenha(@Body recuperarSenhaRequest: ApiRecuperarSenhaRequest): Call<ApiResponse>
}

// Classes de requisição e resposta com nomes distintos para o ApiService
data class ApiLoginRequest(val email: String, val senha: String)
data class ApiRecuperarSenhaRequest(val email: String)

data class ApiResponse(val message: String, val senha: String? = null)

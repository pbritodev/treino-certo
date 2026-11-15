package com.treinocerto.app.data.model

data class Usuario(
    val id: Long? = null,
    val nome: String,
    val email: String,
    val idade: Int,
    val sexo: String,
    val criadoEm: String? = null,
    /** Enviada apenas no cadastro; a API nunca devolve a senha. */
    val senha: String? = null,
)

data class LoginRequest(val email: String, val senha: String)

data class TrocaSenhaRequest(val senhaAtual: String, val novaSenha: String)

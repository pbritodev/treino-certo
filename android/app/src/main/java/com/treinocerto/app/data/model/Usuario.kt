package com.treinocerto.app.data.model

data class Usuario(
    val id: Long? = null,
    val nome: String,
    val email: String,
    val idade: Int,
    val sexo: String,
    val criadoEm: String? = null,
)

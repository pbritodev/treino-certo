package com.treinocerto.app.data.repository

/** Resultado de uma chamada à API: sucesso com o dado ou erro com mensagem amigável. */
sealed class Resultado<out T> {
    data class Sucesso<T>(val dado: T) : Resultado<T>()
    /** [codigo] é o status HTTP devolvido pela API (null quando nem chegou ao servidor). */
    data class Erro(val mensagem: String, val codigo: Int? = null) : Resultado<Nothing>()
}

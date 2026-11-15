package com.treinocerto.app.data.repository

/** Resultado de uma chamada à API: sucesso com o dado ou erro com mensagem amigável. */
sealed class Resultado<out T> {
    data class Sucesso<T>(val dado: T) : Resultado<T>()
    data class Erro(val mensagem: String) : Resultado<Nothing>()
}

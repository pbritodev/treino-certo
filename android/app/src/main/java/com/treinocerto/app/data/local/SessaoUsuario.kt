package com.treinocerto.app.data.local

import android.content.Context
import androidx.core.content.edit

/** Guarda localmente (SharedPreferences) qual usuário está usando o app. */
class SessaoUsuario(context: Context) {

    private val prefs = context.getSharedPreferences("sessao_treino_certo", Context.MODE_PRIVATE)

    val usuarioId: Long?
        get() = prefs.getLong(CHAVE_ID, -1L).takeIf { it > 0 }

    val nome: String?
        get() = prefs.getString(CHAVE_NOME, null)

    val estaLogado: Boolean
        get() = usuarioId != null

    fun salvar(id: Long, nome: String) = prefs.edit {
        putLong(CHAVE_ID, id)
        putString(CHAVE_NOME, nome)
    }

    fun limpar() = prefs.edit { clear() }

    private companion object {
        const val CHAVE_ID = "usuario_id"
        const val CHAVE_NOME = "usuario_nome"
    }
}

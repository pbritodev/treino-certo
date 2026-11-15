package com.treinocerto.app.data.local

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

/** Salva e aplica o tema escolhido (claro, escuro ou seguir o sistema). */
class PreferenciasTema(private val context: Context) {

    enum class Tema(val modo: Int) {
        SISTEMA(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
        CLARO(AppCompatDelegate.MODE_NIGHT_NO),
        ESCURO(AppCompatDelegate.MODE_NIGHT_YES),
    }

    private val preferencias = context.getSharedPreferences("preferencias_tema", Context.MODE_PRIVATE)

    var tema: Tema
        get() = Tema.valueOf(preferencias.getString(CHAVE, Tema.SISTEMA.name) ?: Tema.SISTEMA.name)
        set(valor) {
            preferencias.edit { putString(CHAVE, valor.name) }
            aplicar()
        }

    fun aplicar() = AppCompatDelegate.setDefaultNightMode(tema.modo)

    /** Verdadeiro se o app está sendo exibido no modo escuro agora. */
    fun estaEscuro(): Boolean {
        val modo = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return modo == Configuration.UI_MODE_NIGHT_YES
    }

    /** Alterna rapidamente entre claro e escuro (botão da Home). */
    fun alternar() {
        tema = if (estaEscuro()) Tema.CLARO else Tema.ESCURO
    }

    private companion object {
        const val CHAVE = "tema"
    }
}

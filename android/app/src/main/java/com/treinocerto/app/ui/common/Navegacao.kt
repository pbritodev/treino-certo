package com.treinocerto.app.ui.common

import android.app.Activity
import android.content.Intent
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.ui.home.MainActivity
import com.treinocerto.app.ui.login.LoginActivity

/** Iniciais do nome para o avatar: "Maria Silva" -> "MS". */
fun iniciais(nome: String): String =
    nome.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        .let { partes -> listOfNotNull(partes.firstOrNull(), partes.drop(1).lastOrNull()) }
        .joinToString("") { it.first().uppercase() }

/** Abre uma tela limpando a pilha (o botão "voltar" não retorna para as telas anteriores). */
private fun Activity.abrirComoInicio(destino: Class<*>) {
    startActivity(Intent(this, destino).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    finish()
}

fun Activity.irParaHome() = abrirComoInicio(MainActivity::class.java)

/** Encerra a sessão e volta para o login. */
fun Activity.sair() {
    SessaoUsuario(this).limpar()
    abrirComoInicio(LoginActivity::class.java)
}

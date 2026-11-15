package com.treinocerto.app.util

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

fun Context.toast(mensagem: String) {
    Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
}

fun View.visivel(mostrar: Boolean) {
    visibility = if (mostrar) View.VISIBLE else View.GONE
}

/** Mostra um erro em Snackbar com ação opcional de "tentar novamente". */
fun Activity.mostrarErro(mensagem: String, aoTentarNovamente: (() -> Unit)? = null) {
    val raiz = findViewById<View>(android.R.id.content)
    val snackbar = Snackbar.make(raiz, mensagem, Snackbar.LENGTH_LONG)
    snackbar.setTextMaxLines(5)
    if (aoTentarNovamente != null) {
        snackbar.setAction(com.treinocerto.app.R.string.tentar_novamente) { aoTentarNovamente() }
    }
    snackbar.show()
}

package com.treinocerto.app.ui.common

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.treinocerto.app.R

/** Diálogo padrão de confirmação de exclusão. */
fun Context.confirmarExclusao(
    titulo: String,
    mensagem: String,
    aoCancelar: () -> Unit = {},
    aoConfirmar: () -> Unit,
) {
    MaterialAlertDialogBuilder(this)
        .setTitle(titulo)
        .setMessage(mensagem)
        .setIcon(R.drawable.ic_lixeira)
        .setNegativeButton(R.string.cancelar) { _, _ -> aoCancelar() }
        .setPositiveButton(R.string.excluir) { _, _ -> aoConfirmar() }
        .setOnCancelListener { aoCancelar() }
        .show()
}

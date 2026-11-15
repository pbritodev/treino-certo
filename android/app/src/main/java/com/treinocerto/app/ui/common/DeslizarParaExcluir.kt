package com.treinocerto.app.ui.common

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/** Habilita "deslizar para o lado" nos itens da lista, avisando a posição deslizada. */
fun RecyclerView.habilitarDeslizarParaExcluir(aoDeslizar: (posicao: Int) -> Unit) {
    val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, alvo: RecyclerView.ViewHolder) = false
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direcao: Int) =
            aoDeslizar(viewHolder.bindingAdapterPosition)
    }
    ItemTouchHelper(callback).attachToRecyclerView(this)
}

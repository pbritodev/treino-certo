package com.treinocerto.app.ui.progresso

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.treinocerto.app.R
import com.treinocerto.app.data.model.TreinoRealizado
import com.treinocerto.app.databinding.ItemTreinoRealizadoBinding
import com.treinocerto.app.util.Formatos

class TreinoRealizadoAdapter : ListAdapter<TreinoRealizado, TreinoRealizadoAdapter.ViewHolder>(Diferenca) {

    class ViewHolder(val binding: ItemTreinoRealizadoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemTreinoRealizadoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val treino = getItem(position)
        val contexto = holder.itemView.context
        holder.binding.txtTreino.text = contexto.getString(R.string.progresso_treino_item, treino.dia, treino.foco)
        holder.binding.txtDetalhe.text =
            contexto.getString(R.string.progresso_treino_detalhe, Formatos.dataLonga(treino.data), treino.duracaoMin)
    }

    private object Diferenca : DiffUtil.ItemCallback<TreinoRealizado>() {
        override fun areItemsTheSame(a: TreinoRealizado, b: TreinoRealizado) = a.id == b.id
        override fun areContentsTheSame(a: TreinoRealizado, b: TreinoRealizado) = a == b
    }
}

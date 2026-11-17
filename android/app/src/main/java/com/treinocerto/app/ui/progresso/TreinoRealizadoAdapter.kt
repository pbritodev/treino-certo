package com.treinocerto.app.ui.progresso

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.treinocerto.app.R
import com.treinocerto.app.data.model.TreinoRealizado
import com.treinocerto.app.databinding.ItemTreinoRealizadoBinding
import com.treinocerto.app.ui.treinos.ExibicaoTreino
import com.treinocerto.app.util.visivel

/** Lista de treinos feitos (usada em "Meu progresso" e em "Meus treinos"). */
class TreinoRealizadoAdapter(
    private val aoClicar: ((TreinoRealizado) -> Unit)? = null,
) : ListAdapter<TreinoRealizado, TreinoRealizadoAdapter.ViewHolder>(Diferenca) {

    class ViewHolder(val binding: ItemTreinoRealizadoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemTreinoRealizadoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val treino = getItem(position)
        val contexto = holder.itemView.context
        with(holder.binding) {
            imgTipo.setImageResource(ExibicaoTreino.icone(treino.tipo))
            txtTreino.text = ExibicaoTreino.titulo(contexto, treino)
            txtDetalhe.text = ExibicaoTreino.detalhe(contexto, treino)
            txtKcal.visivel(treino.kcal != null)
            treino.kcal?.let { txtKcal.text = contexto.getString(R.string.treino_kcal, it) }
            root.isClickable = aoClicar != null
            root.setOnClickListener { aoClicar?.invoke(treino) }
        }
    }

    private object Diferenca : DiffUtil.ItemCallback<TreinoRealizado>() {
        override fun areItemsTheSame(a: TreinoRealizado, b: TreinoRealizado) = a.id == b.id
        override fun areContentsTheSame(a: TreinoRealizado, b: TreinoRealizado) = a == b
    }
}

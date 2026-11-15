package com.treinocerto.app.ui.historico

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.treinocerto.app.R
import com.treinocerto.app.data.model.Avaliacao
import com.treinocerto.app.databinding.ItemAvaliacaoBinding
import com.treinocerto.app.ui.common.ExibidorPlano
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.visivel

/** Adapter do histórico. ListAdapter + DiffUtil anima inserções/remoções automaticamente. */
class AvaliacaoAdapter(
    private val aoClicar: (Avaliacao) -> Unit,
) : ListAdapter<Avaliacao, AvaliacaoAdapter.ViewHolder>(Diferenca) {

    class ViewHolder(val binding: ItemAvaliacaoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemAvaliacaoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val avaliacao = getItem(position)
        val contexto = holder.itemView.context
        with(holder.binding) {
            imgObjetivo.setImageResource(ExibidorPlano.iconeDoObjetivo(avaliacao.objetivo))
            txtPerfil.text = avaliacao.perfil
            txtData.text = Formatos.dataHora(avaliacao.criadoEm)
            txtIntensidade.text = avaliacao.classificacao
            txtIntensidade.setTextColor(ContextCompat.getColor(contexto, ExibidorPlano.corDaIntensidade(avaliacao.classificacao)))
            txtKcal.text = contexto.getString(R.string.historico_meta_kcal, avaliacao.metaCalorica)
            imgFavorita.visivel(avaliacao.favorita)
            root.setOnClickListener { aoClicar(avaliacao) }
        }
    }

    private object Diferenca : DiffUtil.ItemCallback<Avaliacao>() {
        override fun areItemsTheSame(antigo: Avaliacao, novo: Avaliacao) = antigo.id == novo.id
        override fun areContentsTheSame(antigo: Avaliacao, novo: Avaliacao) = antigo == novo
    }
}

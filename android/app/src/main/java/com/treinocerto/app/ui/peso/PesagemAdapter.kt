package com.treinocerto.app.ui.peso

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.treinocerto.app.R
import com.treinocerto.app.data.model.Pesagem
import com.treinocerto.app.databinding.ItemPesagemBinding
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.visivel

/** Item da lista de pesagens; mostra a diferença em relação à pesagem anterior. */
data class PesagemComVariacao(val pesagem: Pesagem, val variacao: Double?)

class PesagemAdapter : ListAdapter<PesagemComVariacao, PesagemAdapter.ViewHolder>(Diferenca) {

    class ViewHolder(val binding: ItemPesagemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemPesagemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (pesagem, variacao) = getItem(position)
        val contexto = holder.itemView.context
        with(holder.binding) {
            txtPeso.text = contexto.getString(R.string.peso_kg, Formatos.decimal(pesagem.peso))
            txtData.text = Formatos.dataCurta(pesagem.data)
            txtObservacao.text = pesagem.observacao
            txtObservacao.visivel(pesagem.observacao.isNotBlank())

            txtVariacao.visivel(variacao != null && variacao != 0.0)
            if (variacao != null) {
                val sinal = if (variacao > 0) "+" else "−"
                txtVariacao.text = "$sinal${Formatos.decimal(kotlin.math.abs(variacao))} kg"
                val cor = if (variacao > 0) R.color.laranja else R.color.verde_primario
                txtVariacao.setTextColor(ContextCompat.getColor(contexto, cor))
            }
        }
    }

    private object Diferenca : DiffUtil.ItemCallback<PesagemComVariacao>() {
        override fun areItemsTheSame(a: PesagemComVariacao, b: PesagemComVariacao) = a.pesagem.id == b.pesagem.id
        override fun areContentsTheSame(a: PesagemComVariacao, b: PesagemComVariacao) = a == b
    }
}

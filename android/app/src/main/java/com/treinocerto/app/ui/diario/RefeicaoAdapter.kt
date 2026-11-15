package com.treinocerto.app.ui.diario

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.treinocerto.app.R
import com.treinocerto.app.data.model.Refeicao
import com.treinocerto.app.data.model.RefeicaoDiario
import com.treinocerto.app.data.model.RegistroAlimentar
import com.treinocerto.app.data.model.rotuloDe
import com.treinocerto.app.databinding.ItemRefeicaoBinding
import com.treinocerto.app.databinding.ItemRegistroBinding
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.visivel

/** Um card por refeição (café, almoço, lanche, jantar) com os alimentos registrados. */
class RefeicaoAdapter(
    private val aoAdicionar: (refeicao: String) -> Unit,
    private val aoClicarItem: (RegistroAlimentar) -> Unit,
) : ListAdapter<RefeicaoDiario, RefeicaoAdapter.ViewHolder>(Diferenca) {

    class ViewHolder(val binding: ItemRefeicaoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemRefeicaoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val refeicao = getItem(position)
        val contexto = holder.itemView.context
        with(holder.binding) {
            txtRefeicao.text = rotuloDe<Refeicao>(refeicao.refeicao)
            txtKcalRefeicao.text = contexto.getString(R.string.diario_kcal, Formatos.decimal(refeicao.kcal))
            txtVazio.visivel(refeicao.itens.isEmpty())
            btnAdicionar.setOnClickListener { aoAdicionar(refeicao.refeicao) }

            containerItens.removeAllViews()
            refeicao.itens.forEach { registro ->
                val linha = ItemRegistroBinding.inflate(LayoutInflater.from(contexto), containerItens, false)
                linha.txtAlimento.text = registro.alimento
                linha.txtDetalhe.text = contexto.getString(
                    R.string.diario_item_detalhe, Formatos.decimal(registro.quantidade), Formatos.decimal(registro.proteina),
                )
                linha.txtKcal.text = contexto.getString(R.string.diario_kcal, Formatos.decimal(registro.kcal))
                linha.root.setOnClickListener { aoClicarItem(registro) }
                containerItens.addView(linha.root)
            }
        }
    }

    private object Diferenca : DiffUtil.ItemCallback<RefeicaoDiario>() {
        override fun areItemsTheSame(antiga: RefeicaoDiario, nova: RefeicaoDiario) = antiga.refeicao == nova.refeicao
        override fun areContentsTheSame(antiga: RefeicaoDiario, nova: RefeicaoDiario) = antiga == nova
    }
}

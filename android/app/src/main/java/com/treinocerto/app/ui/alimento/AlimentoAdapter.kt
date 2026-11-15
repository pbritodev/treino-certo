package com.treinocerto.app.ui.alimento

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.treinocerto.app.R
import com.treinocerto.app.data.model.Alimento
import com.treinocerto.app.databinding.ItemAlimentoBinding
import com.treinocerto.app.util.Formatos
import kotlin.math.roundToInt

class AlimentoAdapter(
    private val aoClicar: (Alimento) -> Unit,
) : ListAdapter<Alimento, AlimentoAdapter.ViewHolder>(Diferenca) {

    class ViewHolder(val binding: ItemAlimentoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemAlimentoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alimento = getItem(position)
        with(holder.binding) {
            txtNome.text = alimento.nome
            txtPorcao.text = root.context.getString(
                R.string.alimento_porcao,
                alimento.porcaoDescricao,
                Formatos.decimal(alimento.porcaoG),
                alimento.kcalPara(alimento.porcaoG).roundToInt(),
            )
            root.setOnClickListener { aoClicar(alimento) }
        }
    }

    private object Diferenca : DiffUtil.ItemCallback<Alimento>() {
        override fun areItemsTheSame(antigo: Alimento, novo: Alimento) = antigo.id == novo.id
        override fun areContentsTheSame(antigo: Alimento, novo: Alimento) = antigo == novo
    }
}

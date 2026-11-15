package com.treinocerto.app.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.treinocerto.app.R
import com.treinocerto.app.data.model.DiaTreino
import com.treinocerto.app.data.model.Exercicio
import com.treinocerto.app.databinding.ItemDiaTreinoBinding
import com.treinocerto.app.databinding.ItemExercicioBinding

/** Lista os dias do plano; cada dia mostra seus exercícios (clicáveis). */
class PlanoAdapter(
    private val dias: List<DiaTreino>,
    private val aoClicarExercicio: (Exercicio) -> Unit,
) : RecyclerView.Adapter<PlanoAdapter.DiaViewHolder>() {

    class DiaViewHolder(val binding: ItemDiaTreinoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DiaViewHolder(ItemDiaTreinoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = dias.size

    override fun onBindViewHolder(holder: DiaViewHolder, position: Int) {
        val dia = dias[position]
        val contexto = holder.itemView.context
        holder.binding.txtDia.text = dia.dia
        holder.binding.txtFoco.text = dia.foco

        val container = holder.binding.containerExercicios
        container.removeAllViews()
        dia.exercicios.forEach { exercicio ->
            val item = ItemExercicioBinding.inflate(LayoutInflater.from(contexto), container, false)
            item.txtNomeExercicio.text = exercicio.nome
            item.txtPrescricao.text =
                contexto.getString(R.string.exercicio_prescricao, exercicio.series, exercicio.repeticoes)
            item.root.setOnClickListener { aoClicarExercicio(exercicio) }
            container.addView(item.root)
        }
    }
}

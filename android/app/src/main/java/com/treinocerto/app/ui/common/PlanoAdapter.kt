package com.treinocerto.app.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.treinocerto.app.R
import com.treinocerto.app.data.model.DiaTreino
import com.treinocerto.app.data.model.Exercicio
import com.treinocerto.app.databinding.ItemDiaTreinoBinding
import com.treinocerto.app.databinding.ItemExercicioBinding
import com.treinocerto.app.util.visivel

/** Lista os dias do plano; cada dia mostra seus exercícios (clicáveis). */
class PlanoAdapter(
    private val dias: List<DiaTreino>,
    private val aoClicarExercicio: (Exercicio) -> Unit,
    /** Quando informado, mostra o botão "Concluí este treino" em cada dia. */
    private val aoConcluirDia: ((DiaTreino) -> Unit)? = null,
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
        holder.binding.btnConcluir.visivel(aoConcluirDia != null)
        holder.binding.btnConcluir.setOnClickListener { aoConcluirDia?.invoke(dia) }

        val container = holder.binding.containerExercicios
        container.removeAllViews()
        dia.exercicios.forEach { exercicio ->
            val linha = ItemExercicioBinding.inflate(LayoutInflater.from(contexto), container, false)
            linha.txtNomeExercicio.text = exercicio.nome
            linha.txtPrescricao.text =
                contexto.getString(R.string.exercicio_prescricao, exercicio.series, exercicio.repeticoes)
            linha.root.setOnClickListener { aoClicarExercicio(exercicio) }
            container.addView(linha.root)
        }
    }
}

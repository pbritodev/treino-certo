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

    /** Dias do plano já concluídos hoje (ex.: "Dia 1"): mostram um ✓ em vez do botão. */
    private val concluidosHoje = mutableSetOf<String>()

    fun definirConcluidos(dias: Collection<String>) {
        concluidosHoje.clear()
        concluidosHoje.addAll(dias)
        notifyItemRangeChanged(0, itemCount)
    }

    fun marcarConcluido(dia: String) {
        concluidosHoje.add(dia)
        notifyItemChanged(dias.indexOfFirst { it.dia == dia })
    }

    fun estaConcluido(dia: String) = dia in concluidosHoje

    class DiaViewHolder(val binding: ItemDiaTreinoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        DiaViewHolder(ItemDiaTreinoBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = dias.size

    override fun onBindViewHolder(holder: DiaViewHolder, position: Int) {
        val dia = dias[position]
        val contexto = holder.itemView.context
        holder.binding.txtDia.text = dia.dia
        holder.binding.txtFoco.text = dia.foco
        val concluido = estaConcluido(dia.dia)
        with(holder.binding.btnConcluir) {
            visivel(aoConcluirDia != null)
            isEnabled = !concluido // evita registrar o mesmo treino duas vezes sem querer
            setText(if (concluido) R.string.treino_concluido_hoje else R.string.treino_concluir)
            setOnClickListener { aoConcluirDia?.invoke(dia) }
        }

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

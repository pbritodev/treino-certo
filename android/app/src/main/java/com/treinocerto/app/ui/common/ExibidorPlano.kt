package com.treinocerto.app.ui.common

import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.treinocerto.app.R
import com.treinocerto.app.data.model.Avaliacao
import com.treinocerto.app.data.model.DiaTreino
import com.treinocerto.app.data.model.Local
import com.treinocerto.app.data.model.rotuloDe
import com.treinocerto.app.databinding.LayoutPlanoCompletoBinding
import com.treinocerto.app.util.IntentsExternas

/**
 * Preenche o bloco "layout_plano_completo" (resumo, plano semanal e dicas).
 * Compartilhado pelas telas de Resultado e Detalhe para evitar código duplicado.
 */
object ExibidorPlano {

    fun exibir(
        binding: LayoutPlanoCompletoBinding,
        avaliacao: Avaliacao,
        aoConcluirDia: ((DiaTreino) -> Unit)? = null,
    ): PlanoAdapter {
        val contexto = binding.root.context

        binding.imgObjetivo.setImageResource(iconeDoObjetivo(avaliacao.objetivo))
        binding.txtPerfil.text = avaliacao.perfil
        binding.txtResumo.text = contexto.getString(
            R.string.resultado_resumo,
            avaliacao.diasSemana,
            avaliacao.minutosSessao,
            rotuloDe<Local>(avaliacao.local),
        )

        binding.txtIntensidade.text = contexto.getString(
            R.string.resultado_intensidade, avaliacao.classificacao, avaliacao.intensidade,
        )
        binding.barraIntensidade.progress = avaliacao.intensidade
        binding.barraIntensidade.progressTintList =
            ColorStateList.valueOf(ContextCompat.getColor(contexto, corDaIntensidade(avaliacao.classificacao)))

        binding.txtImc.text = contexto.getString(R.string.resultado_imc, avaliacao.imc, avaliacao.classificacaoImc)
        binding.txtMetaCalorica.text = contexto.getString(
            R.string.resultado_meta_calorica, avaliacao.metaCalorica, avaliacao.metaProteina,
        )

        binding.rvPlano.layoutManager = LinearLayoutManager(contexto)
        val adapter = PlanoAdapter(
            dias = avaliacao.plano,
            aoClicarExercicio = { exercicio -> IntentsExternas.buscarVideo(contexto, exercicio.nome) },
            aoConcluirDia = aoConcluirDia,
        )
        binding.rvPlano.adapter = adapter

        binding.txtDicas.text = avaliacao.dicas.joinToString("\n") { "• $it" }
        return adapter
    }

    /** Texto usado no compartilhamento (Intent implícita ACTION_SEND). */
    fun textoParaCompartilhar(avaliacao: Avaliacao): String = buildString {
        appendLine("🏋️ Meu plano TreinoCerto: ${avaliacao.perfil}")
        appendLine("Intensidade ${avaliacao.classificacao} · ${avaliacao.diasSemana}x por semana")
        appendLine("Meta diária: ${avaliacao.metaCalorica} kcal")
        appendLine()
        avaliacao.plano.forEach { dia ->
            appendLine("${dia.dia} – ${dia.foco}")
            dia.exercicios.forEach { appendLine("  • ${it.nome}: ${it.series}x ${it.repeticoes}") }
        }
    }

    fun iconeDoObjetivo(objetivo: String) = when (objetivo) {
        "EMAGRECIMENTO" -> R.drawable.ic_fogo
        "HIPERTROFIA" -> R.drawable.ic_haltere
        "CONDICIONAMENTO" -> R.drawable.ic_corrida
        else -> R.drawable.ic_coracao
    }

    fun corDaIntensidade(classificacao: String) = when (classificacao) {
        "Leve" -> R.color.intensidade_leve
        "Moderada" -> R.color.intensidade_moderada
        else -> R.color.intensidade_intensa
    }
}

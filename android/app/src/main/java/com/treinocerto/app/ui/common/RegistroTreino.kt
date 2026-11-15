package com.treinocerto.app.ui.common

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.treinocerto.app.R
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.data.model.Avaliacao
import com.treinocerto.app.data.model.DiaTreino
import com.treinocerto.app.data.model.TreinoRealizadoEntrada
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.LayoutPlanoCompletoBinding
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.toast
import kotlinx.coroutines.launch

private const val HTTP_CONFLITO = 409

/**
 * Registra na API (POST /api/treinos-realizados) que o usuário concluiu hoje
 * o treino de um dia do plano. Se ele já tinha sido registrado hoje (409),
 * avisa e chama [aoJaRegistrado] em vez de duplicar.
 */
fun AppCompatActivity.registrarTreinoConcluido(
    avaliacaoId: Long?,
    dia: DiaTreino,
    duracaoMin: Int,
    aoJaRegistrado: () -> Unit = {},
    aoConcluir: () -> Unit = {},
) {
    val usuarioId = SessaoUsuario(this).usuarioId ?: return
    val entrada = TreinoRealizadoEntrada(usuarioId, avaliacaoId, dia.dia, dia.foco, duracaoMin, Formatos.hojeApi())
    lifecycleScope.launch {
        when (val resultado = TreinoRepository().registrarTreino(entrada)) {
            is Resultado.Sucesso -> {
                toast(getString(R.string.treino_registrado))
                aoConcluir()
            }
            is Resultado.Erro -> if (resultado.codigo == HTTP_CONFLITO) {
                toast(getString(R.string.treino_ja_feito))
                aoJaRegistrado()
            } else {
                mostrarErro(resultado.mensagem)
            }
        }
    }
}

/**
 * Exibe o plano com o botão "Concluí este treino" em cada dia e marca com ✓
 * os dias que o usuário já concluiu hoje (consulta GET .../treinos-realizados?data=hoje).
 * Usado pelas telas de Resultado e Detalhe.
 */
fun AppCompatActivity.exibirPlanoComTreinosDeHoje(binding: LayoutPlanoCompletoBinding, avaliacao: Avaliacao) {
    lateinit var adapter: PlanoAdapter
    adapter = ExibidorPlano.exibir(binding, avaliacao) { dia ->
        registrarTreinoConcluido(
            avaliacaoId = avaliacao.id,
            dia = dia,
            duracaoMin = avaliacao.minutosSessao,
            aoJaRegistrado = { adapter.marcarConcluido(dia.dia) },
            aoConcluir = { adapter.marcarConcluido(dia.dia) },
        )
    }

    val usuarioId = SessaoUsuario(this).usuarioId ?: return
    lifecycleScope.launch {
        val resultado = TreinoRepository().listarTreinosRealizados(usuarioId, Formatos.hojeApi())
        if (resultado is Resultado.Sucesso) {
            adapter.definirConcluidos(resultado.dado.filter { it.avaliacaoId == avaliacao.id }.map { it.dia })
        }
    }
}

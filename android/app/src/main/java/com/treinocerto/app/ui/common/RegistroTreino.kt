package com.treinocerto.app.ui.common

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.treinocerto.app.R
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.data.model.DiaTreino
import com.treinocerto.app.data.model.TreinoRealizadoEntrada
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.toast
import kotlinx.coroutines.launch

/**
 * Registra na API (POST /api/treinos-realizados) que o usuário concluiu hoje
 * o treino de um dia do plano. Usado pelas telas de Resultado, Detalhe e Progresso.
 */
fun AppCompatActivity.registrarTreinoConcluido(
    avaliacaoId: Long?,
    dia: DiaTreino,
    duracaoMin: Int,
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
            is Resultado.Erro -> mostrarErro(resultado.mensagem)
        }
    }
}

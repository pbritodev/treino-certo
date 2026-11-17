package com.treinocerto.app.ui.treinos

import android.content.Context
import com.treinocerto.app.R
import com.treinocerto.app.data.model.TipoTreino
import com.treinocerto.app.data.model.TreinoRealizado
import com.treinocerto.app.data.model.rotuloDe
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.IntentsExternas
import java.util.Calendar

/** Como cada treino aparece nas listas e o que é enviado para a agenda do celular. */
object ExibicaoTreino {

    fun icone(tipo: String) = when (tipo) {
        TipoTreino.CORRIDA.api, TipoTreino.CAMINHADA.api -> R.drawable.ic_corrida
        TipoTreino.YOGA.api -> R.drawable.ic_coracao
        TipoTreino.FUNCIONAL.api, TipoTreino.ESPORTE.api -> R.drawable.ic_fogo
        TipoTreino.PLANO.api -> R.drawable.ic_check
        else -> R.drawable.ic_haltere
    }

    /** "Dia 2 · Pernas e core" (plano) ou "Corrida no parque · 5 km" (avulso). */
    fun titulo(contexto: Context, treino: TreinoRealizado): String =
        if (treino.foco.isBlank()) treino.dia
        else contexto.getString(R.string.progresso_treino_item, treino.dia, treino.foco)

    /** "Terça-feira, 17 de novembro · 35 min" (+ o tipo, nos treinos avulsos). */
    fun detalhe(contexto: Context, treino: TreinoRealizado): String {
        val texto = contexto.getString(R.string.treino_item_detalhe, Formatos.dataLonga(treino.data), treino.duracaoMin)
        return if (treino.doPlano) texto else "${rotuloDe<TipoTreino>(treino.tipo)} · $texto"
    }

    /** Intent implícita: abre a agenda do celular com o treino preenchido (no dia do treino). */
    fun adicionarNaAgenda(contexto: Context, treino: TreinoRealizado) {
        val inicio = Formatos.calendarioDe(treino.data).apply {
            val agora = Calendar.getInstance()
            set(Calendar.HOUR_OF_DAY, agora.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, agora.get(Calendar.MINUTE))
        }
        IntentsExternas.adicionarNaAgenda(
            contexto,
            titulo = "🏋️ ${treino.dia}",
            descricao = listOf(treino.foco, treino.observacao).filter { it.isNotBlank() }.joinToString("\n"),
            inicioMillis = inicio.timeInMillis,
            duracaoMin = treino.duracaoMin,
        )
    }
}

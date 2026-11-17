package com.treinocerto.app.data.model

/**
 * Treino enviado para a API: um dia do plano (tipo "PLANO") ou um treino avulso
 * cadastrado na tela "Meus treinos" (CORRIDA, NATACAO...). Em treinos avulsos,
 * [dia] guarda o título do treino.
 */
data class TreinoRealizadoEntrada(
    val usuarioId: Long,
    val avaliacaoId: Long?,
    val dia: String,
    val foco: String,
    val duracaoMin: Int,
    val data: String,
    val tipo: String = TipoTreino.PLANO.api,
    val intensidade: String? = null,
    val observacao: String = "",
)

data class TreinoRealizado(
    val id: Long,
    val avaliacaoId: Long?,
    val dia: String,
    val foco: String,
    val duracaoMin: Int,
    val data: String,
    val tipo: String = TipoTreino.PLANO.api,
    val intensidade: String? = null,
    /** Calorias estimadas pela API (MET x peso x horas). */
    val kcal: Int? = null,
    val observacao: String = "",
) {
    val doPlano get() = tipo == TipoTreino.PLANO.api
}

data class PlanoResumo(
    val id: Long,
    val perfil: String,
    val objetivo: String,
    val classificacao: String,
    val diasSemana: Int,
    val metaCalorica: Int,
    val imc: Double,
    val pesoMeta: Double?,
)

data class DiaDaSemana(val data: String, val treinos: Int)

/** Resumo devolvido por GET /api/usuarios/{id}/progresso. */
data class Progresso(
    val usuario: Usuario,
    val planoAtual: PlanoResumo?,
    val pesoInicial: Double?,
    val pesoAtual: Double?,
    val totalTreinos: Int,
    val minutosTotais: Int,
    val treinosSemana: Int,
    val metaSemanal: Int,
    val sequenciaDias: Int,
    val ultimos7Dias: List<DiaDaSemana>,
    val treinosRecentes: List<TreinoRealizado>,
)

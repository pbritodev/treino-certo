package com.treinocerto.app.data.model

data class TreinoRealizadoRequest(
    val usuarioId: Long,
    val avaliacaoId: Long?,
    val dia: String,
    val foco: String,
    val duracaoMin: Int,
    val data: String,
)

data class TreinoRealizado(
    val id: Long,
    val avaliacaoId: Long?,
    val dia: String,
    val foco: String,
    val duracaoMin: Int,
    val data: String,
)

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

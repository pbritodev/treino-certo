package com.treinocerto.app.data.model

/** Respostas do questionário enviadas para POST /api/avaliacoes. */
data class AvaliacaoRequest(
    val usuarioId: Long,
    val objetivo: String,
    val nivel: String,
    val diasSemana: Int,
    val minutosSessao: Int,
    val local: String,
    val possuiEquipamento: Boolean,
    val restricoes: List<String>,
    val peso: Double,
    val altura: Double,
)

/** Corpo do PATCH /api/avaliacoes/{id}: apenas os campos editáveis. */
data class AvaliacaoUpdate(
    val observacao: String? = null,
    val favorita: Boolean? = null,
)

/** Avaliação completa devolvida pela API, já com o plano gerado. */
data class Avaliacao(
    val id: Long,
    val usuarioId: Long,
    val objetivo: String,
    val nivel: String,
    val diasSemana: Int,
    val minutosSessao: Int,
    val local: String,
    val possuiEquipamento: Boolean,
    val restricoes: List<String>,
    val peso: Double,
    val altura: Double,
    val imc: Double,
    val classificacaoImc: String,
    val intensidade: Int,
    val classificacao: String,
    val perfil: String,
    val plano: List<DiaTreino>,
    val dicas: List<String>,
    val observacao: String,
    val favorita: Boolean,
    val criadoEm: String,
)

data class DiaTreino(
    val dia: String,
    val foco: String,
    val exercicios: List<Exercicio>,
)

data class Exercicio(
    val nome: String,
    val grupo: String,
    val series: Int,
    val repeticoes: String,
)

/** Formato de erro padrão da API. */
data class ErroApi(
    val erro: String?,
    val detalhes: List<String>?,
)

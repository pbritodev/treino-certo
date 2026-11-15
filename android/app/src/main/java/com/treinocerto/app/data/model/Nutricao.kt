package com.treinocerto.app.data.model

/** Alimento com valores nutricionais por 100 g (ou 100 ml). */
data class Alimento(
    val id: Long,
    val nome: String,
    val categoria: String,
    val kcal: Double,
    val proteina: Double,
    val carboidrato: Double,
    val gordura: Double,
    val porcaoG: Double,
    val porcaoDescricao: String,
) {
    /** Calorias para a quantidade informada (regra de três sobre os 100 g). */
    fun kcalPara(quantidade: Double) = kcal * quantidade / 100
    fun proteinaPara(quantidade: Double) = proteina * quantidade / 100
    fun carboidratoPara(quantidade: Double) = carboidrato * quantidade / 100
    fun gorduraPara(quantidade: Double) = gordura * quantidade / 100
}

data class RegistroAlimentarRequest(
    val usuarioId: Long,
    val alimentoId: Long,
    val data: String,
    val refeicao: String,
    val quantidade: Double,
)

data class RegistroAlimentarUpdate(
    val quantidade: Double,
    val refeicao: String,
)

/** Item do diário: alimento consumido com nutrientes já calculados pela API. */
data class RegistroAlimentar(
    val id: Long,
    val alimentoId: Long,
    val alimento: String,
    val porcaoDescricao: String,
    val porcaoG: Double,
    val data: String,
    val refeicao: String,
    val quantidade: Double,
    val kcal: Double,
    val proteina: Double,
    val carboidrato: Double,
    val gordura: Double,
)

data class Nutrientes(
    val kcal: Double,
    val proteina: Double,
    val carboidrato: Double,
    val gordura: Double,
)

data class RefeicaoDiario(
    val refeicao: String,
    val kcal: Double,
    val itens: List<RegistroAlimentar>,
)

data class Diario(
    val data: String,
    val metaCalorica: Int,
    val metaProteina: Int?,
    val metaDefinidaPorAvaliacao: Boolean,
    val totais: Nutrientes,
    val restante: Int,
    val refeicoes: List<RefeicaoDiario>,
)

data class PesagemRequest(
    val usuarioId: Long,
    val peso: Double,
    val data: String,
    val observacao: String,
)

data class Pesagem(
    val id: Long,
    val peso: Double,
    val data: String,
    val observacao: String,
)

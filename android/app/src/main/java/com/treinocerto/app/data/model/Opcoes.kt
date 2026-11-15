package com.treinocerto.app.data.model

/**
 * Valores aceitos pela API (campo [api]) e o texto exibido ao usuário (campo [rotulo]).
 * Centralizar aqui evita espalhar strings mágicas pelas telas.
 */
interface OpcaoApi {
    val api: String
    val rotulo: String
}

enum class Sexo(override val api: String, override val rotulo: String) : OpcaoApi {
    MASCULINO("MASCULINO", "Masculino"),
    FEMININO("FEMININO", "Feminino"),
    OUTRO("OUTRO", "Outro"),
}

enum class Objetivo(override val api: String, override val rotulo: String) : OpcaoApi {
    EMAGRECIMENTO("EMAGRECIMENTO", "Emagrecimento"),
    HIPERTROFIA("HIPERTROFIA", "Ganho de massa muscular"),
    CONDICIONAMENTO("CONDICIONAMENTO", "Condicionamento físico"),
    SAUDE("SAUDE", "Saúde e bem-estar"),
}

enum class Nivel(override val api: String, override val rotulo: String) : OpcaoApi {
    INICIANTE("INICIANTE", "Iniciante"),
    INTERMEDIARIO("INTERMEDIARIO", "Intermediário"),
    AVANCADO("AVANCADO", "Avançado"),
}

enum class Local(override val api: String, override val rotulo: String) : OpcaoApi {
    CASA("CASA", "Em casa"),
    ACADEMIA("ACADEMIA", "Academia"),
    AR_LIVRE("AR_LIVRE", "Ao ar livre"),
}

enum class Restricao(override val api: String, override val rotulo: String) : OpcaoApi {
    JOELHO("JOELHO", "Joelho"),
    COLUNA("COLUNA", "Coluna"),
    OMBRO("OMBRO", "Ombro"),
    CARDIACO("CARDIACO", "Cardíaca"),
}

/** Converte o valor vindo da API no rótulo amigável (ou devolve o próprio valor). */
inline fun <reified T> rotuloDe(valorApi: String): String
    where T : Enum<T>, T : OpcaoApi =
    enumValues<T>().firstOrNull { it.api == valorApi }?.rotulo ?: valorApi

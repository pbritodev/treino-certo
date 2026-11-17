package com.treinocerto.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Conversões de datas e números entre o formato da API e o exibido na tela. */
object Formatos {

    private val LOCALE_BR = Locale("pt", "BR")
    private val formatoApi = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val formatoCurto = SimpleDateFormat("dd/MM/yyyy", LOCALE_BR)
    private val formatoLongo = SimpleDateFormat("EEEE, dd 'de' MMMM", LOCALE_BR)
    private val formatoDiaMes = SimpleDateFormat("dd/MM", LOCALE_BR)
    private val formatoDataHoraApi = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun hojeApi(): String = formatoApi.format(Date())

    fun paraApi(calendario: Calendar): String = formatoApi.format(calendario.time)

    fun calendarioDe(dataApi: String): Calendar =
        Calendar.getInstance().apply { time = formatoApi.parse(dataApi) ?: Date() }

    /** "2026-11-16" -> "16/11/2026" */
    fun dataCurta(dataApi: String): String =
        runCatching { formatoCurto.format(formatoApi.parse(dataApi)!!) }.getOrDefault(dataApi)

    /** "2026-11-16" -> "16/11" */
    fun diaMes(dataApi: String): String =
        runCatching { formatoDiaMes.format(formatoApi.parse(dataApi)!!) }.getOrDefault(dataApi)

    /** "2026-11-16" -> "Segunda-feira, 16 de novembro" */
    fun dataLonga(dataApi: String): String =
        runCatching {
            formatoLongo.format(formatoApi.parse(dataApi)!!).replaceFirstChar { it.titlecase(LOCALE_BR) }
        }.getOrDefault(dataApi)

    /** "2026-11-16 19:30:00" -> "16/11/2026 às 19:30" */
    fun dataHora(dataHoraApi: String): String = runCatching {
        val data = formatoDataHoraApi.parse(dataHoraApi)!!
        "${formatoCurto.format(data)} às ${SimpleDateFormat("HH:mm", LOCALE_BR).format(data)}"
    }.getOrDefault(dataHoraApi)

    /** Número com no máximo 1 casa decimal e vírgula: 82.0 -> "82", 82.45 -> "82,5" */
    fun decimal(valor: Double): String =
        if (valor % 1.0 == 0.0) valor.toLong().toString()
        else String.format(LOCALE_BR, "%.1f", valor)

    /** Número inteiro formatado no padrão do aparelho (ex.: 1.250). */
    fun inteiro(valor: Int): String = String.format(Locale.getDefault(), "%,d", valor)

    /** Lê números digitados com vírgula ou ponto. */
    fun lerDecimal(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()
}

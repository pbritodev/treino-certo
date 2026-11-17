package com.treinocerto.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.net.toUri

/**
 * Intents IMPLÍCITAS: o app descreve a ação (ver, enviar, compartilhar) e o
 * Android escolhe qual aplicativo instalado vai tratá-la.
 */
object IntentsExternas {

    /** Abre o seletor de compartilhamento (WhatsApp, e-mail, etc.). */
    fun compartilharTexto(context: Context, titulo: String, texto: String) {
        val envio = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, titulo)
            putExtra(Intent.EXTRA_TEXT, texto)
        }
        iniciar(context, Intent.createChooser(envio, titulo))
    }

    /** Abre uma página no navegador. */
    fun abrirUrl(context: Context, url: String) {
        iniciar(context, Intent(Intent.ACTION_VIEW, url.toUri()))
    }

    /** Abre o app de e-mail já com destinatário e assunto preenchidos. */
    fun enviarEmail(context: Context, destinatario: String, assunto: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(destinatario))
            putExtra(Intent.EXTRA_SUBJECT, assunto)
        }
        iniciar(context, intent)
    }

    /** Abre o app de mapas pesquisando o termo próximo à localização do usuário. */
    fun buscarNoMapa(context: Context, termo: String) {
        iniciar(context, Intent(Intent.ACTION_VIEW, "geo:0,0?q=${Uri.encode(termo)}".toUri()))
    }

    /**
     * Abre o app de agenda/calendário do celular (Google Agenda, por exemplo) com um
     * evento de treino já preenchido. O usuário só confirma e salva.
     */
    fun adicionarNaAgenda(context: Context, titulo: String, descricao: String, inicioMillis: Long, duracaoMin: Int) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, titulo)
            putExtra(CalendarContract.Events.DESCRIPTION, descricao)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, inicioMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, inicioMillis + duracaoMin * 60_000L)
        }
        iniciar(context, intent)
    }

    /** Busca vídeos de execução do exercício no YouTube (app ou navegador). */
    fun buscarVideo(context: Context, termo: String) {
        abrirUrl(context, "https://www.youtube.com/results?search_query=${Uri.encode("como fazer $termo")}")
    }

    private fun iniciar(context: Context, intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            context.toast("Nenhum aplicativo encontrado para esta ação")
        }
    }
}

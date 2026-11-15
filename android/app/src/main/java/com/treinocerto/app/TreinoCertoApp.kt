package com.treinocerto.app

import android.app.Application
import com.treinocerto.app.data.local.PreferenciasTema

/** Classe Application: aplica o tema salvo antes de qualquer tela ser aberta. */
class TreinoCertoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PreferenciasTema(this).aplicar()
    }
}

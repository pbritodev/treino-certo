package com.treinocerto.app.ui.sobre

import android.os.Bundle
import com.treinocerto.app.BuildConfig
import com.treinocerto.app.R
import com.treinocerto.app.data.local.PreferenciasTema
import com.treinocerto.app.data.local.PreferenciasTema.Tema
import com.treinocerto.app.databinding.ActivitySobreBinding
import com.treinocerto.app.ui.common.TelaBase
import com.treinocerto.app.util.IntentsExternas

/** Informações do app e atalhos que usam Intents implícitas (web, e-mail, compartilhar). */
class SobreActivity : TelaBase() {

    private lateinit var binding: ActivitySobreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySobreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.menu_sobre)

        binding.txtVersao.text = getString(R.string.sobre_versao, BuildConfig.VERSION_NAME)
        configurarTema()

        binding.btnSwagger.setOnClickListener {
            IntentsExternas.abrirUrl(this, BuildConfig.API_BASE_URL + "docs")
        }
        binding.btnOms.setOnClickListener {
            IntentsExternas.abrirUrl(this, getString(R.string.url_oms))
        }
        binding.btnContato.setOnClickListener {
            IntentsExternas.enviarEmail(this, getString(R.string.email_contato), getString(R.string.app_name))
        }
        binding.btnCompartilharApp.setOnClickListener {
            IntentsExternas.compartilharTexto(this, getString(R.string.app_name), getString(R.string.sobre_texto_compartilhar))
        }
    }

    /** RadioGroup de aparência: sistema, claro ou escuro (salvo em SharedPreferences). */
    private fun configurarTema() {
        val preferencias = PreferenciasTema(this)
        val radios = mapOf(
            Tema.SISTEMA to binding.rbTemaSistema,
            Tema.CLARO to binding.rbTemaClaro,
            Tema.ESCURO to binding.rbTemaEscuro,
        )
        radios[preferencias.tema]?.isChecked = true
        binding.grupoTema.setOnCheckedChangeListener { _, idMarcado ->
            val escolhido = radios.entries.first { it.value.id == idMarcado }.key
            if (escolhido != preferencias.tema) preferencias.tema = escolhido
        }
    }
}

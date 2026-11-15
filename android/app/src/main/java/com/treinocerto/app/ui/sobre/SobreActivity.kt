package com.treinocerto.app.ui.sobre

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.treinocerto.app.BuildConfig
import com.treinocerto.app.R
import com.treinocerto.app.databinding.ActivitySobreBinding
import com.treinocerto.app.util.IntentsExternas

/** Informações do app e atalhos que usam Intents implícitas (web, e-mail, compartilhar). */
class SobreActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySobreBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySobreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.menu_sobre)

        binding.txtVersao.text = getString(R.string.sobre_versao, BuildConfig.VERSION_NAME)

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

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

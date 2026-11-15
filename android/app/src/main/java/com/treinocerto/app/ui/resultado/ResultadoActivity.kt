package com.treinocerto.app.ui.resultado

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.treinocerto.app.R
import com.treinocerto.app.data.model.Avaliacao
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.ActivityResultadoBinding
import com.treinocerto.app.ui.common.ExibidorPlano
import com.treinocerto.app.ui.common.Extras
import com.treinocerto.app.ui.common.TelaBase
import com.treinocerto.app.ui.common.exibirPlanoComTreinosDeHoje
import com.treinocerto.app.ui.diario.DiarioActivity
import com.treinocerto.app.ui.historico.HistoricoActivity
import com.treinocerto.app.ui.home.MainActivity
import com.treinocerto.app.util.IntentsExternas
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.visivel
import kotlinx.coroutines.launch

/** Exibe o plano recém-gerado, buscando a avaliação na API (GET /api/avaliacoes/{id}). */
class ResultadoActivity : TelaBase() {

    private lateinit var binding: ActivityResultadoBinding
    private val repositorio = TreinoRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultadoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.resultado_titulo)

        val avaliacaoId = intent.getLongExtra(Extras.AVALIACAO_ID, -1)
        if (avaliacaoId <= 0) return finish()
        carregar(avaliacaoId)

        binding.btnHistorico.setOnClickListener {
            startActivity(Intent(this, HistoricoActivity::class.java))
            finish()
        }
        binding.btnDiario.setOnClickListener {
            startActivity(Intent(this, DiarioActivity::class.java))
        }
        binding.btnInicio.setOnClickListener { voltarAoInicio() }
    }

    private fun carregar(id: Long) {
        binding.progresso.visivel(true)
        lifecycleScope.launch {
            when (val resultado = repositorio.buscarAvaliacao(id)) {
                is Resultado.Sucesso -> exibir(resultado.dado)
                is Resultado.Erro -> mostrarErro(resultado.mensagem) { carregar(id) }
            }
            binding.progresso.visivel(false)
        }
    }

    private fun exibir(avaliacao: Avaliacao) {
        binding.conteudo.visivel(true)
        exibirPlanoComTreinosDeHoje(binding.plano, avaliacao)

        binding.btnCompartilhar.setOnClickListener {
            IntentsExternas.compartilharTexto(this, getString(R.string.app_name), ExibidorPlano.textoParaCompartilhar(avaliacao))
        }
        binding.btnMapa.setOnClickListener {
            val termo = if (avaliacao.local == "AR_LIVRE") R.string.busca_mapa_ar_livre else R.string.busca_mapa
            IntentsExternas.buscarNoMapa(this, getString(termo))
        }
    }

    /** Volta para a Home limpando a pilha (evita voltar ao questionário com o botão "voltar"). */
    private fun voltarAoInicio() {
        val intent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }
}

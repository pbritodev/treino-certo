package com.treinocerto.app.ui.detalhe

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.treinocerto.app.R
import com.treinocerto.app.data.model.Avaliacao
import com.treinocerto.app.data.model.AvaliacaoUpdate
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.ActivityDetalheBinding
import com.treinocerto.app.ui.common.ExibidorPlano
import com.treinocerto.app.ui.common.Extras
import com.treinocerto.app.ui.common.registrarTreinoConcluido
import com.treinocerto.app.ui.common.confirmarExclusao
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.IntentsExternas
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.toast
import com.treinocerto.app.util.visivel
import kotlinx.coroutines.launch

/** Detalhe de uma avaliação do histórico: consulta (GET), edição (PATCH) e exclusão (DELETE). */
class DetalheActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalheBinding
    private val repositorio = TreinoRepository()
    private var avaliacaoId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalheBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.detalhe_titulo)

        avaliacaoId = intent.getLongExtra(Extras.AVALIACAO_ID, -1)
        if (avaliacaoId <= 0) return finish()

        binding.btnSalvar.setOnClickListener { salvarAnotacoes() }
        binding.btnExcluir.setOnClickListener { confirmarRemocao() }
        carregar()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun carregar() {
        binding.progresso.visivel(true)
        lifecycleScope.launch {
            when (val resultado = repositorio.buscarAvaliacao(avaliacaoId)) {
                is Resultado.Sucesso -> exibir(resultado.dado)
                is Resultado.Erro -> mostrarErro(resultado.mensagem) { carregar() }
            }
            binding.progresso.visivel(false)
        }
    }

    private fun exibir(avaliacao: Avaliacao) {
        binding.conteudo.visivel(true)
        binding.txtData.text = getString(R.string.detalhe_criado_em, Formatos.dataHora(avaliacao.criadoEm))
        ExibidorPlano.exibir(binding.plano, avaliacao) { dia ->
            registrarTreinoConcluido(avaliacao.id, dia, avaliacao.minutosSessao)
        }
        binding.edtObservacao.setText(avaliacao.observacao)
        binding.swFavorita.isChecked = avaliacao.favorita
        binding.btnCompartilhar.setOnClickListener {
            IntentsExternas.compartilharTexto(this, getString(R.string.app_name), ExibidorPlano.textoParaCompartilhar(avaliacao))
        }
    }

    private fun salvarAnotacoes() {
        val update = AvaliacaoUpdate(
            observacao = binding.edtObservacao.text.toString().trim(),
            favorita = binding.swFavorita.isChecked,
        )
        binding.btnSalvar.isEnabled = false
        lifecycleScope.launch {
            when (val resultado = repositorio.atualizarAvaliacao(avaliacaoId, update)) {
                is Resultado.Sucesso -> toast(getString(R.string.detalhe_salvo))
                is Resultado.Erro -> mostrarErro(resultado.mensagem) { salvarAnotacoes() }
            }
            binding.btnSalvar.isEnabled = true
        }
    }

    private fun confirmarRemocao() {
        confirmarExclusao(
            getString(R.string.detalhe_confirmar_exclusao_titulo),
            getString(R.string.detalhe_confirmar_exclusao),
        ) {
            lifecycleScope.launch {
                when (val resultado = repositorio.removerAvaliacao(avaliacaoId)) {
                    is Resultado.Sucesso -> {
                        toast(getString(R.string.historico_excluido))
                        finish()
                    }
                    is Resultado.Erro -> mostrarErro(resultado.mensagem)
                }
            }
        }
    }
}

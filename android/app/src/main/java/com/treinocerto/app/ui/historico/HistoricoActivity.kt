package com.treinocerto.app.ui.historico

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.treinocerto.app.R
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.data.model.Avaliacao
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.ActivityHistoricoBinding
import com.treinocerto.app.ui.common.Extras
import com.treinocerto.app.ui.common.TelaBase
import com.treinocerto.app.ui.common.confirmarExclusao
import com.treinocerto.app.ui.common.habilitarDeslizarParaExcluir
import com.treinocerto.app.ui.detalhe.DetalheActivity
import com.treinocerto.app.ui.questionario.QuestionarioActivity
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.toast
import com.treinocerto.app.util.visivel
import kotlinx.coroutines.launch

/** Lista (RecyclerView) de todas as avaliações do usuário, com filtro e exclusão. */
class HistoricoActivity : TelaBase() {

    private lateinit var binding: ActivityHistoricoBinding
    private lateinit var sessao: SessaoUsuario
    private val repositorio = TreinoRepository()
    private var avaliacoes: List<Avaliacao> = emptyList()

    private val adapter = AvaliacaoAdapter { avaliacao ->
        startActivity(Intent(this, DetalheActivity::class.java).putExtra(Extras.AVALIACAO_ID, avaliacao.id))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoricoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.historico_titulo)
        sessao = SessaoUsuario(this)

        binding.rvAvaliacoes.layoutManager = LinearLayoutManager(this)
        binding.rvAvaliacoes.adapter = adapter
        binding.rvAvaliacoes.habilitarDeslizarParaExcluir { posicao -> confirmarRemocao(adapter.currentList[posicao]) }
        binding.swSomenteFavoritas.setOnCheckedChangeListener { _, _ -> atualizarLista() }

        binding.vazio.imgVazio.setImageResource(R.drawable.ic_historico)
        binding.vazio.btnVazio.setText(R.string.menu_nova_avaliacao)
        binding.vazio.btnVazio.setOnClickListener {
            startActivity(Intent(this, QuestionarioActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        carregar() // recarrega ao voltar do Detalhe (pode ter sido editado/excluído)
    }

    private fun carregar() {
        val usuarioId = sessao.usuarioId ?: return finish()
        binding.progresso.visivel(avaliacoes.isEmpty())
        lifecycleScope.launch {
            when (val resultado = repositorio.listarAvaliacoes(usuarioId)) {
                is Resultado.Sucesso -> {
                    avaliacoes = resultado.dado
                    atualizarLista()
                }
                is Resultado.Erro -> mostrarErro(resultado.mensagem) { carregar() }
            }
            binding.progresso.visivel(false)
        }
    }

    private fun atualizarLista() {
        val somenteFavoritas = binding.swSomenteFavoritas.isChecked
        val lista = if (somenteFavoritas) avaliacoes.filter { it.favorita } else avaliacoes
        adapter.submitList(lista)

        binding.vazio.root.visivel(lista.isEmpty())
        binding.vazio.txtVazio.setText(if (somenteFavoritas) R.string.historico_vazio_favoritas else R.string.historico_vazio)
        binding.vazio.btnVazio.visivel(!somenteFavoritas)
    }

    private fun confirmarRemocao(avaliacao: Avaliacao) {
        confirmarExclusao(
            titulo = getString(R.string.detalhe_confirmar_exclusao_titulo),
            mensagem = getString(R.string.detalhe_confirmar_exclusao),
            aoCancelar = { adapter.notifyItemChanged(adapter.currentList.indexOf(avaliacao)) },
        ) { remover(avaliacao) }
    }

    private fun remover(avaliacao: Avaliacao) {
        lifecycleScope.launch {
            when (val resultado = repositorio.removerAvaliacao(avaliacao.id)) {
                is Resultado.Sucesso -> {
                    avaliacoes = avaliacoes - avaliacao
                    atualizarLista()
                    toast(getString(R.string.historico_excluido))
                }
                is Resultado.Erro -> {
                    adapter.notifyItemChanged(adapter.currentList.indexOf(avaliacao))
                    mostrarErro(resultado.mensagem)
                }
            }
        }
    }
}

package com.treinocerto.app.ui.treinos

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.treinocerto.app.R
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.data.model.TreinoRealizado
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.ActivityTreinosBinding
import com.treinocerto.app.ui.common.TelaBase
import com.treinocerto.app.ui.common.confirmarExclusao
import com.treinocerto.app.ui.common.habilitarDeslizarParaExcluir
import com.treinocerto.app.ui.progresso.TreinoRealizadoAdapter
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.IntentsExternas
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.toast
import com.treinocerto.app.util.visivel
import java.util.Calendar
import kotlinx.coroutines.launch

/**
 * "Meus treinos": todos os treinos feitos (do plano e avulsos), resumo da semana,
 * cadastro de novo treino e as Intents implícitas de agenda e de academias próximas.
 */
class TreinosActivity : TelaBase() {

    private lateinit var binding: ActivityTreinosBinding
    private lateinit var sessao: SessaoUsuario
    private val repositorio = TreinoRepository()
    private val adapter = TreinoRealizadoAdapter { treino -> mostrarAcoes(treino) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTreinosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.treinos_titulo)
        sessao = SessaoUsuario(this)

        binding.estatSemana.txtRotulo.setText(R.string.treinos_semana)
        binding.estatMinutos.txtRotulo.setText(R.string.treinos_minutos)
        binding.estatKcal.txtRotulo.setText(R.string.treinos_kcal)

        binding.rvTreinos.layoutManager = LinearLayoutManager(this)
        binding.rvTreinos.adapter = adapter
        binding.rvTreinos.habilitarDeslizarParaExcluir { posicao -> confirmarRemocao(posicao) }

        binding.fabNovoTreino.setOnClickListener { startActivity(Intent(this, NovoTreinoActivity::class.java)) }
        binding.btnAcademias.setOnClickListener {
            IntentsExternas.buscarNoMapa(this, getString(R.string.busca_mapa))
        }
        binding.btnAgendar.setOnClickListener { agendarProximoTreino() }
    }

    override fun onResume() {
        super.onResume()
        carregar() // atualiza ao voltar do cadastro de treino
    }

    private fun carregar() {
        val usuarioId = sessao.usuarioId ?: return finish()
        lifecycleScope.launch {
            when (val resultado = repositorio.listarTreinosRealizados(usuarioId)) {
                is Resultado.Sucesso -> exibir(resultado.dado)
                is Resultado.Erro -> mostrarErro(resultado.mensagem) { carregar() }
            }
            binding.progresso.visivel(false)
        }
    }

    private fun exibir(treinos: List<TreinoRealizado>) {
        adapter.submitList(treinos)
        binding.txtVazio.visivel(treinos.isEmpty())

        // Resumo dos últimos 7 dias (hoje e os 6 anteriores).
        val inicioSemana = Formatos.paraApi(
            Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -6) },
        )
        val daSemana = treinos.filter { it.data >= inicioSemana }
        binding.estatSemana.txtValor.text = Formatos.inteiro(daSemana.size)
        binding.estatMinutos.txtValor.text = Formatos.inteiro(daSemana.sumOf { it.duracaoMin })
        binding.estatKcal.txtValor.text = Formatos.inteiro(daSemana.sumOf { it.kcal ?: 0 })
    }

    /** Intent implícita: abre a agenda com um treino amanhã às 7h. */
    private fun agendarProximoTreino() {
        val amanha = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
        }
        IntentsExternas.adicionarNaAgenda(
            this,
            titulo = getString(R.string.treinos_agendar_titulo),
            descricao = getString(R.string.treinos_agendar_descricao),
            inicioMillis = amanha.timeInMillis,
            duracaoMin = 60,
        )
    }

    /** Toque em um treino: adicionar à agenda, compartilhar ou excluir. */
    private fun mostrarAcoes(treino: TreinoRealizado) {
        val acoes = arrayOf(
            getString(R.string.treinos_acao_agenda),
            getString(R.string.treinos_acao_compartilhar),
            getString(R.string.excluir),
        )
        MaterialAlertDialogBuilder(this)
            .setTitle(ExibicaoTreino.titulo(this, treino))
            .setItems(acoes) { _, indice ->
                when (indice) {
                    0 -> ExibicaoTreino.adicionarNaAgenda(this, treino)
                    1 -> compartilhar(treino)
                    else -> confirmarRemocao(adapter.currentList.indexOf(treino))
                }
            }
            .show()
    }

    private fun compartilhar(treino: TreinoRealizado) {
        val kcal = treino.kcal?.let { getString(R.string.treinos_kcal_sufixo, it) }.orEmpty()
        IntentsExternas.compartilharTexto(
            this,
            getString(R.string.app_name),
            getString(R.string.treinos_compartilhar_texto, ExibicaoTreino.titulo(this, treino), treino.duracaoMin, kcal),
        )
    }

    private fun confirmarRemocao(posicao: Int) {
        val treino = adapter.currentList[posicao]
        confirmarExclusao(
            titulo = getString(R.string.excluir),
            mensagem = ExibicaoTreino.titulo(this, treino),
            aoCancelar = { adapter.notifyItemChanged(posicao) },
        ) {
            lifecycleScope.launch {
                when (val resultado = repositorio.removerTreino(treino.id)) {
                    is Resultado.Sucesso -> {
                        toast(getString(R.string.treinos_removido))
                        carregar()
                    }
                    is Resultado.Erro -> {
                        adapter.notifyItemChanged(posicao)
                        mostrarErro(resultado.mensagem)
                    }
                }
            }
        }
    }
}

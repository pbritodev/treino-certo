package com.treinocerto.app.ui.peso

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.treinocerto.app.R
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.data.model.Pesagem
import com.treinocerto.app.data.model.PesagemEntrada
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.ActivityPesoBinding
import com.treinocerto.app.databinding.LayoutEstatisticaBinding
import com.treinocerto.app.ui.common.TelaBase
import com.treinocerto.app.ui.common.confirmarExclusao
import com.treinocerto.app.ui.common.habilitarDeslizarParaExcluir
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.toast
import com.treinocerto.app.util.visivel
import java.util.Calendar
import kotlin.math.abs
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/** Registro de pesagens (POST), histórico (GET), exclusão (DELETE) e gráfico de evolução. */
class PesoActivity : TelaBase() {

    private lateinit var binding: ActivityPesoBinding
    private lateinit var sessao: SessaoUsuario
    private val repositorio = TreinoRepository()
    private val adapter = PesagemAdapter()
    private val dataPesagem: Calendar = Calendar.getInstance()
    private var pesoMeta: Double? = null
    private var pesoDaAvaliacao: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPesoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.peso_titulo)
        sessao = SessaoUsuario(this)

        binding.estatAtual.txtRotulo.setText(R.string.peso_atual)
        binding.estatVariacao.txtRotulo.setText(R.string.peso_variacao)
        binding.estatMeta.txtRotulo.setText(R.string.peso_meta)

        binding.rvPesagens.layoutManager = LinearLayoutManager(this)
        binding.rvPesagens.adapter = adapter
        binding.rvPesagens.habilitarDeslizarParaExcluir { posicao ->
            confirmarRemocao(adapter.currentList[posicao].pesagem, posicao)
        }

        binding.edtData.setText(Formatos.dataCurta(Formatos.paraApi(dataPesagem)))
        binding.edtData.setOnClickListener { escolherData() }
        binding.btnRegistrar.setOnClickListener { registrar() }

        carregar()
    }

    /** Busca pesagens e a meta (última avaliação) em paralelo. */
    private fun carregar() {
        val usuarioId = sessao.usuarioId ?: return finish()
        binding.progresso.visivel(true)
        lifecycleScope.launch {
            val pesagens = async { repositorio.listarPesagens(usuarioId) }
            val avaliacoes = async { repositorio.listarAvaliacoes(usuarioId) }

            (avaliacoes.await() as? Resultado.Sucesso)?.dado?.firstOrNull()?.let { ultima ->
                pesoMeta = ultima.pesoMeta
                pesoDaAvaliacao = ultima.peso
            }
            when (val resultado = pesagens.await()) {
                is Resultado.Sucesso -> exibir(resultado.dado)
                is Resultado.Erro -> mostrarErro(resultado.mensagem) { carregar() }
            }
            binding.progresso.visivel(false)
        }
    }

    private fun exibir(pesagens: List<Pesagem>) {
        // Peso atual = última pesagem; sem pesagens, usa o peso informado no questionário.
        val atual = pesagens.lastOrNull()?.peso ?: pesoDaAvaliacao
        val inicial = pesagens.firstOrNull()?.peso ?: pesoDaAvaliacao

        exibirEstatistica(binding.estatAtual, atual)
        exibirEstatistica(binding.estatMeta, pesoMeta)
        binding.estatVariacao.txtValor.text = if (atual != null && inicial != null) {
            val variacao = atual - inicial
            (if (variacao > 0) "+" else if (variacao < 0) "−" else "") + Formatos.decimal(abs(variacao))
        } else {
            getString(R.string.peso_sem_meta)
        }

        val meta = pesoMeta
        binding.txtFaltaMeta.visivel(atual != null && meta != null)
        if (atual != null && meta != null) {
            val falta = abs(atual - meta)
            binding.txtFaltaMeta.text = if (falta < 0.1) getString(R.string.peso_meta_atingida)
            else getString(R.string.peso_falta_meta, Formatos.decimal(falta))
        }

        binding.grafico.visivel(pesagens.size >= 2)
        binding.txtGraficoVazio.visivel(pesagens.size < 2)
        binding.grafico.definirDados(pesagens.map { GraficoPesoView.Ponto(Formatos.diaMes(it.data), it.peso) }, meta)

        // Lista do mais recente para o mais antigo, com a variação para a pesagem anterior.
        val itens = pesagens.mapIndexed { i, p -> PesagemComVariacao(p, if (i > 0) p.peso - pesagens[i - 1].peso else null) }
        adapter.submitList(itens.reversed())
        binding.txtVazio.visivel(pesagens.isEmpty())

        if (binding.edtPeso.text.isNullOrBlank() && atual != null) binding.edtPeso.setText(Formatos.decimal(atual))
    }

    private fun exibirEstatistica(estatistica: LayoutEstatisticaBinding, valor: Double?) {
        estatistica.txtValor.text = valor?.let { Formatos.decimal(it) } ?: getString(R.string.peso_sem_meta)
    }

    private fun escolherData() {
        DatePickerDialog(
            this,
            { _, ano, mes, dia ->
                dataPesagem.set(ano, mes, dia)
                binding.edtData.setText(Formatos.dataCurta(Formatos.paraApi(dataPesagem)))
            },
            dataPesagem.get(Calendar.YEAR),
            dataPesagem.get(Calendar.MONTH),
            dataPesagem.get(Calendar.DAY_OF_MONTH),
        ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
    }

    private fun registrar() {
        val usuarioId = sessao.usuarioId ?: return
        val peso = Formatos.lerDecimal(binding.edtPeso.text.toString())
        binding.campoPeso.error = if (peso == null || peso !in 30.0..300.0) getString(R.string.q_erro_peso) else null
        if (peso == null || binding.campoPeso.error != null) return

        val entrada = PesagemEntrada(
            usuarioId = usuarioId,
            peso = peso,
            data = Formatos.paraApi(dataPesagem),
            observacao = binding.edtObservacao.text.toString().trim(),
        )
        binding.btnRegistrar.isEnabled = false
        lifecycleScope.launch {
            when (val resultado = repositorio.registrarPesagem(entrada)) {
                is Resultado.Sucesso -> {
                    toast(getString(R.string.peso_registrado))
                    binding.edtObservacao.text = null
                    carregar()
                }
                is Resultado.Erro -> mostrarErro(resultado.mensagem)
            }
            binding.btnRegistrar.isEnabled = true
        }
    }

    private fun confirmarRemocao(pesagem: Pesagem, posicao: Int) {
        confirmarExclusao(
            titulo = getString(R.string.excluir),
            mensagem = getString(R.string.peso_kg, Formatos.decimal(pesagem.peso)) + " · " + Formatos.dataCurta(pesagem.data),
            aoCancelar = { adapter.notifyItemChanged(posicao) },
        ) {
            lifecycleScope.launch {
                when (val resultado = repositorio.removerPesagem(pesagem.id)) {
                    is Resultado.Sucesso -> {
                        toast(getString(R.string.peso_removido))
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

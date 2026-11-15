package com.treinocerto.app.ui.progresso

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.treinocerto.app.R
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.data.model.DiaDaSemana
import com.treinocerto.app.data.model.Progresso
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.ActivityProgressoBinding
import com.treinocerto.app.databinding.LayoutEstatisticaBinding
import com.treinocerto.app.ui.common.Extras
import com.treinocerto.app.ui.common.confirmarExclusao
import com.treinocerto.app.ui.common.habilitarDeslizarParaExcluir
import com.treinocerto.app.ui.common.iniciais
import com.treinocerto.app.ui.common.registrarTreinoConcluido
import com.treinocerto.app.ui.detalhe.DetalheActivity
import com.treinocerto.app.ui.peso.PesoActivity
import com.treinocerto.app.ui.questionario.QuestionarioActivity
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.toast
import com.treinocerto.app.util.visivel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

/**
 * "Meu progresso": perfil, plano atual, estatísticas dos treinos feitos
 * (semana, sequência, total, minutos), últimos 7 dias, peso e lista de treinos.
 */
class ProgressoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProgressoBinding
    private lateinit var sessao: SessaoUsuario
    private val repositorio = TreinoRepository()
    private val adapter = TreinoRealizadoAdapter()
    private var progresso: Progresso? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.progresso_titulo)
        sessao = SessaoUsuario(this)

        binding.estatSemana.txtRotulo.setText(R.string.progresso_semana)
        binding.estatSequencia.txtRotulo.setText(R.string.progresso_sequencia)
        binding.estatTotal.txtRotulo.setText(R.string.progresso_total)
        binding.estatMinutos.txtRotulo.setText(R.string.progresso_minutos)

        binding.rvTreinos.layoutManager = LinearLayoutManager(this)
        binding.rvTreinos.adapter = adapter
        binding.rvTreinos.habilitarDeslizarParaExcluir { posicao -> confirmarRemocao(posicao) }

        binding.cardPeso.setOnClickListener { startActivity(Intent(this, PesoActivity::class.java)) }
        binding.btnRegistrar.setOnClickListener { escolherTreinoDeHoje() }
        binding.btnVerPlano.setOnClickListener { abrirPlanoAtual() }
    }

    override fun onResume() {
        super.onResume()
        carregar() // atualiza após concluir um treino no plano
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun carregar() {
        val usuarioId = sessao.usuarioId ?: return finish()
        lifecycleScope.launch {
            when (val resultado = repositorio.buscarProgresso(usuarioId)) {
                is Resultado.Sucesso -> exibir(resultado.dado)
                is Resultado.Erro -> mostrarErro(resultado.mensagem) { carregar() }
            }
            binding.progresso.visivel(false)
        }
    }

    private fun exibir(p: Progresso) {
        progresso = p
        binding.conteudo.visivel(true)

        binding.txtAvatar.text = iniciais(p.usuario.nome)
        binding.txtNome.text = p.usuario.nome
        binding.txtPlano.text = p.planoAtual?.perfil ?: getString(R.string.progresso_sem_plano)

        estatistica(binding.estatSemana, getString(R.string.progresso_semana_valor, p.treinosSemana, p.metaSemanal))
        estatistica(binding.estatSequencia, "🔥 ${p.sequenciaDias}")
        estatistica(binding.estatTotal, p.totalTreinos.toString())
        estatistica(binding.estatMinutos, p.minutosTotais.toString())

        binding.barraSemana.max = p.metaSemanal
        binding.barraSemana.progress = p.treinosSemana.coerceAtMost(p.metaSemanal)
        val faltam = p.metaSemanal - p.treinosSemana
        binding.txtMotivacao.text = if (faltam <= 0) getString(R.string.progresso_motivacao_meta)
        else getString(R.string.progresso_motivacao_falta, faltam)

        exibirUltimosDias(p.ultimos7Dias)

        binding.cardPeso.visivel(p.pesoAtual != null)
        if (p.pesoInicial != null && p.pesoAtual != null) {
            binding.txtPeso.text = getString(
                R.string.progresso_peso_valor, Formatos.decimal(p.pesoInicial), Formatos.decimal(p.pesoAtual),
            )
        }
        binding.txtMetaPeso.text = p.planoAtual?.pesoMeta
            ?.let { getString(R.string.progresso_meta_peso, Formatos.decimal(it)) } ?: getString(R.string.progresso_peso)

        adapter.submitList(p.treinosRecentes)
        binding.txtVazio.visivel(p.treinosRecentes.isEmpty())
        binding.btnVerPlano.setText(if (p.planoAtual == null) R.string.menu_nova_avaliacao else R.string.progresso_ver_plano)
    }

    private fun estatistica(item: LayoutEstatisticaBinding, valor: String) {
        item.txtValor.text = valor
    }

    /** Um círculo por dia (letra do dia da semana + número), preenchido se houve treino. */
    private fun exibirUltimosDias(dias: List<DiaDaSemana>) {
        val container = binding.containerDias
        container.removeAllViews()
        val tamanho = (36 * resources.displayMetrics.density).toInt()
        dias.forEach { dia ->
            val calendario = Formatos.calendarioDe(dia.data)
            val letra = "DSTQQSS"[calendario.get(Calendar.DAY_OF_WEEK) - 1].toString()
            val treinou = dia.treinos > 0

            val coluna = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            coluna.addView(TextView(this).apply {
                text = letra
                setTextAppearance(R.style.Texto_Secundario)
            })
            coluna.addView(TextView(this).apply {
                text = String.format(Locale.getDefault(), "%d", calendario.get(Calendar.DAY_OF_MONTH))
                gravity = Gravity.CENTER
                setBackgroundResource(if (treinou) R.drawable.fundo_dia_ativo else R.drawable.fundo_dia_inativo)
                setTextColor(ContextCompat.getColor(context, if (treinou) R.color.sobre_primario else R.color.texto_secundario))
                layoutParams = LinearLayout.LayoutParams(tamanho, tamanho).apply { topMargin = tamanho / 6 }
            })
            container.addView(coluna)
        }
    }

    /** Escolhe qual dia do plano atual foi feito hoje e registra (POST). */
    private fun escolherTreinoDeHoje() {
        val plano = progresso?.planoAtual ?: return abrirPlanoAtual()
        lifecycleScope.launch {
            when (val resultado = repositorio.buscarAvaliacao(plano.id)) {
                is Resultado.Sucesso -> {
                    val avaliacao = resultado.dado
                    val opcoes = avaliacao.plano.map { getString(R.string.progresso_treino_item, it.dia, it.foco) }
                    MaterialAlertDialogBuilder(this@ProgressoActivity)
                        .setTitle(R.string.progresso_escolher_dia)
                        .setItems(opcoes.toTypedArray()) { _, indice ->
                            registrarTreinoConcluido(avaliacao.id, avaliacao.plano[indice], avaliacao.minutosSessao) { carregar() }
                        }
                        .setNegativeButton(R.string.cancelar, null)
                        .show()
                }
                is Resultado.Erro -> mostrarErro(resultado.mensagem)
            }
        }
    }

    private fun abrirPlanoAtual() {
        val plano = progresso?.planoAtual
        val intent = if (plano != null) {
            Intent(this, DetalheActivity::class.java).putExtra(Extras.AVALIACAO_ID, plano.id)
        } else {
            Intent(this, QuestionarioActivity::class.java)
        }
        startActivity(intent)
    }

    private fun confirmarRemocao(posicao: Int) {
        val treino = adapter.currentList[posicao]
        confirmarExclusao(
            titulo = getString(R.string.excluir),
            mensagem = getString(R.string.progresso_treino_item, treino.dia, Formatos.dataCurta(treino.data)),
            aoCancelar = { adapter.notifyItemChanged(posicao) },
        ) {
            lifecycleScope.launch {
                when (val resultado = repositorio.removerTreino(treino.id)) {
                    is Resultado.Sucesso -> {
                        toast(getString(R.string.progresso_treino_removido))
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

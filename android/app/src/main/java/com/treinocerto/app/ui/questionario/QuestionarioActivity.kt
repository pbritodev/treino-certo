package com.treinocerto.app.ui.questionario

import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import androidx.lifecycle.lifecycleScope
import com.treinocerto.app.R
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.data.model.AtividadeDiaria
import com.treinocerto.app.data.model.AvaliacaoEntrada
import com.treinocerto.app.data.model.Local
import com.treinocerto.app.data.model.Nivel
import com.treinocerto.app.data.model.Objetivo
import com.treinocerto.app.data.model.Restricao
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.ActivityQuestionarioBinding
import com.treinocerto.app.ui.common.Extras
import com.treinocerto.app.ui.common.TelaBase
import com.treinocerto.app.ui.common.configurarOpcoes
import com.treinocerto.app.ui.common.opcaoSelecionada
import com.treinocerto.app.ui.resultado.ResultadoActivity
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.toast
import com.treinocerto.app.util.visivel
import kotlinx.coroutines.launch

/** Formulário de avaliação: coleta as respostas e envia para a API gerar o plano. */
class QuestionarioActivity : TelaBase() {

    private lateinit var binding: ActivityQuestionarioBinding
    private lateinit var sessao: SessaoUsuario
    private val repositorio = TreinoRepository()

    private val opcoesMinutos = listOf(30, 45, 60, 90)

    private val radiosNivel by lazy {
        mapOf(binding.rbIniciante to Nivel.INICIANTE, binding.rbIntermediario to Nivel.INTERMEDIARIO,
            binding.rbAvancado to Nivel.AVANCADO)
    }
    private val radiosLocal by lazy {
        mapOf(binding.rbCasa to Local.CASA, binding.rbAcademia to Local.ACADEMIA, binding.rbArLivre to Local.AR_LIVRE)
    }
    private val checkRestricoes by lazy {
        mapOf(binding.cbJoelho to Restricao.JOELHO, binding.cbColuna to Restricao.COLUNA,
            binding.cbOmbro to Restricao.OMBRO, binding.cbCardiaco to Restricao.CARDIACO)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionarioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.q_titulo)
        sessao = SessaoUsuario(this)

        configurarCampos()
        binding.btnGerarPlano.setOnClickListener { enviar() }
    }

    private fun configurarCampos() {
        binding.spnObjetivo.configurarOpcoes(Objetivo.entries)
        binding.spnAtividade.configurarOpcoes(AtividadeDiaria.entries, AtividadeDiaria.LEVE)

        val adaptadorMinutos = android.widget.ArrayAdapter(
            this, android.R.layout.simple_spinner_item,
            opcoesMinutos.map { getString(R.string.q_minutos_opcao, it) },
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spnMinutos.adapter = adaptadorMinutos
        binding.spnMinutos.setSelection(opcoesMinutos.indexOf(60))

        aoMudarSeekBar(binding.seekDias) { binding.txtDias.text = getString(R.string.q_dias, diasSemana()) }
        aoMudarSeekBar(binding.seekSono) { binding.txtSono.text = getString(R.string.q_sono, horasSono()) }

        // O switch de halteres só faz sentido para quem treina em casa.
        binding.grupoLocal.setOnCheckedChangeListener { _, id -> binding.swEquipamento.visivel(id == binding.rbCasa.id) }
    }

    /** Reage às mudanças da SeekBar e já mostra o valor inicial. */
    private fun aoMudarSeekBar(seekBar: SeekBar, aoMudar: () -> Unit) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, progresso: Int, doUsuario: Boolean) = aoMudar()
            override fun onStartTrackingTouch(s: SeekBar?) = Unit
            override fun onStopTrackingTouch(s: SeekBar?) = Unit
        })
        aoMudar()
    }

    private fun diasSemana() = binding.seekDias.progress + 2 // SeekBar 0..4 -> 2..6 dias
    private fun horasSono() = binding.seekSono.progress + 3 // SeekBar 0..9 -> 3..12 horas

    private fun enviar() {
        val usuarioId = sessao.usuarioId ?: return finish()
        val entrada = lerFormulario(usuarioId) ?: return

        carregando(true)
        lifecycleScope.launch {
            val resultado = repositorio.criarAvaliacao(entrada)
            carregando(false)
            when (resultado) {
                is Resultado.Sucesso -> {
                    // Intent explícita levando o id da avaliação criada para a tela de resultado.
                    val intent = Intent(this@QuestionarioActivity, ResultadoActivity::class.java)
                        .putExtra(Extras.AVALIACAO_ID, resultado.dado.id)
                    startActivity(intent)
                    finish()
                }
                is Resultado.Erro -> mostrarErro(resultado.mensagem) { enviar() }
            }
        }
    }

    private fun lerFormulario(usuarioId: Long): AvaliacaoEntrada? {
        val peso = Formatos.lerDecimal(binding.edtPeso.text.toString())
        val alturaCm = binding.edtAltura.text.toString().toIntOrNull()
        val textoPesoMeta = binding.edtPesoMeta.text.toString()
        val pesoMeta = Formatos.lerDecimal(textoPesoMeta)

        binding.campoPeso.error = if (peso == null || peso !in 30.0..300.0) getString(R.string.q_erro_peso) else null
        binding.campoAltura.error = if (alturaCm == null || alturaCm !in 100..250) getString(R.string.q_erro_altura) else null
        binding.campoPesoMeta.error =
            if (textoPesoMeta.isNotBlank() && (pesoMeta == null || pesoMeta !in 30.0..300.0)) getString(R.string.q_erro_peso) else null

        if (binding.campoPeso.error != null || binding.campoAltura.error != null || binding.campoPesoMeta.error != null) {
            toast(getString(R.string.q_erro_formulario))
            return null
        }

        val local = radiosLocal.entries.first { it.key.isChecked }.value
        return AvaliacaoEntrada(
            usuarioId = usuarioId,
            objetivo = binding.spnObjetivo.opcaoSelecionada<Objetivo>().api,
            nivel = radiosNivel.entries.first { it.key.isChecked }.value.api,
            diasSemana = diasSemana(),
            minutosSessao = opcoesMinutos[binding.spnMinutos.selectedItemPosition],
            local = local.api,
            possuiEquipamento = local == Local.CASA && binding.swEquipamento.isChecked,
            restricoes = checkRestricoes.filterKeys { it.isChecked }.values.map { it.api },
            peso = peso!!,
            altura = alturaCm!! / 100.0,
            pesoMeta = pesoMeta,
            atividadeDiaria = binding.spnAtividade.opcaoSelecionada<AtividadeDiaria>().api,
            horasSono = horasSono(),
        )
    }

    private fun carregando(ativo: Boolean) {
        binding.progresso.visivel(ativo)
        binding.btnGerarPlano.isEnabled = !ativo
        binding.btnGerarPlano.text = getString(if (ativo) R.string.q_gerando else R.string.q_gerar)
    }
}

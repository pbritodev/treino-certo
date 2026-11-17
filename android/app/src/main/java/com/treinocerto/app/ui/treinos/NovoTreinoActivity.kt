package com.treinocerto.app.ui.treinos

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.treinocerto.app.R
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.data.model.IntensidadeTreino
import com.treinocerto.app.data.model.TipoTreino
import com.treinocerto.app.data.model.TreinoRealizadoEntrada
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.ActivityNovoTreinoBinding
import com.treinocerto.app.ui.common.TelaBase
import com.treinocerto.app.ui.common.configurarOpcoes
import com.treinocerto.app.ui.common.opcaoSelecionada
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.toast
import com.treinocerto.app.util.visivel
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Cadastro de um treino avulso (corrida, natação, futebol...) em POST /api/treinos-realizados.
 * A API estima as calorias gastas. Opcionalmente, abre a agenda do celular (Intent implícita).
 */
class NovoTreinoActivity : TelaBase() {

    private lateinit var binding: ActivityNovoTreinoBinding
    private val repositorio = TreinoRepository()
    private val dataTreino: Calendar = Calendar.getInstance()

    /** Enquanto o usuário não digitar um título, ele acompanha o tipo escolhido no Spinner. */
    private var tituloEditado = false
    private var preenchendoTitulo = false

    private val radiosIntensidade by lazy {
        mapOf(
            binding.rbLeve to IntensidadeTreino.LEVE,
            binding.rbModerada to IntensidadeTreino.MODERADA,
            binding.rbIntensa to IntensidadeTreino.INTENSA,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNovoTreinoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.novo_treino_titulo)

        configurarTipo()
        configurarDuracao()
        binding.edtData.setText(Formatos.dataCurta(Formatos.paraApi(dataTreino)))
        binding.edtData.setOnClickListener { escolherData() }
        binding.btnSalvar.setOnClickListener { salvar() }
    }

    private fun configurarTipo() {
        binding.spnTipo.configurarOpcoes(TipoTreino.avulsos, TipoTreino.CORRIDA)
        binding.spnTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(pai: AdapterView<*>?, view: View?, posicao: Int, id: Long) {
                if (!tituloEditado) {
                    preenchendoTitulo = true
                    binding.edtTitulo.setText(binding.spnTipo.opcaoSelecionada<TipoTreino>().rotulo.substringBefore(" ("))
                    preenchendoTitulo = false
                }
            }
            override fun onNothingSelected(pai: AdapterView<*>?) = Unit
        }
        binding.edtTitulo.doAfterTextChanged { if (!preenchendoTitulo) tituloEditado = true }
    }

    /** Chips de atalho: 30, 45, 60 e 90 minutos. */
    private fun configurarDuracao() {
        mapOf(binding.chip30 to 30, binding.chip45 to 45, binding.chip60 to 60, binding.chip90 to 90)
            .forEach { (chip, minutos) ->
                chip.text = getString(R.string.q_minutos_opcao, minutos)
                chip.setOnClickListener { binding.edtDuracao.setText(String.format(Locale.ROOT, "%d", minutos)) }
            }
        binding.edtDuracao.setText(String.format(Locale.ROOT, "%d", 45))
    }

    private fun escolherData() {
        DatePickerDialog(
            this,
            { _, ano, mes, dia ->
                dataTreino.set(ano, mes, dia)
                binding.edtData.setText(Formatos.dataCurta(Formatos.paraApi(dataTreino)))
            },
            dataTreino.get(Calendar.YEAR),
            dataTreino.get(Calendar.MONTH),
            dataTreino.get(Calendar.DAY_OF_MONTH),
        ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
    }

    private fun salvar() {
        val usuarioId = SessaoUsuario(this).usuarioId ?: return finish()
        val titulo = binding.edtTitulo.text.toString().trim()
        val duracao = binding.edtDuracao.text.toString().toIntOrNull()

        binding.campoTitulo.error = if (titulo.isEmpty() || titulo.length > 40) getString(R.string.novo_treino_erro_nome) else null
        binding.campoDuracao.error = if (duracao == null || duracao !in 5..300) getString(R.string.novo_treino_erro_duracao) else null
        if (binding.campoTitulo.error != null || duracao == null || binding.campoDuracao.error != null) return

        val entrada = TreinoRealizadoEntrada(
            usuarioId = usuarioId,
            avaliacaoId = null,
            dia = titulo,
            foco = binding.edtFoco.text.toString().trim(),
            duracaoMin = duracao,
            data = Formatos.paraApi(dataTreino),
            tipo = binding.spnTipo.opcaoSelecionada<TipoTreino>().api,
            intensidade = radiosIntensidade.entries.first { it.key.isChecked }.value.api,
            observacao = binding.edtObservacao.text.toString().trim(),
        )

        carregando(true)
        lifecycleScope.launch {
            when (val resultado = repositorio.registrarTreino(entrada)) {
                is Resultado.Sucesso -> {
                    val treino = resultado.dado
                    toast(getString(R.string.novo_treino_salvo, treino.kcal ?: 0))
                    if (binding.swAgenda.isChecked) ExibicaoTreino.adicionarNaAgenda(this@NovoTreinoActivity, treino)
                    finish()
                }
                is Resultado.Erro -> {
                    carregando(false)
                    mostrarErro(resultado.mensagem)
                }
            }
        }
    }

    private fun carregando(ativo: Boolean) {
        binding.progresso.visivel(ativo)
        binding.btnSalvar.isEnabled = !ativo
    }
}

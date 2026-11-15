package com.treinocerto.app.ui.diario

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.treinocerto.app.R
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.data.model.Diario
import com.treinocerto.app.data.model.Refeicao
import com.treinocerto.app.data.model.RegistroAlimentar
import com.treinocerto.app.data.model.RegistroAlimentarUpdate
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.ActivityDiarioBinding
import com.treinocerto.app.databinding.DialogEditarRegistroBinding
import com.treinocerto.app.databinding.LayoutMacroBinding
import com.treinocerto.app.ui.alimento.AdicionarAlimentoActivity
import com.treinocerto.app.ui.common.Extras
import com.treinocerto.app.ui.common.configurarOpcoes
import com.treinocerto.app.ui.common.opcaoSelecionada
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.toast
import com.treinocerto.app.util.visivel
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Diário alimentar no estilo "contador de calorias": mostra a meta do dia,
 * o consumido, o restante e os alimentos de cada refeição.
 */
class DiarioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDiarioBinding
    private lateinit var sessao: SessaoUsuario
    private val repositorio = TreinoRepository()
    private val dataSelecionada: Calendar = Calendar.getInstance()

    private val adapter = RefeicaoAdapter(
        aoAdicionar = { refeicao -> abrirAdicionarAlimento(refeicao) },
        aoClicarItem = { registro -> editarRegistro(registro) },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiarioBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.diario_titulo)
        sessao = SessaoUsuario(this)

        binding.rvRefeicoes.layoutManager = LinearLayoutManager(this)
        binding.rvRefeicoes.adapter = adapter

        binding.btnDiaAnterior.setOnClickListener { mudarDia(-1) }
        binding.btnProximoDia.setOnClickListener { mudarDia(+1) }
        binding.txtData.setOnClickListener { escolherData() }
        binding.fabAdicionar.setOnClickListener { abrirAdicionarAlimento(refeicaoSugerida()) }

        binding.macroProteina.txtNomeMacro.setText(R.string.diario_proteina)
        binding.macroCarboidrato.txtNomeMacro.setText(R.string.diario_carboidrato)
        binding.macroGordura.txtNomeMacro.setText(R.string.diario_gordura)
    }

    override fun onResume() {
        super.onResume()
        carregar() // atualiza ao voltar da tela de adicionar alimento
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun mudarDia(dias: Int) {
        dataSelecionada.add(Calendar.DAY_OF_MONTH, dias)
        carregar()
    }

    private fun escolherData() {
        DatePickerDialog(
            this,
            { _, ano, mes, dia ->
                dataSelecionada.set(ano, mes, dia)
                carregar()
            },
            dataSelecionada.get(Calendar.YEAR),
            dataSelecionada.get(Calendar.MONTH),
            dataSelecionada.get(Calendar.DAY_OF_MONTH),
        ).show()
    }

    private fun carregar() {
        val usuarioId = sessao.usuarioId ?: return finish()
        val data = Formatos.paraApi(dataSelecionada)
        binding.txtData.text = if (data == Formatos.hojeApi()) getString(R.string.diario_hoje) else Formatos.dataLonga(data)
        binding.progresso.visivel(true)

        lifecycleScope.launch {
            when (val resultado = repositorio.buscarDiario(usuarioId, data)) {
                is Resultado.Sucesso -> exibir(resultado.dado)
                is Resultado.Erro -> mostrarErro(resultado.mensagem) { carregar() }
            }
            binding.progresso.visivel(false)
        }
    }

    private fun exibir(diario: Diario) {
        val consumido = diario.totais.kcal.roundToInt()
        val excedeu = diario.restante < 0
        val cor = ContextCompat.getColor(this, if (excedeu) R.color.intensidade_intensa else R.color.verde_primario)

        binding.txtRestante.text = abs(diario.restante).toString()
        binding.txtRestante.setTextColor(cor)
        binding.txtRotuloRestante.setText(if (excedeu) R.string.diario_excedido else R.string.diario_restante)
        binding.barraCalorias.max = diario.metaCalorica
        binding.barraCalorias.progress = consumido.coerceAtMost(diario.metaCalorica)
        binding.barraCalorias.progressTintList = ColorStateList.valueOf(cor)
        binding.txtEquacao.text = getString(R.string.diario_equacao, diario.metaCalorica, consumido, diario.restante)
        binding.txtOrigemMeta.setText(
            if (diario.metaDefinidaPorAvaliacao) R.string.diario_meta_avaliacao else R.string.diario_meta_padrao,
        )

        // Proteína tem meta própria; carboidrato e gordura mostram a proporção das calorias do dia.
        exibirMacro(binding.macroProteina, diario.totais.proteina, diario.metaProteina)
        exibirMacro(binding.macroCarboidrato, diario.totais.carboidrato, null, percentual = diario.totais.carboidrato * 4 / diario.metaCalorica)
        exibirMacro(binding.macroGordura, diario.totais.gordura, null, percentual = diario.totais.gordura * 9 / diario.metaCalorica)

        adapter.atualizar(diario.refeicoes)
    }

    private fun exibirMacro(macro: LayoutMacroBinding, gramas: Double, meta: Int?, percentual: Double = 0.0) {
        macro.txtValorMacro.text = if (meta != null) {
            getString(R.string.diario_gramas_meta, Formatos.decimal(gramas), meta)
        } else {
            getString(R.string.diario_gramas, Formatos.decimal(gramas))
        }
        val progresso = if (meta != null && meta > 0) gramas / meta else percentual
        macro.barraMacro.progress = (progresso * 100).roundToInt().coerceIn(0, 100)
    }

    /** Sugere a refeição pelo horário atual (usado pelo botão flutuante). */
    private fun refeicaoSugerida(): String {
        val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hora < 11 -> Refeicao.CAFE_DA_MANHA
            hora < 15 -> Refeicao.ALMOCO
            hora < 18 -> Refeicao.LANCHE
            else -> Refeicao.JANTAR
        }.api
    }

    private fun abrirAdicionarAlimento(refeicao: String) {
        val intent = Intent(this, AdicionarAlimentoActivity::class.java)
            .putExtra(Extras.DATA, Formatos.paraApi(dataSelecionada))
            .putExtra(Extras.REFEICAO, refeicao)
        startActivity(intent)
    }

    /** Diálogo para alterar a quantidade/refeição (PUT) ou excluir (DELETE) um item. */
    private fun editarRegistro(registro: RegistroAlimentar) {
        val dialogo = DialogEditarRegistroBinding.inflate(LayoutInflater.from(this))
        dialogo.edtQuantidade.setText(Formatos.decimal(registro.quantidade))
        dialogo.spnRefeicao.configurarOpcoes(Refeicao.entries, Refeicao.entries.first { it.api == registro.refeicao })

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.diario_editar_titulo, registro.alimento))
            .setView(dialogo.root)
            .setNegativeButton(R.string.excluir) { _, _ -> removerRegistro(registro) }
            .setNeutralButton(R.string.cancelar, null)
            .setPositiveButton(R.string.diario_salvar) { _, _ ->
                val quantidade = Formatos.lerDecimal(dialogo.edtQuantidade.text.toString())
                if (quantidade == null || quantidade !in 1.0..5000.0) {
                    toast(getString(R.string.diario_quantidade_invalida))
                } else {
                    atualizarRegistro(registro.id, RegistroAlimentarUpdate(quantidade, dialogo.spnRefeicao.opcaoSelecionada<Refeicao>().api))
                }
            }
            .show()
    }

    private fun atualizarRegistro(id: Long, update: RegistroAlimentarUpdate) {
        lifecycleScope.launch {
            when (val resultado = repositorio.atualizarRegistro(id, update)) {
                is Resultado.Sucesso -> carregar()
                is Resultado.Erro -> mostrarErro(resultado.mensagem)
            }
        }
    }

    private fun removerRegistro(registro: RegistroAlimentar) {
        lifecycleScope.launch {
            when (val resultado = repositorio.removerRegistro(registro.id)) {
                is Resultado.Sucesso -> {
                    toast(getString(R.string.diario_item_removido))
                    carregar()
                }
                is Resultado.Erro -> mostrarErro(resultado.mensagem)
            }
        }
    }
}

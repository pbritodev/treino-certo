package com.treinocerto.app.ui.alimento

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.treinocerto.app.R
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.data.model.Alimento
import com.treinocerto.app.data.model.CategoriaAlimento
import com.treinocerto.app.data.model.Refeicao
import com.treinocerto.app.data.model.RegistroAlimentarRequest
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.ActivityAdicionarAlimentoBinding
import com.treinocerto.app.databinding.BottomSheetQuantidadeBinding
import com.treinocerto.app.ui.common.Extras
import com.treinocerto.app.ui.common.configurarOpcoes
import com.treinocerto.app.ui.common.opcaoSelecionada
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.toast
import com.treinocerto.app.util.visivel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Busca de alimentos e cálculo das calorias pela quantidade consumida. */
class AdicionarAlimentoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdicionarAlimentoBinding
    private lateinit var sessao: SessaoUsuario
    private val repositorio = TreinoRepository()
    private val adapter = AlimentoAdapter { alimento -> abrirQuantidade(alimento) }

    private lateinit var data: String
    private lateinit var refeicaoInicial: Refeicao
    private var categoriaSelecionada: CategoriaAlimento? = null
    private var buscaAtual: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdicionarAlimentoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        sessao = SessaoUsuario(this)

        data = intent.getStringExtra(Extras.DATA) ?: Formatos.hojeApi()
        refeicaoInicial = Refeicao.entries.firstOrNull { it.api == intent.getStringExtra(Extras.REFEICAO) }
            ?: Refeicao.ALMOCO
        title = getString(R.string.alimento_titulo, refeicaoInicial.rotulo)

        binding.rvAlimentos.layoutManager = LinearLayoutManager(this)
        binding.rvAlimentos.adapter = adapter
        binding.vazio.imgVazio.setImageResource(R.drawable.ic_busca)
        binding.vazio.txtVazio.setText(R.string.alimento_nenhum)

        criarChipsDeCategoria()
        // Pequeno atraso (debounce) para não chamar a API a cada letra digitada.
        binding.edtBusca.doAfterTextChanged { buscar(atraso = 350) }
        buscar()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun criarChipsDeCategoria() {
        val opcoes = listOf<CategoriaAlimento?>(null) + CategoriaAlimento.entries
        opcoes.forEach { categoria ->
            val chip = Chip(this).apply {
                text = categoria?.rotulo ?: getString(R.string.alimento_todas)
                isCheckable = true
                isChecked = categoria == null
                setOnClickListener {
                    categoriaSelecionada = categoria
                    buscar()
                }
            }
            binding.grupoCategorias.addView(chip)
        }
    }

    private fun buscar(atraso: Long = 0) {
        buscaAtual?.cancel()
        buscaAtual = lifecycleScope.launch {
            delay(atraso)
            binding.progresso.visivel(true)
            val busca = binding.edtBusca.text.toString()
            when (val resultado = repositorio.listarAlimentos(busca, categoriaSelecionada?.api)) {
                is Resultado.Sucesso -> {
                    adapter.submitList(resultado.dado)
                    binding.vazio.root.visivel(resultado.dado.isEmpty())
                }
                is Resultado.Erro -> mostrarErro(resultado.mensagem) { buscar() }
            }
            binding.progresso.visivel(false)
        }
    }

    /** BottomSheet onde o usuário informa a quantidade e vê as calorias calculadas em tempo real. */
    private fun abrirQuantidade(alimento: Alimento) {
        val folha = BottomSheetDialog(this)
        val sheet = BottomSheetQuantidadeBinding.inflate(LayoutInflater.from(this))
        folha.setContentView(sheet.root)

        sheet.txtNome.text = alimento.nome
        sheet.txtPor100g.text = getString(
            R.string.alimento_por_100g,
            Formatos.decimal(alimento.kcal), Formatos.decimal(alimento.proteina),
            Formatos.decimal(alimento.carboidrato), Formatos.decimal(alimento.gordura),
        )
        sheet.spnRefeicao.configurarOpcoes(Refeicao.entries, refeicaoInicial)

        fun recalcular() {
            val quantidade = Formatos.lerDecimal(sheet.edtQuantidade.text.toString()) ?: 0.0
            sheet.txtTotalKcal.text = getString(R.string.alimento_total, alimento.kcalPara(quantidade).roundToInt())
            sheet.txtMacros.text = getString(
                R.string.alimento_macros,
                Formatos.decimal(alimento.proteinaPara(quantidade)),
                Formatos.decimal(alimento.carboidratoPara(quantidade)),
                Formatos.decimal(alimento.gorduraPara(quantidade)),
            )
            sheet.btnAdicionar.isEnabled = quantidade in 1.0..5000.0
        }

        sheet.edtQuantidade.doAfterTextChanged { recalcular() }
        mapOf(sheet.chipMeia to 0.5, sheet.chipUma to 1.0, sheet.chipDuas to 2.0).forEach { (chip, fator) ->
            chip.setOnClickListener { sheet.edtQuantidade.setText(Formatos.decimal(alimento.porcaoG * fator)) }
        }
        sheet.edtQuantidade.setText(Formatos.decimal(alimento.porcaoG))

        sheet.btnAdicionar.setOnClickListener {
            val quantidade = Formatos.lerDecimal(sheet.edtQuantidade.text.toString()) ?: return@setOnClickListener
            val refeicao = sheet.spnRefeicao.opcaoSelecionada<Refeicao>()
            salvar(alimento, quantidade, refeicao, folha)
        }
        folha.show()
    }

    private fun salvar(alimento: Alimento, quantidade: Double, refeicao: Refeicao, folha: BottomSheetDialog) {
        val usuarioId = sessao.usuarioId ?: return finish()
        val request = RegistroAlimentarRequest(usuarioId, alimento.id, data, refeicao.api, quantidade)
        lifecycleScope.launch {
            when (val resultado = repositorio.adicionarAoDiario(request)) {
                is Resultado.Sucesso -> {
                    folha.dismiss()
                    toast(getString(R.string.alimento_adicionado, alimento.nome))
                    finish()
                }
                is Resultado.Erro -> mostrarErro(resultado.mensagem)
            }
        }
    }
}

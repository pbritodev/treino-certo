package com.treinocerto.app.ui.alimento

import android.os.Bundle
import android.view.LayoutInflater
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
import com.treinocerto.app.data.model.RegistroAlimentarEntrada
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.ActivityAdicionarAlimentoBinding
import com.treinocerto.app.databinding.BottomSheetQuantidadeBinding
import com.treinocerto.app.ui.common.Extras
import com.treinocerto.app.ui.common.TelaBase
import com.treinocerto.app.ui.common.configurarOpcoes
import com.treinocerto.app.ui.common.opcaoSelecionada
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.toast
import com.treinocerto.app.util.visivel
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Busca de alimentos e cálculo das calorias pela quantidade consumida. */
class AdicionarAlimentoActivity : TelaBase() {

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

    private fun criarChipsDeCategoria() {
        val opcoes = listOf<CategoriaAlimento?>(null) + CategoriaAlimento.entries
        opcoes.forEach { categoria ->
            val chip = (layoutInflater.inflate(R.layout.item_chip_categoria, binding.grupoCategorias, false) as Chip).apply {
                text = categoria?.rotulo ?: getString(R.string.alimento_todas)
                isChecked = categoria == null
                setOnClickListener {
                    categoriaSelecionada = categoria
                    // Rola a barra para deixar a categoria escolhida visível.
                    binding.rolagemCategorias.smoothScrollTo((left - width / 2).coerceAtLeast(0), 0)
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
        val painel = BottomSheetQuantidadeBinding.inflate(LayoutInflater.from(this))
        folha.setContentView(painel.root)

        painel.txtNome.text = alimento.nome
        painel.txtPor100g.text = getString(
            R.string.alimento_por_100g,
            Formatos.decimal(alimento.kcal), Formatos.decimal(alimento.proteina),
            Formatos.decimal(alimento.carboidrato), Formatos.decimal(alimento.gordura),
        )
        painel.spnRefeicao.configurarOpcoes(Refeicao.entries, refeicaoInicial)

        fun recalcular() {
            val quantidade = Formatos.lerDecimal(painel.edtQuantidade.text.toString()) ?: 0.0
            painel.txtTotalKcal.text = getString(R.string.alimento_total, alimento.kcalPara(quantidade).roundToInt())
            painel.txtMacros.text = getString(
                R.string.alimento_macros,
                Formatos.decimal(alimento.proteinaPara(quantidade)),
                Formatos.decimal(alimento.carboidratoPara(quantidade)),
                Formatos.decimal(alimento.gorduraPara(quantidade)),
            )
            painel.btnAdicionar.isEnabled = quantidade in 1.0..5000.0
        }

        painel.edtQuantidade.doAfterTextChanged { recalcular() }
        mapOf(painel.chipMeia to 0.5, painel.chipUma to 1.0, painel.chipDuas to 2.0).forEach { (chip, fator) ->
            chip.setOnClickListener { painel.edtQuantidade.setText(Formatos.decimal(alimento.porcaoG * fator)) }
        }
        painel.edtQuantidade.setText(Formatos.decimal(alimento.porcaoG))

        painel.btnAdicionar.setOnClickListener {
            val quantidade = Formatos.lerDecimal(painel.edtQuantidade.text.toString()) ?: return@setOnClickListener
            val refeicao = painel.spnRefeicao.opcaoSelecionada<Refeicao>()
            salvar(alimento, quantidade, refeicao, folha)
        }
        folha.show()
    }

    private fun salvar(alimento: Alimento, quantidade: Double, refeicao: Refeicao, folha: BottomSheetDialog) {
        val usuarioId = sessao.usuarioId ?: return finish()
        val entrada = RegistroAlimentarEntrada(usuarioId, alimento.id, data, refeicao.api, quantidade)
        lifecycleScope.launch {
            when (val resultado = repositorio.adicionarAoDiario(entrada)) {
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

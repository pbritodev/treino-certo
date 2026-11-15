package com.treinocerto.app.ui.alimento

import android.content.Intent
import android.os.Bundle
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.treinocerto.app.R
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.data.model.AlimentoEntrada
import com.treinocerto.app.data.model.CategoriaAlimento
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.ActivityCriarAlimentoBinding
import com.treinocerto.app.ui.common.Extras
import com.treinocerto.app.ui.common.TelaBase
import com.treinocerto.app.ui.common.configurarOpcoes
import com.treinocerto.app.ui.common.opcaoSelecionada
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.toast
import com.treinocerto.app.util.visivel
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * Cadastro de um alimento próprio (POST /api/alimentos). O usuário digita os valores
 * de uma porção, como na tabela nutricional da embalagem, e o app converte para 100 g.
 * Ao salvar, devolve o id do alimento para a tela de busca já abrir a quantidade.
 */
class CriarAlimentoActivity : TelaBase() {

    private lateinit var binding: ActivityCriarAlimentoBinding
    private val repositorio = TreinoRepository()

    /** Valores digitados para uma porção. */
    private data class ValoresDaPorcao(
        val porcaoG: Double,
        val kcal: Double,
        val proteina: Double,
        val carboidrato: Double,
        val gordura: Double,
    ) {
        /** Regra de três: valor da porção -> valor em 100 g. */
        fun por100g(valor: Double) = valor * 100 / porcaoG
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCriarAlimentoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.criar_alimento_titulo)

        binding.spnCategoria.configurarOpcoes(CategoriaAlimento.entries, CategoriaAlimento.OUTROS)
        intent.getStringExtra(Extras.NOME_ALIMENTO)?.let { binding.edtNome.setText(it) }

        listOf(binding.edtPorcao, binding.edtKcal, binding.edtProteina, binding.edtCarboidrato, binding.edtGordura)
            .forEach { campo -> campo.doAfterTextChanged { atualizarPrevia() } }
        atualizarPrevia()

        binding.btnSalvar.setOnClickListener { salvar() }
    }

    private fun lerNumero(campo: TextInputEditText) = Formatos.lerDecimal(campo.text.toString())

    /** Mostra, enquanto o usuário digita, como o alimento ficará em 100 g. */
    private fun atualizarPrevia() {
        val porcao = lerNumero(binding.edtPorcao)?.takeIf { it > 0 }
        binding.txtPrevia.visivel(porcao != null)
        if (porcao == null) return
        val valores = ValoresDaPorcao(
            porcao,
            lerNumero(binding.edtKcal) ?: 0.0,
            lerNumero(binding.edtProteina) ?: 0.0,
            lerNumero(binding.edtCarboidrato) ?: 0.0,
            lerNumero(binding.edtGordura) ?: 0.0,
        )
        binding.txtPrevia.text = getString(
            R.string.criar_alimento_previa,
            valores.por100g(valores.kcal).roundToInt().toString(),
            Formatos.decimal(valores.por100g(valores.proteina)),
            Formatos.decimal(valores.por100g(valores.carboidrato)),
            Formatos.decimal(valores.por100g(valores.gordura)),
        )
    }

    private fun salvar() {
        val usuarioId = SessaoUsuario(this).usuarioId ?: return finish()
        val entrada = lerFormulario(usuarioId) ?: return

        carregando(true)
        lifecycleScope.launch {
            when (val resultado = repositorio.criarAlimento(entrada)) {
                is Resultado.Sucesso -> {
                    toast(getString(R.string.criar_alimento_criado, resultado.dado.nome))
                    setResult(RESULT_OK, Intent().putExtra(Extras.ALIMENTO_ID, resultado.dado.id))
                    finish()
                }
                is Resultado.Erro -> {
                    carregando(false)
                    mostrarErro(resultado.mensagem)
                }
            }
        }
    }

    /** Valida os campos (mostrando os erros) e monta os dados já convertidos para 100 g. */
    private fun lerFormulario(usuarioId: Long): AlimentoEntrada? {
        val nome = binding.edtNome.text.toString().trim()
        val porcao = lerNumero(binding.edtPorcao)
        binding.campoNome.error = if (nome.length !in 2..60) getString(R.string.criar_alimento_erro_nome) else null
        binding.campoPorcao.error = if (porcao == null || porcao !in 1.0..2000.0) getString(R.string.criar_alimento_erro_porcao) else null

        // Calorias até 9 kcal por grama (gordura pura); macros não podem passar do peso da porção.
        val limite = (porcao ?: 0.0) * 9
        val kcal = validarCampo(binding.campoKcal, binding.edtKcal, limite)
        val proteina = validarCampo(binding.campoProteina, binding.edtProteina, porcao ?: 0.0)
        val carboidrato = validarCampo(binding.campoCarboidrato, binding.edtCarboidrato, porcao ?: 0.0)
        val gordura = validarCampo(binding.campoGordura, binding.edtGordura, porcao ?: 0.0)

        val campos = listOf(binding.campoNome, binding.campoPorcao, binding.campoKcal,
            binding.campoProteina, binding.campoCarboidrato, binding.campoGordura)
        if (campos.any { it.error != null } || porcao == null || kcal == null ||
            proteina == null || carboidrato == null || gordura == null
        ) return null

        if (proteina + carboidrato + gordura > porcao) {
            mostrarErro(getString(R.string.criar_alimento_erro_macros))
            return null
        }

        val valores = ValoresDaPorcao(porcao, kcal, proteina, carboidrato, gordura)
        val descricao = binding.edtPorcaoDescricao.text.toString().trim()
            .ifEmpty { getString(R.string.criar_alimento_porcao_padrao) }
        return AlimentoEntrada(
            usuarioId = usuarioId,
            nome = nome,
            categoria = binding.spnCategoria.opcaoSelecionada<CategoriaAlimento>().api,
            kcal = arredondar(valores.por100g(kcal)),
            proteina = arredondar(valores.por100g(proteina)),
            carboidrato = arredondar(valores.por100g(carboidrato)),
            gordura = arredondar(valores.por100g(gordura)),
            porcaoG = porcao,
            porcaoDescricao = descricao,
        )
    }

    /** Campo numérico obrigatório entre 0 e o máximo; devolve o valor ou null (com erro no campo). */
    private fun validarCampo(campo: TextInputLayout, texto: TextInputEditText, maximo: Double): Double? {
        val valor = lerNumero(texto)
        val valido = valor != null && valor >= 0 && valor <= maximo
        campo.error = if (valido) null else getString(R.string.criar_alimento_erro_valor)
        return if (valido) valor else null
    }

    private fun arredondar(valor: Double) = (valor * 10).roundToInt() / 10.0

    private fun carregando(ativo: Boolean) {
        binding.progresso.visivel(ativo)
        binding.btnSalvar.isEnabled = !ativo
    }
}

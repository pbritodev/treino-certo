package com.treinocerto.app.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.lifecycleScope
import com.treinocerto.app.R
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.ActivityMainBinding
import com.treinocerto.app.databinding.ItemMenuGradeBinding
import com.treinocerto.app.ui.common.TelaBase
import com.treinocerto.app.ui.common.sair
import com.treinocerto.app.ui.diario.DiarioActivity
import com.treinocerto.app.ui.historico.HistoricoActivity
import com.treinocerto.app.ui.perfil.PerfilActivity
import com.treinocerto.app.ui.peso.PesoActivity
import com.treinocerto.app.ui.progresso.ProgressoActivity
import com.treinocerto.app.ui.questionario.QuestionarioActivity
import com.treinocerto.app.ui.sobre.SobreActivity
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.visivel
import kotlin.math.roundToInt
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/** Home: saudação, resumo do dia, menu em grade e botão de tema. Exige usuário logado. */
class MainActivity : TelaBase() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessao: SessaoUsuario
    private val repositorio = TreinoRepository()

    override val mostrarBarraSuperior = false // a Home tem cabeçalho próprio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessao = SessaoUsuario(this)
        if (!sessao.estaLogado) return sair() // sem sessão: volta para o login

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTema.setImageResource(iconeDoTema())
        binding.btnTema.setOnClickListener { alternarTema() }

        configurarMenu(binding.menuNovaAvaliacao, R.drawable.ic_haltere, R.string.menu_nova_avaliacao,
            R.string.menu_nova_avaliacao_desc, QuestionarioActivity::class.java)
        configurarMenu(binding.menuProgresso, R.drawable.ic_grafico, R.string.menu_progresso,
            R.string.menu_progresso_desc, ProgressoActivity::class.java)
        configurarMenu(binding.menuDiario, R.drawable.ic_restaurante, R.string.menu_diario,
            R.string.menu_diario_desc, DiarioActivity::class.java)
        configurarMenu(binding.menuPeso, R.drawable.ic_balanca, R.string.menu_peso,
            R.string.menu_peso_desc, PesoActivity::class.java)
        configurarMenu(binding.menuHistorico, R.drawable.ic_historico, R.string.menu_historico,
            R.string.menu_historico_desc, HistoricoActivity::class.java)
        configurarMenu(binding.menuPerfil, R.drawable.ic_pessoa, R.string.menu_perfil,
            R.string.menu_perfil_desc, PerfilActivity::class.java)
        configurarMenu(binding.menuSobre, R.drawable.ic_info, R.string.menu_sobre,
            R.string.menu_sobre_desc, SobreActivity::class.java)
        binding.cardResumo.setOnClickListener { startActivity(Intent(this, DiarioActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        if (!::binding.isInitialized) return
        binding.txtSaudacao.text = getString(R.string.home_ola, sessao.nome.orEmpty().substringBefore(" "))
        carregarResumo()
    }

    /** Busca, em paralelo, o diário de hoje e as pesagens para montar o card de resumo. */
    private fun carregarResumo() {
        val usuarioId = sessao.usuarioId ?: return
        lifecycleScope.launch {
            val diario = async { repositorio.buscarDiario(usuarioId, Formatos.hojeApi()) }
            val pesagens = async { repositorio.listarPesagens(usuarioId) }

            when (val resultado = diario.await()) {
                is Resultado.Sucesso -> with(resultado.dado) {
                    binding.cardResumo.visivel(true)
                    val consumido = totais.kcal.roundToInt()
                    binding.txtKcalHoje.text = getString(R.string.home_kcal_hoje, consumido, metaCalorica)
                    binding.barraKcalHoje.max = metaCalorica
                    binding.barraKcalHoje.progress = consumido.coerceAtMost(metaCalorica)
                }
                // Sem conexão com a API: esconde o resumo em vez de mostrar dados vazios.
                is Resultado.Erro -> binding.cardResumo.visivel(false)
            }
            val ultimoPeso = (pesagens.await() as? Resultado.Sucesso)?.dado?.lastOrNull()
            binding.txtUltimoPeso.text = ultimoPeso
                ?.let { getString(R.string.home_ultimo_peso, Formatos.decimal(it.peso)) }
                ?: getString(R.string.home_sem_peso)
        }
    }

    private fun configurarMenu(
        atalho: ItemMenuGradeBinding,
        @DrawableRes icone: Int,
        @StringRes titulo: Int,
        @StringRes descricao: Int,
        destino: Class<*>,
    ) {
        atalho.imgIcone.setImageResource(icone)
        atalho.txtTitulo.setText(titulo)
        atalho.txtDescricao.setText(descricao)
        atalho.root.setOnClickListener { startActivity(Intent(this, destino)) }
    }
}

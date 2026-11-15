package com.treinocerto.app.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.treinocerto.app.R
import com.treinocerto.app.data.local.PreferenciasTema
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.ActivityMainBinding
import com.treinocerto.app.databinding.ItemMenuHomeBinding
import com.treinocerto.app.ui.cadastro.CadastroActivity
import com.treinocerto.app.ui.common.Extras
import com.treinocerto.app.ui.diario.DiarioActivity
import com.treinocerto.app.ui.historico.HistoricoActivity
import com.treinocerto.app.ui.peso.PesoActivity
import com.treinocerto.app.ui.questionario.QuestionarioActivity
import com.treinocerto.app.ui.sobre.SobreActivity
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.visivel
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Tela inicial: saudação, resumo do dia, menu de navegação e botão de tema. */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessao: SessaoUsuario
    private lateinit var tema: PreferenciasTema
    private val repositorio = TreinoRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        sessao = SessaoUsuario(this)
        tema = PreferenciasTema(this)

        binding.btnTema.setImageResource(if (tema.estaEscuro()) R.drawable.ic_tema_claro else R.drawable.ic_tema_escuro)
        binding.btnTema.setOnClickListener { tema.alternar() } // a Activity é recriada com o novo tema

        configurarMenu(binding.menuNovaAvaliacao, R.drawable.ic_haltere, R.string.menu_nova_avaliacao,
            R.string.menu_nova_avaliacao_desc) { abrirExigindoPerfil(QuestionarioActivity::class.java) }
        configurarMenu(binding.menuDiario, R.drawable.ic_restaurante, R.string.menu_diario,
            R.string.menu_diario_desc) { abrirExigindoPerfil(DiarioActivity::class.java) }
        configurarMenu(binding.menuPeso, R.drawable.ic_balanca, R.string.menu_peso,
            R.string.menu_peso_desc) { abrirExigindoPerfil(PesoActivity::class.java) }
        configurarMenu(binding.menuHistorico, R.drawable.ic_historico, R.string.menu_historico,
            R.string.menu_historico_desc) { abrirExigindoPerfil(HistoricoActivity::class.java) }
        configurarMenu(binding.menuSobre, R.drawable.ic_info, R.string.menu_sobre,
            R.string.menu_sobre_desc) { startActivity(Intent(this, SobreActivity::class.java)) }
        binding.cardResumo.setOnClickListener { startActivity(Intent(this, DiarioActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        // Atualiza saudação e resumo sempre que o usuário volta para a Home.
        binding.txtSaudacao.text = sessao.nome
            ?.let { getString(R.string.home_ola, it.substringBefore(" ")) }
            ?: getString(R.string.slogan)

        val descricaoPerfil = if (sessao.estaLogado) R.string.menu_perfil_desc else R.string.menu_perfil_desc_novo
        configurarMenu(binding.menuPerfil, R.drawable.ic_pessoa, R.string.menu_perfil, descricaoPerfil) {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
        carregarResumo()
    }

    /** Busca, em paralelo, o diário de hoje e as pesagens para montar o card de resumo. */
    private fun carregarResumo() {
        val usuarioId = sessao.usuarioId
        binding.cardResumo.visivel(usuarioId != null)
        if (usuarioId == null) return

        lifecycleScope.launch {
            val diario = async { repositorio.buscarDiario(usuarioId, Formatos.hojeApi()) }
            val pesagens = async { repositorio.listarPesagens(usuarioId) }

            when (val resultado = diario.await()) {
                is Resultado.Sucesso -> with(resultado.dado) {
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

    /** Telas que dependem de um usuário: sem perfil, abre o cadastro primeiro. */
    private fun abrirExigindoPerfil(destino: Class<*>) {
        if (sessao.estaLogado) {
            startActivity(Intent(this, destino))
            return
        }
        val intent = Intent(this, CadastroActivity::class.java)
            .putExtra(Extras.ABRIR_QUESTIONARIO_APOS_CADASTRO, destino == QuestionarioActivity::class.java)
        startActivity(intent)
    }

    private fun configurarMenu(
        item: ItemMenuHomeBinding,
        @DrawableRes icone: Int,
        @StringRes titulo: Int,
        @StringRes descricao: Int,
        aoClicar: () -> Unit,
    ) {
        item.imgIcone.setImageResource(icone)
        item.txtTitulo.setText(titulo)
        item.txtDescricao.setText(descricao)
        item.root.setOnClickListener { aoClicar() }
    }
}

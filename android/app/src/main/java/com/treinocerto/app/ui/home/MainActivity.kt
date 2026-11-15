package com.treinocerto.app.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import com.treinocerto.app.R
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.databinding.ActivityMainBinding
import com.treinocerto.app.databinding.ItemMenuHomeBinding
import com.treinocerto.app.ui.cadastro.CadastroActivity
import com.treinocerto.app.ui.common.Extras
import com.treinocerto.app.ui.sobre.SobreActivity
import com.treinocerto.app.util.toast

/** Tela inicial: saudação e menu de navegação para as demais telas. */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessao: SessaoUsuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        sessao = SessaoUsuario(this)

        configurarMenu(binding.menuNovaAvaliacao, R.drawable.ic_haltere, R.string.menu_nova_avaliacao,
            R.string.menu_nova_avaliacao_desc) { abrirNovaAvaliacao() }
        configurarMenu(binding.menuHistorico, R.drawable.ic_historico, R.string.menu_historico,
            R.string.menu_historico_desc) { abrirHistorico() }
        configurarMenu(binding.menuSobre, R.drawable.ic_info, R.string.menu_sobre,
            R.string.menu_sobre_desc) { startActivity(Intent(this, SobreActivity::class.java)) }
    }

    override fun onResume() {
        super.onResume()
        // Atualiza a saudação sempre que o usuário volta para a Home (ex.: após o cadastro).
        binding.txtSaudacao.text = sessao.nome
            ?.let { getString(R.string.home_ola, it.substringBefore(" ")) }
            ?: getString(R.string.slogan)

        val descricaoPerfil = if (sessao.estaLogado) R.string.menu_perfil_desc else R.string.menu_perfil_desc_novo
        configurarMenu(binding.menuPerfil, R.drawable.ic_pessoa, R.string.menu_perfil, descricaoPerfil) {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
    }

    private fun abrirNovaAvaliacao() {
        if (!sessao.estaLogado) {
            // Sem perfil: primeiro cadastra e, ao salvar, segue direto para o questionário.
            val intent = Intent(this, CadastroActivity::class.java)
                .putExtra(Extras.ABRIR_QUESTIONARIO_APOS_CADASTRO, true)
            startActivity(intent)
            return
        }
        toast("Em breve: questionário")
    }

    private fun abrirHistorico() {
        if (!sessao.estaLogado) {
            toast(getString(R.string.menu_perfil_desc_novo))
            return
        }
        toast("Em breve: histórico")
    }

    private fun configurarMenu(
        item: ItemMenuHomeBinding,
        @DrawableRes icone: Int,
        titulo: Int,
        descricao: Int,
        aoClicar: () -> Unit,
    ) {
        item.imgIcone.setImageResource(icone)
        item.txtTitulo.setText(titulo)
        item.txtDescricao.setText(descricao)
        item.root.setOnClickListener { aoClicar() }
    }
}

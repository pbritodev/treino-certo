package com.treinocerto.app.ui.perfil

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.treinocerto.app.R
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.data.model.Sexo
import com.treinocerto.app.data.model.Usuario
import com.treinocerto.app.data.model.rotuloDe
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.ActivityPerfilBinding
import com.treinocerto.app.databinding.ItemMenuHomeBinding
import com.treinocerto.app.ui.cadastro.CadastroActivity
import com.treinocerto.app.ui.common.iniciais
import com.treinocerto.app.ui.common.sair
import com.treinocerto.app.ui.progresso.ProgressoActivity
import com.treinocerto.app.util.Formatos
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.visivel
import kotlinx.coroutines.launch

/** "Meu perfil": dados da conta e atalhos para editar, trocar senha, progresso e sair. */
class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var sessao: SessaoUsuario
    private val repositorio = TreinoRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.perfil_titulo)
        sessao = SessaoUsuario(this)

        binding.linhaIdade.txtRotulo.setText(R.string.perfil_idade)
        binding.linhaSexo.txtRotulo.setText(R.string.perfil_sexo)
        binding.linhaMembroDesde.txtRotulo.setText(R.string.perfil_membro_desde)

        configurarAtalho(binding.menuProgresso, R.drawable.ic_grafico, R.string.perfil_progresso, R.string.menu_progresso_desc) {
            startActivity(Intent(this, ProgressoActivity::class.java))
        }
        configurarAtalho(binding.menuEditar, R.drawable.ic_editar, R.string.perfil_editar, R.string.perfil_editar_desc) {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
        configurarAtalho(binding.menuTrocarSenha, R.drawable.ic_cadeado, R.string.perfil_trocar_senha, R.string.perfil_senha_desc) {
            startActivity(Intent(this, TrocarSenhaActivity::class.java))
        }
        binding.btnSair.setOnClickListener { confirmarSaida() }
    }

    override fun onResume() {
        super.onResume()
        carregar() // atualiza após editar os dados
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun carregar() {
        val id = sessao.usuarioId ?: return sair()
        lifecycleScope.launch {
            when (val resultado = repositorio.buscarUsuario(id)) {
                is Resultado.Sucesso -> exibir(resultado.dado)
                is Resultado.Erro -> mostrarErro(resultado.mensagem) { carregar() }
            }
            binding.progresso.visivel(false)
        }
    }

    private fun exibir(usuario: Usuario) {
        binding.txtAvatar.text = iniciais(usuario.nome)
        binding.txtNome.text = usuario.nome
        binding.txtEmail.text = usuario.email
        binding.linhaIdade.txtValor.text = getString(R.string.perfil_idade_valor, usuario.idade)
        binding.linhaSexo.txtValor.text = rotuloDe<Sexo>(usuario.sexo)
        binding.linhaMembroDesde.txtValor.text = usuario.criadoEm?.let { Formatos.dataCurta(it.take(10)) } ?: "—"
        sessao.salvar(requireNotNull(usuario.id), usuario.nome) // mantém o nome da saudação atualizado
    }

    private fun confirmarSaida() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.perfil_sair)
            .setMessage(R.string.perfil_sair_confirmar)
            .setIcon(R.drawable.ic_sair)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.perfil_sair) { _, _ -> sair() }
            .show()
    }

    private fun configurarAtalho(item: ItemMenuHomeBinding, icone: Int, titulo: Int, descricao: Int, aoClicar: () -> Unit) {
        item.imgIcone.setImageResource(icone)
        item.txtTitulo.setText(titulo)
        item.txtDescricao.setText(descricao)
        item.root.setOnClickListener { aoClicar() }
    }
}

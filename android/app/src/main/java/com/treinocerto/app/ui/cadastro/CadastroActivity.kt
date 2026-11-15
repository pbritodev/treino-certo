package com.treinocerto.app.ui.cadastro

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.treinocerto.app.R
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.data.model.Sexo
import com.treinocerto.app.data.model.Usuario
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.ActivityCadastroBinding
import com.treinocerto.app.ui.common.Extras
import com.treinocerto.app.ui.questionario.QuestionarioActivity
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.toast
import com.treinocerto.app.util.visivel
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Cadastro (POST) e edição (PUT) do perfil do usuário.
 * Se já existe um usuário na sessão, os dados são carregados da API (GET).
 */
class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private lateinit var sessao: SessaoUsuario
    private val repositorio = TreinoRepository()

    private val radiosPorSexo by lazy {
        mapOf(
            Sexo.MASCULINO to binding.rbMasculino,
            Sexo.FEMININO to binding.rbFeminino,
            Sexo.OUTRO to binding.rbOutro,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.menu_perfil)
        sessao = SessaoUsuario(this)

        binding.btnSalvar.setOnClickListener { salvar() }
        binding.btnTrocarUsuario.setOnClickListener { trocarUsuario() }

        sessao.usuarioId?.let { carregarUsuario(it) }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun carregarUsuario(id: Long) {
        binding.btnTrocarUsuario.visivel(true)
        carregando(true)
        lifecycleScope.launch {
            when (val resultado = repositorio.buscarUsuario(id)) {
                is Resultado.Sucesso -> preencher(resultado.dado)
                is Resultado.Erro -> mostrarErro(resultado.mensagem) { carregarUsuario(id) }
            }
            carregando(false)
        }
    }

    private fun preencher(usuario: Usuario) {
        binding.edtNome.setText(usuario.nome)
        binding.edtEmail.setText(usuario.email)
        binding.edtIdade.setText(String.format(Locale.ROOT, "%d", usuario.idade))
        Sexo.entries.firstOrNull { it.api == usuario.sexo }?.let { radiosPorSexo[it]?.isChecked = true }
    }

    private fun salvar() {
        val usuario = lerFormulario() ?: return
        carregando(true)
        lifecycleScope.launch {
            val id = sessao.usuarioId
            val resultado = if (id == null) repositorio.criarUsuario(usuario) else repositorio.atualizarUsuario(id, usuario)
            carregando(false)
            when (resultado) {
                is Resultado.Sucesso -> aoSalvar(resultado.dado)
                is Resultado.Erro -> mostrarErro(resultado.mensagem)
            }
        }
    }

    private fun aoSalvar(usuario: Usuario) {
        sessao.salvar(requireNotNull(usuario.id), usuario.nome)
        toast(getString(R.string.cadastro_sucesso))
        if (intent.getBooleanExtra(Extras.ABRIR_QUESTIONARIO_APOS_CADASTRO, false)) {
            startActivity(Intent(this, QuestionarioActivity::class.java))
        }
        finish()
    }

    /** Valida os campos e devolve o usuário, ou null mostrando os erros na tela. */
    private fun lerFormulario(): Usuario? {
        val nome = binding.edtNome.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val idade = binding.edtIdade.text.toString().toIntOrNull()
        val sexo = radiosPorSexo.entries.firstOrNull { it.value.isChecked }?.key

        binding.campoNome.error = if (nome.length < 2) getString(R.string.cadastro_erro_nome) else null
        binding.campoEmail.error =
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) getString(R.string.cadastro_erro_email) else null
        binding.campoIdade.error = if (idade == null || idade !in 12..100) getString(R.string.cadastro_erro_idade) else null
        if (sexo == null) toast(getString(R.string.cadastro_erro_sexo))

        val valido = binding.campoNome.error == null && binding.campoEmail.error == null &&
            binding.campoIdade.error == null && sexo != null
        if (!valido) return null
        return Usuario(nome = nome, email = email, idade = idade!!, sexo = sexo!!.api)
    }

    private fun trocarUsuario() {
        sessao.limpar()
        toast(getString(R.string.menu_perfil_desc_novo))
        finish()
    }

    private fun carregando(ativo: Boolean) {
        binding.progresso.visivel(ativo)
        binding.btnSalvar.isEnabled = !ativo
    }
}

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
import com.treinocerto.app.ui.home.MainActivity
import com.treinocerto.app.ui.questionario.QuestionarioActivity
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.toast
import com.treinocerto.app.util.visivel
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Dois modos na mesma tela:
 * - "Cadastre-se" (sem sessão): cria a conta com senha (POST) e já abre o questionário.
 * - "Editar dados" (com sessão): carrega (GET) e atualiza (PUT) os dados, sem mexer na senha.
 */
class CadastroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroBinding
    private lateinit var sessao: SessaoUsuario
    private val repositorio = TreinoRepository()
    private val modoEdicao get() = sessao.estaLogado

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
        sessao = SessaoUsuario(this)

        binding.btnSalvar.setOnClickListener { salvar() }
        binding.btnJaTenhoConta.setOnClickListener { finish() } // volta para o Login

        if (modoEdicao) configurarEdicao() else title = getString(R.string.login_cadastre_se)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun configurarEdicao() {
        title = getString(R.string.cadastro_editar_titulo)
        binding.txtTitulo.setText(R.string.cadastro_editar_titulo)
        binding.txtSubtitulo.setText(R.string.cadastro_subtitulo)
        binding.btnSalvar.setText(R.string.cadastro_salvar)
        binding.grupoSenha.visivel(false)
        binding.btnJaTenhoConta.visivel(false)
        sessao.usuarioId?.let { carregarUsuario(it) }
    }

    private fun carregarUsuario(id: Long) {
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
        val eraCadastro = !modoEdicao
        sessao.salvar(requireNotNull(usuario.id), usuario.nome)
        if (eraCadastro) {
            // Conta nova: Home vira a base da pilha e o questionário abre por cima dela.
            toast(getString(R.string.cadastro_conta_criada))
            val home = Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivities(arrayOf(home, Intent(this, QuestionarioActivity::class.java)))
        } else {
            toast(getString(R.string.cadastro_sucesso))
        }
        finish()
    }

    /** Valida os campos e devolve o usuário, ou null mostrando os erros na tela. */
    private fun lerFormulario(): Usuario? {
        val nome = binding.edtNome.text.toString().trim()
        val email = binding.edtEmail.text.toString().trim()
        val idade = binding.edtIdade.text.toString().toIntOrNull()
        val sexo = radiosPorSexo.entries.firstOrNull { it.value.isChecked }?.key
        val senha = binding.edtSenha.text.toString()
        val confirmacao = binding.edtConfirmarSenha.text.toString()

        binding.campoNome.error = if (nome.length < 2) getString(R.string.cadastro_erro_nome) else null
        binding.campoEmail.error =
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) getString(R.string.cadastro_erro_email) else null
        binding.campoIdade.error = if (idade == null || idade !in 12..100) getString(R.string.cadastro_erro_idade) else null
        if (!modoEdicao) {
            binding.campoSenha.error = if (senha.length < 6) getString(R.string.cadastro_erro_senha) else null
            binding.campoConfirmarSenha.error =
                if (confirmacao != senha) getString(R.string.cadastro_erro_confirmacao) else null
        }
        if (sexo == null) toast(getString(R.string.cadastro_erro_sexo))

        val campos = listOf(binding.campoNome, binding.campoEmail, binding.campoIdade, binding.campoSenha, binding.campoConfirmarSenha)
        if (campos.any { it.error != null } || sexo == null) return null

        return Usuario(
            nome = nome,
            email = email,
            idade = idade!!,
            sexo = sexo.api,
            senha = if (modoEdicao) null else senha,
        )
    }

    private fun carregando(ativo: Boolean) {
        binding.progresso.visivel(ativo)
        binding.btnSalvar.isEnabled = !ativo
    }
}

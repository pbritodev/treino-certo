package com.treinocerto.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.lifecycleScope
import com.treinocerto.app.R
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.ActivityLoginBinding
import com.treinocerto.app.ui.cadastro.CadastroActivity
import com.treinocerto.app.ui.common.TelaBase
import com.treinocerto.app.ui.common.irParaHome
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.visivel
import kotlinx.coroutines.launch

/** Tela inicial do app: login com e-mail e senha (POST /api/auth/login). */
class LoginActivity : TelaBase() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessao: SessaoUsuario
    private val repositorio = TreinoRepository()

    override val mostrarBarraSuperior = false // o Login tem cabeçalho próprio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessao = SessaoUsuario(this)
        // Já está logado: pula direto para a Home.
        if (sessao.estaLogado) return irParaHome()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTema.setImageResource(iconeDoTema())
        binding.btnTema.setOnClickListener { alternarTema() }

        binding.btnEntrar.setOnClickListener { entrar() }
        binding.edtSenha.setOnEditorActionListener { _, acao, _ ->
            (acao == EditorInfo.IME_ACTION_DONE).also { if (it) entrar() }
        }
        binding.btnCadastrese.setOnClickListener {
            startActivity(Intent(this, CadastroActivity::class.java))
        }
    }

    private fun entrar() {
        val email = binding.edtEmail.text.toString().trim()
        val senha = binding.edtSenha.text.toString()
        binding.campoEmail.error = if (email.isEmpty()) getString(R.string.login_erro_campos) else null
        binding.campoSenha.error = if (senha.isEmpty()) getString(R.string.login_erro_campos) else null
        if (email.isEmpty() || senha.isEmpty()) return

        carregando(true)
        lifecycleScope.launch {
            when (val resultado = repositorio.login(email, senha)) {
                is Resultado.Sucesso -> {
                    val usuario = resultado.dado
                    sessao.salvar(requireNotNull(usuario.id), usuario.nome)
                    irParaHome()
                }
                is Resultado.Erro -> {
                    carregando(false)
                    binding.campoSenha.error = resultado.mensagem
                    mostrarErro(resultado.mensagem)
                }
            }
        }
    }

    private fun carregando(ativo: Boolean) {
        binding.progresso.visivel(ativo)
        binding.btnEntrar.isEnabled = !ativo
    }
}

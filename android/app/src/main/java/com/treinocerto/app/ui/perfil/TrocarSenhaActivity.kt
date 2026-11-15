package com.treinocerto.app.ui.perfil

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.treinocerto.app.R
import com.treinocerto.app.data.local.SessaoUsuario
import com.treinocerto.app.data.repository.Resultado
import com.treinocerto.app.data.repository.TreinoRepository
import com.treinocerto.app.databinding.ActivityTrocarSenhaBinding
import com.treinocerto.app.ui.common.TelaBase
import com.treinocerto.app.util.mostrarErro
import com.treinocerto.app.util.toast
import com.treinocerto.app.util.visivel
import kotlinx.coroutines.launch

/** Troca de senha (PUT /api/usuarios/{id}/senha): exige a senha atual. */
class TrocarSenhaActivity : TelaBase() {

    private lateinit var binding: ActivityTrocarSenhaBinding
    private val repositorio = TreinoRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrocarSenhaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        title = getString(R.string.senha_titulo)

        binding.btnSalvar.setOnClickListener { salvar() }
    }

    private fun salvar() {
        val usuarioId = SessaoUsuario(this).usuarioId ?: return finish()
        val atual = binding.edtSenhaAtual.text.toString()
        val nova = binding.edtNovaSenha.text.toString()
        val confirmacao = binding.edtConfirmarSenha.text.toString()

        binding.campoSenhaAtual.error = if (atual.isEmpty()) getString(R.string.login_erro_campos) else null
        binding.campoNovaSenha.error = if (nova.length < 6) getString(R.string.cadastro_erro_senha) else null
        binding.campoConfirmarSenha.error = if (confirmacao != nova) getString(R.string.cadastro_erro_confirmacao) else null
        if (listOf(binding.campoSenhaAtual, binding.campoNovaSenha, binding.campoConfirmarSenha).any { it.error != null }) return

        carregando(true)
        lifecycleScope.launch {
            when (val resultado = repositorio.trocarSenha(usuarioId, atual, nova)) {
                is Resultado.Sucesso -> {
                    toast(getString(R.string.senha_alterada))
                    finish()
                }
                is Resultado.Erro -> {
                    carregando(false)
                    binding.campoSenhaAtual.error = resultado.mensagem
                    mostrarErro(resultado.mensagem)
                }
            }
        }
    }

    private fun carregando(ativo: Boolean) {
        binding.progresso.visivel(ativo)
        binding.btnSalvar.isEnabled = !ativo
    }
}

package com.treinocerto.app.data.repository

import com.google.gson.Gson
import com.treinocerto.app.data.model.Avaliacao
import com.treinocerto.app.data.model.AvaliacaoRequest
import com.treinocerto.app.data.model.AvaliacaoUpdate
import com.treinocerto.app.data.model.ErroApi
import com.treinocerto.app.data.model.LoginRequest
import com.treinocerto.app.data.model.TreinoRealizadoRequest
import com.treinocerto.app.data.model.TrocaSenhaRequest
import com.treinocerto.app.data.model.PesagemRequest
import com.treinocerto.app.data.model.RegistroAlimentarRequest
import com.treinocerto.app.data.model.RegistroAlimentarUpdate
import com.treinocerto.app.data.model.Usuario
import com.treinocerto.app.data.remote.RetrofitClient
import com.treinocerto.app.data.remote.TreinoApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException

/**
 * Único ponto de acesso aos dados remotos. As Activities não conhecem Retrofit:
 * recebem sempre um [Resultado] já tratado.
 */
class TreinoRepository(private val api: TreinoApi = RetrofitClient.api) {

    suspend fun login(email: String, senha: String) = executar { api.login(LoginRequest(email, senha)) }

    suspend fun trocarSenha(id: Long, senhaAtual: String, novaSenha: String): Resultado<Unit> =
        executar { api.trocarSenha(id, TrocaSenhaRequest(senhaAtual, novaSenha)) }

    suspend fun criarUsuario(usuario: Usuario) = executar { api.criarUsuario(usuario) }

    suspend fun buscarUsuario(id: Long) = executar { api.buscarUsuario(id) }

    suspend fun atualizarUsuario(id: Long, usuario: Usuario) = executar { api.atualizarUsuario(id, usuario) }

    suspend fun listarAvaliacoes(usuarioId: Long) = executar { api.listarAvaliacoes(usuarioId) }

    suspend fun criarAvaliacao(request: AvaliacaoRequest) = executar { api.criarAvaliacao(request) }

    suspend fun buscarAvaliacao(id: Long) = executar { api.buscarAvaliacao(id) }

    suspend fun atualizarAvaliacao(id: Long, update: AvaliacaoUpdate): Resultado<Avaliacao> =
        executar { api.atualizarAvaliacao(id, update) }

    suspend fun removerAvaliacao(id: Long): Resultado<Unit> = executar { api.removerAvaliacao(id) }

    suspend fun listarAlimentos(busca: String? = null, categoria: String? = null) =
        executar { api.listarAlimentos(busca?.takeIf { it.isNotBlank() }, categoria) }

    suspend fun buscarDiario(usuarioId: Long, data: String) = executar { api.buscarDiario(usuarioId, data) }

    suspend fun adicionarAoDiario(request: RegistroAlimentarRequest) = executar { api.criarRegistro(request) }

    suspend fun atualizarRegistro(id: Long, update: RegistroAlimentarUpdate) =
        executar { api.atualizarRegistro(id, update) }

    suspend fun removerRegistro(id: Long): Resultado<Unit> = executar { api.removerRegistro(id) }

    suspend fun listarPesagens(usuarioId: Long) = executar { api.listarPesagens(usuarioId) }

    suspend fun registrarPesagem(request: PesagemRequest) = executar { api.criarPesagem(request) }

    suspend fun removerPesagem(id: Long): Resultado<Unit> = executar { api.removerPesagem(id) }

    suspend fun registrarTreino(request: TreinoRealizadoRequest) = executar { api.registrarTreino(request) }

    suspend fun removerTreino(id: Long): Resultado<Unit> = executar { api.removerTreino(id) }

    suspend fun buscarProgresso(usuarioId: Long) = executar { api.buscarProgresso(usuarioId) }

    /** Executa a chamada em background e converte resposta/exceção em [Resultado]. */
    private suspend fun <T> executar(chamada: suspend () -> Response<T>): Resultado<T> =
        withContext(Dispatchers.IO) {
            try {
                val resposta = chamada()
                if (resposta.isSuccessful) {
                    @Suppress("UNCHECKED_CAST")
                    Resultado.Sucesso(resposta.body() ?: Unit as T)
                } else {
                    Resultado.Erro(mensagemDeErro(resposta))
                }
            } catch (e: IOException) {
                Resultado.Erro("Não foi possível conectar ao servidor. Verifique se a API está rodando.")
            } catch (e: Exception) {
                Resultado.Erro("Erro inesperado: ${e.message}")
            }
        }

    private fun mensagemDeErro(resposta: Response<*>): String {
        val corpo = resposta.errorBody()?.string()
        val erro = runCatching { Gson().fromJson(corpo, ErroApi::class.java) }.getOrNull()
        val detalhes = erro?.detalhes?.joinToString("\n")
        return when {
            !detalhes.isNullOrBlank() -> "${erro.erro}:\n$detalhes"
            !erro?.erro.isNullOrBlank() -> erro!!.erro!!
            else -> "Erro ${resposta.code()} ao comunicar com o servidor"
        }
    }
}

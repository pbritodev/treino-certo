package com.treinocerto.app.data.remote

import com.treinocerto.app.data.model.Alimento
import com.treinocerto.app.data.model.Avaliacao
import com.treinocerto.app.data.model.AvaliacaoEntrada
import com.treinocerto.app.data.model.AvaliacaoAlteracao
import com.treinocerto.app.data.model.Diario
import com.treinocerto.app.data.model.LoginEntrada
import com.treinocerto.app.data.model.Pesagem
import com.treinocerto.app.data.model.PesagemEntrada
import com.treinocerto.app.data.model.Progresso
import com.treinocerto.app.data.model.RegistroAlimentar
import com.treinocerto.app.data.model.RegistroAlimentarEntrada
import com.treinocerto.app.data.model.RegistroAlimentarAlteracao
import com.treinocerto.app.data.model.TreinoRealizado
import com.treinocerto.app.data.model.TreinoRealizadoEntrada
import com.treinocerto.app.data.model.TrocaSenhaEntrada
import com.treinocerto.app.data.model.Usuario
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints da TreinoCerto API (documentados no Swagger em /docs). */
interface TreinoApi {

    // ---------- Autenticação e usuários ----------

    @POST("api/auth/login")
    suspend fun login(@Body entrada: LoginEntrada): Response<Usuario>

    @PUT("api/usuarios/{id}/senha")
    suspend fun trocarSenha(@Path("id") id: Long, @Body entrada: TrocaSenhaEntrada): Response<Unit>

    @POST("api/usuarios")
    suspend fun criarUsuario(@Body usuario: Usuario): Response<Usuario>

    @GET("api/usuarios/{id}")
    suspend fun buscarUsuario(@Path("id") id: Long): Response<Usuario>

    @PUT("api/usuarios/{id}")
    suspend fun atualizarUsuario(@Path("id") id: Long, @Body usuario: Usuario): Response<Usuario>

    @GET("api/usuarios/{id}/avaliacoes")
    suspend fun listarAvaliacoes(@Path("id") usuarioId: Long): Response<List<Avaliacao>>

    @POST("api/avaliacoes")
    suspend fun criarAvaliacao(@Body entrada: AvaliacaoEntrada): Response<Avaliacao>

    @GET("api/avaliacoes/{id}")
    suspend fun buscarAvaliacao(@Path("id") id: Long): Response<Avaliacao>

    @PATCH("api/avaliacoes/{id}")
    suspend fun atualizarAvaliacao(@Path("id") id: Long, @Body alteracao: AvaliacaoAlteracao): Response<Avaliacao>

    @DELETE("api/avaliacoes/{id}")
    suspend fun removerAvaliacao(@Path("id") id: Long): Response<Unit>

    // ---------- Alimentação ----------

    @GET("api/alimentos")
    suspend fun listarAlimentos(
        @Query("busca") busca: String?,
        @Query("categoria") categoria: String?,
    ): Response<List<Alimento>>

    @GET("api/usuarios/{id}/diario")
    suspend fun buscarDiario(@Path("id") usuarioId: Long, @Query("data") data: String): Response<Diario>

    @POST("api/registros-alimentares")
    suspend fun criarRegistro(@Body entrada: RegistroAlimentarEntrada): Response<RegistroAlimentar>

    @PUT("api/registros-alimentares/{id}")
    suspend fun atualizarRegistro(@Path("id") id: Long, @Body alteracao: RegistroAlimentarAlteracao): Response<RegistroAlimentar>

    @DELETE("api/registros-alimentares/{id}")
    suspend fun removerRegistro(@Path("id") id: Long): Response<Unit>

    // ---------- Pesagens ----------

    @GET("api/usuarios/{id}/pesagens")
    suspend fun listarPesagens(@Path("id") usuarioId: Long): Response<List<Pesagem>>

    @POST("api/pesagens")
    suspend fun criarPesagem(@Body entrada: PesagemEntrada): Response<Pesagem>

    @DELETE("api/pesagens/{id}")
    suspend fun removerPesagem(@Path("id") id: Long): Response<Unit>

    // ---------- Treinos realizados e progresso ----------

    @POST("api/treinos-realizados")
    suspend fun registrarTreino(@Body entrada: TreinoRealizadoEntrada): Response<TreinoRealizado>

    @DELETE("api/treinos-realizados/{id}")
    suspend fun removerTreino(@Path("id") id: Long): Response<Unit>

    @GET("api/usuarios/{id}/progresso")
    suspend fun buscarProgresso(@Path("id") usuarioId: Long): Response<Progresso>
}

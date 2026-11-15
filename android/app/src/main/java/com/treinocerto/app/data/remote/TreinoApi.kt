package com.treinocerto.app.data.remote

import com.treinocerto.app.data.model.Avaliacao
import com.treinocerto.app.data.model.AvaliacaoRequest
import com.treinocerto.app.data.model.AvaliacaoUpdate
import com.treinocerto.app.data.model.Alimento
import com.treinocerto.app.data.model.Diario
import com.treinocerto.app.data.model.Pesagem
import com.treinocerto.app.data.model.PesagemRequest
import com.treinocerto.app.data.model.RegistroAlimentar
import com.treinocerto.app.data.model.RegistroAlimentarRequest
import com.treinocerto.app.data.model.RegistroAlimentarUpdate
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

    @POST("api/usuarios")
    suspend fun criarUsuario(@Body usuario: Usuario): Response<Usuario>

    @GET("api/usuarios/{id}")
    suspend fun buscarUsuario(@Path("id") id: Long): Response<Usuario>

    @PUT("api/usuarios/{id}")
    suspend fun atualizarUsuario(@Path("id") id: Long, @Body usuario: Usuario): Response<Usuario>

    @GET("api/usuarios/{id}/avaliacoes")
    suspend fun listarAvaliacoes(@Path("id") usuarioId: Long): Response<List<Avaliacao>>

    @POST("api/avaliacoes")
    suspend fun criarAvaliacao(@Body request: AvaliacaoRequest): Response<Avaliacao>

    @GET("api/avaliacoes/{id}")
    suspend fun buscarAvaliacao(@Path("id") id: Long): Response<Avaliacao>

    @PATCH("api/avaliacoes/{id}")
    suspend fun atualizarAvaliacao(@Path("id") id: Long, @Body update: AvaliacaoUpdate): Response<Avaliacao>

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
    suspend fun criarRegistro(@Body request: RegistroAlimentarRequest): Response<RegistroAlimentar>

    @PUT("api/registros-alimentares/{id}")
    suspend fun atualizarRegistro(@Path("id") id: Long, @Body update: RegistroAlimentarUpdate): Response<RegistroAlimentar>

    @DELETE("api/registros-alimentares/{id}")
    suspend fun removerRegistro(@Path("id") id: Long): Response<Unit>

    // ---------- Pesagens ----------

    @GET("api/usuarios/{id}/pesagens")
    suspend fun listarPesagens(@Path("id") usuarioId: Long): Response<List<Pesagem>>

    @POST("api/pesagens")
    suspend fun criarPesagem(@Body request: PesagemRequest): Response<Pesagem>

    @DELETE("api/pesagens/{id}")
    suspend fun removerPesagem(@Path("id") id: Long): Response<Unit>
}

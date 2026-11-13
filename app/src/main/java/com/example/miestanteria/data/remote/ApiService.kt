package com.example.miestanteria.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints del servidor Mi Estanteria (seccion 10.2 del documento). */
interface ApiService {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("usuarios/me")
    suspend fun me(): UsuarioDto

    @GET("libros/buscar")
    suspend fun buscarLibros(
        @Query("q") q: String,
        @Query("tipo") tipo: String? = null,
        @Query("pagina") pagina: Int = 0
    ): List<LibroDto>

    @GET("libros/{apiId}")
    suspend fun detalleLibro(@Path("apiId") apiId: String): LibroDto
}

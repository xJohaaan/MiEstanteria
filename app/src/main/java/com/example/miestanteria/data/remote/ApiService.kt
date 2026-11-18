package com.example.miestanteria.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints del servidor Mi Estanteria (seccion 10.2 del documento). */
interface ApiService {

    // ---- Autenticacion ----
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("usuarios/me")
    suspend fun me(): UsuarioDto

    // ---- Libros (Google Books / Open Library) ----
    @GET("libros/buscar")
    suspend fun buscarLibros(
        @Query("q") q: String,
        @Query("tipo") tipo: String? = null,
        @Query("pagina") pagina: Int = 0
    ): List<LibroDto>

    @GET("libros/{apiId}")
    suspend fun detalleLibro(@Path("apiId") apiId: String): LibroDto

    // ---- Biblioteca personal ----
    @GET("biblioteca")
    suspend fun biblioteca(): List<BibliotecaItemDto>

    @POST("biblioteca")
    suspend fun agregarABiblioteca(@Body body: AgregarBibliotecaRequest): BibliotecaItemDto

    @PUT("biblioteca/{id}")
    suspend fun actualizarBiblioteca(@Path("id") id: Long, @Body body: ActualizarBibliotecaRequest): BibliotecaItemDto

    @DELETE("biblioteca/{id}")
    suspend fun eliminarDeBiblioteca(@Path("id") id: Long): Response<Unit>

    // ---- Resenas y me gusta ----
    @GET("libros/{apiId}/resenas")
    suspend fun resenasDeLibro(@Path("apiId") apiId: String): ResenasLibroDto

    @POST("resenas")
    suspend fun crearResena(@Body body: CrearResenaRequest): ResenaDto

    @PUT("resenas/{id}")
    suspend fun actualizarResena(@Path("id") id: Long, @Body body: ActualizarResenaRequest): ResenaDto

    @DELETE("resenas/{id}")
    suspend fun eliminarResena(@Path("id") id: Long): Response<Unit>

    @POST("resenas/{id}/me-gusta")
    suspend fun darMeGusta(@Path("id") id: Long): MeGustaDto

    @DELETE("resenas/{id}/me-gusta")
    suspend fun quitarMeGusta(@Path("id") id: Long): MeGustaDto

    // ---- Recomendaciones y comunidad ----
    @POST("recomendaciones")
    suspend fun recomendar(@Body body: RecomendarRequest): RecomendacionDto

    @PUT("recomendaciones/{id}/visto")
    suspend fun marcarRecomendacionVista(@Path("id") id: Long): Response<Unit>

    @GET("feed")
    suspend fun feed(): FeedDto

    @GET("usuarios/buscar")
    suspend fun buscarUsuarios(@Query("q") q: String): List<UsuarioResumenDto>
}

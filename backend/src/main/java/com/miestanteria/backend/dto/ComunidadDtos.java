package com.miestanteria.backend.dto;

import com.miestanteria.backend.dto.BibliotecaDtos.LibroEntrada;
import com.miestanteria.backend.model.Recomendacion;
import com.miestanteria.backend.model.Resena;
import com.miestanteria.backend.model.Usuario;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Objetos de resenas (RF05), me gusta (RF06), recomendaciones (RF07) y feed de comunidad. */
public final class ComunidadDtos {

    private ComunidadDtos() {}

    public static final int MAX_COMENTARIO = 500;
    public static final int MAX_MENSAJE = 300;

    /** Datos minimos de un usuario para mostrarlo junto a una resena o recomendacion. */
    public record UsuarioResumen(Long id, String nombreUsuario, String fotoPerfilUrl) {
        public static UsuarioResumen from(Usuario u) {
            return new UsuarioResumen(u.getId(), u.getNombreUsuario(), u.getFotoPerfilUrl());
        }
    }

    // ---------- Resenas ----------

    public record CrearResenaRequest(
            @NotNull(message = "Faltan los datos del libro") @Valid LibroEntrada libro,
            @NotNull(message = "Elige una calificacion")
            @Min(value = 1, message = "La calificacion va de 1 a 5 estrellas")
            @Max(value = 5, message = "La calificacion va de 1 a 5 estrellas") Integer calificacion,
            @Size(max = MAX_COMENTARIO, message = "El comentario puede tener maximo 500 caracteres") String comentario
    ) {}

    public record ActualizarResenaRequest(
            @NotNull(message = "Elige una calificacion")
            @Min(value = 1, message = "La calificacion va de 1 a 5 estrellas")
            @Max(value = 5, message = "La calificacion va de 1 a 5 estrellas") Integer calificacion,
            @Size(max = MAX_COMENTARIO, message = "El comentario puede tener maximo 500 caracteres") String comentario
    ) {}

    public record ResenaDto(
            Long id,
            UsuarioResumen autor,
            LibroDto libro,
            Integer calificacion,
            String comentario,
            String fechaPublicacion,
            long meGustas,
            boolean meGustaMio,
            boolean esMia
    ) {
        public static ResenaDto from(Resena r, long meGustas, boolean meGustaMio, Long idUsuarioActual) {
            return new ResenaDto(r.getId(), UsuarioResumen.from(r.getUsuario()), LibroDto.from(r.getLibro()),
                    r.getCalificacion(), r.getComentario(),
                    Fechas.iso(r.getFechaPublicacion()),
                    meGustas, meGustaMio, r.getUsuario().getId().equals(idUsuarioActual));
        }
    }

    /** Resenas de un libro con su promedio. */
    public record ResenasLibroDto(double promedio, int total, List<ResenaDto> resenas) {}

    public record MeGustaDto(Long idResena, long meGustas, boolean meGustaMio) {}

    // ---------- Recomendaciones ----------

    public record RecomendarRequest(
            @NotNull(message = "Elige a quien recomendar") Long receptorId,
            @NotNull(message = "Faltan los datos del libro") @Valid LibroEntrada libro,
            @Size(max = MAX_MENSAJE, message = "El mensaje puede tener maximo 300 caracteres") String mensaje
    ) {}

    public record RecomendacionDto(
            Long id,
            UsuarioResumen emisor,
            LibroDto libro,
            String mensaje,
            boolean visto,
            String fechaEnvio
    ) {
        public static RecomendacionDto from(Recomendacion r) {
            return new RecomendacionDto(r.getId(), UsuarioResumen.from(r.getEmisor()), LibroDto.from(r.getLibro()),
                    r.getMensaje(), r.isVisto(), Fechas.iso(r.getFechaEnvio()));
        }
    }

    /** Muro de actividad (GET /feed): recomendaciones recibidas + resenas recientes de la comunidad. */
    public record FeedDto(long noVistas, List<RecomendacionDto> recomendaciones, List<ResenaDto> resenasRecientes) {}
}

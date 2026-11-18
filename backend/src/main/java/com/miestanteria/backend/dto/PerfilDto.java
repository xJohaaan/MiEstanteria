package com.miestanteria.backend.dto;

import com.miestanteria.backend.dto.ComunidadDtos.ResenaDto;

import java.util.List;

/** Perfil de un lector con sus estadisticas de lectura (RF08). */
public record PerfilDto(
        Long id,
        String nombreUsuario,
        String fotoPerfilUrl,
        String biografia,
        String fechaRegistro,
        boolean esMio,
        Estadisticas estadisticas,
        List<Conteo> generosFavoritos,
        List<Conteo> leidosPorAnio,
        List<ResenaDto> resenas
) {
    public record Estadisticas(
            int leidos,
            int leyendo,
            int porLeer,
            int paginasLeidas,
            int resenasEscritas,
            double promedioCalificacion,
            long meGustasRecibidos,
            long recomendacionesRecibidas
    ) {}

    /** Par etiqueta / cantidad para los graficos (genero o anio). */
    public record Conteo(String etiqueta, int cantidad) {}
}

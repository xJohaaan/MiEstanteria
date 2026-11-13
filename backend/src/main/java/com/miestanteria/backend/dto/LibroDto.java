package com.miestanteria.backend.dto;

import com.miestanteria.backend.model.Libro;

/** Libro tal como lo recibe la app (resultado de busqueda o libro guardado). */
public record LibroDto(
        Long id,
        String apiId,
        String titulo,
        String autores,
        String editorial,
        String fechaPublicacion,
        String portadaUrl,
        String descripcion,
        String categoria,
        Integer paginas
) {
    public static LibroDto from(Libro l) {
        return new LibroDto(l.getId(), l.getApiId(), l.getTitulo(), l.getAutores(), l.getEditorial(),
                l.getFechaPublicacion(), l.getPortadaUrl(), l.getDescripcion(), l.getCategoria(), l.getPaginas());
    }
}

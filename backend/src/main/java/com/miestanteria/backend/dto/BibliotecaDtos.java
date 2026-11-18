package com.miestanteria.backend.dto;

import com.miestanteria.backend.model.BibliotecaUsuario;
import com.miestanteria.backend.model.EstadoLectura;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Objetos de la biblioteca personal (RF04). */
public final class BibliotecaDtos {

    private BibliotecaDtos() {}

    /** Datos minimos del libro que la app envia al agregarlo (vienen del resultado de busqueda). */
    public record LibroEntrada(
            @NotBlank(message = "Falta el identificador del libro") String apiId,
            @NotBlank(message = "Falta el titulo del libro") String titulo,
            String autores,
            String editorial,
            String fechaPublicacion,
            String portadaUrl,
            String descripcion,
            String categoria,
            Integer paginas
    ) {}

    public record AgregarRequest(
            @NotNull(message = "Faltan los datos del libro") @Valid LibroEntrada libro,
            EstadoLectura estadoLectura,
            @Min(value = 0, message = "La pagina no puede ser negativa") Integer paginaActual
    ) {}

    public record ActualizarRequest(
            EstadoLectura estadoLectura,
            @Min(value = 0, message = "La pagina no puede ser negativa") Integer paginaActual
    ) {}

    public record BibliotecaItemDto(
            Long id,
            LibroDto libro,
            EstadoLectura estadoLectura,
            Integer paginaActual,
            String fechaAgregado
    ) {
        public static BibliotecaItemDto from(BibliotecaUsuario b) {
            return new BibliotecaItemDto(b.getId(), LibroDto.from(b.getLibro()), b.getEstadoLectura(),
                    b.getPaginaActual(), Fechas.iso(b.getFechaAgregado()));
        }
    }
}

package com.miestanteria.backend.service;

import com.miestanteria.backend.dto.LibroDto;
import com.miestanteria.backend.exception.ApiException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Pruebas de la conversion de la respuesta de Google Books (sin red). */
class GoogleBooksServiceTest {

    @Test
    void convierteVolumenCompleto() {
        var info = new GoogleBooksService.VolumeInfo("Cien anios de soledad", List.of("Gabriel Garcia Marquez"),
                "Sudamericana", "1967", "Novela", 471, List.of("Fiction"),
                new GoogleBooksService.ImageLinks(null, "http://books.google.com/portada.jpg"));
        LibroDto dto = GoogleBooksService.aLibroDto(new GoogleBooksService.Volume("abc123", info));

        assertThat(dto.apiId()).isEqualTo("abc123");
        assertThat(dto.titulo()).isEqualTo("Cien anios de soledad");
        assertThat(dto.autores()).isEqualTo("Gabriel Garcia Marquez");
        assertThat(dto.paginas()).isEqualTo(471);
        assertThat(dto.categoria()).isEqualTo("Fiction");
        assertThat(dto.portadaUrl()).startsWith("https://");
    }

    @Test
    void volumenSinDatosUsaValoresPorDefecto() {
        LibroDto dto = GoogleBooksService.aLibroDto(new GoogleBooksService.Volume("x", null));
        assertThat(dto.titulo()).isEqualTo("Sin titulo");
        assertThat(dto.autores()).isEqualTo("Autor desconocido");
        assertThat(dto.portadaUrl()).isNull();
    }

    @Test
    void variosAutoresSeUnenConComa() {
        var info = new GoogleBooksService.VolumeInfo("T", List.of("A", "B"), null, null, null, null, null, null);
        assertThat(GoogleBooksService.aLibroDto(new GoogleBooksService.Volume("x", info)).autores()).isEqualTo("A, B");
    }

    @Test
    void busquedaVaciaDevuelveError() {
        GoogleBooksService s = new GoogleBooksService("");
        assertThatThrownBy(() -> s.buscar("  ", null, 0)).isInstanceOf(ApiException.class);
    }
}

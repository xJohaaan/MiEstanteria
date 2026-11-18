package com.miestanteria.backend.service;

import com.miestanteria.backend.dto.LibroDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Conversion de resultados de Open Library (sin red). */
class OpenLibraryServiceTest {

    @Test
    void convierteDocumentoDeBusqueda() {
        var doc = new OpenLibraryService.Doc("/works/OL34766796W", "El Principito",
                List.of("Antoine de Saint-Exupery"), 13499066L, 1943, 110, List.of("Fiction"), List.of("Salamandra"));
        LibroDto dto = OpenLibraryService.aLibroDto(doc);

        assertThat(dto.apiId()).isEqualTo("OL34766796W");
        assertThat(dto.autores()).isEqualTo("Antoine de Saint-Exupery");
        assertThat(dto.portadaUrl()).isEqualTo("https://covers.openlibrary.org/b/id/13499066-M.jpg");
        assertThat(dto.fechaPublicacion()).isEqualTo("1943");
        assertThat(dto.paginas()).isEqualTo(110);
        assertThat(dto.editorial()).isEqualTo("Salamandra");
    }

    @Test
    void reconoceIdsDeOpenLibrary() {
        assertThat(OpenLibraryService.esIdOpenLibrary("OL34766796W")).isTrue();
        assertThat(OpenLibraryService.esIdOpenLibrary("zyTCAlFPjgYC")).isFalse();
        assertThat(OpenLibraryService.esIdOpenLibrary(null)).isFalse();
    }
}

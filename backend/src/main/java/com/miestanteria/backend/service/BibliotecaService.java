package com.miestanteria.backend.service;

import com.miestanteria.backend.dto.BibliotecaDtos.ActualizarRequest;
import com.miestanteria.backend.dto.BibliotecaDtos.AgregarRequest;
import com.miestanteria.backend.dto.BibliotecaDtos.BibliotecaItemDto;
import com.miestanteria.backend.exception.ApiException;
import com.miestanteria.backend.model.BibliotecaUsuario;
import com.miestanteria.backend.model.EstadoLectura;
import com.miestanteria.backend.model.Libro;
import com.miestanteria.backend.model.Usuario;
import com.miestanteria.backend.repository.BibliotecaUsuarioRepository;
import com.miestanteria.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Biblioteca personal de lectura (RF04): agregar, listar, cambiar estado/pagina y quitar libros. */
@Service
public class BibliotecaService {

    private final BibliotecaUsuarioRepository biblioteca;
    private final UsuarioRepository usuarios;
    private final LibroService libroService;

    public BibliotecaService(BibliotecaUsuarioRepository biblioteca, UsuarioRepository usuarios, LibroService libroService) {
        this.biblioteca = biblioteca;
        this.usuarios = usuarios;
        this.libroService = libroService;
    }

    @Transactional(readOnly = true)
    public List<BibliotecaItemDto> listar(Long idUsuario, EstadoLectura filtro) {
        return biblioteca.findAllDeUsuario(idUsuario).stream()
                .filter(b -> filtro == null || b.getEstadoLectura() == filtro)
                .map(BibliotecaItemDto::from)
                .toList();
    }

    /** Agrega un libro. Un mismo libro no puede estar dos veces en la biblioteca del usuario (409). */
    @Transactional
    public BibliotecaItemDto agregar(Long idUsuario, AgregarRequest req) {
        Usuario u = usuarios.findById(idUsuario).orElseThrow(() -> ApiException.unauthorized("La cuenta ya no existe"));
        Libro libro = libroService.obtenerOCrear(req.libro());
        if (biblioteca.findByUsuarioIdAndLibroId(idUsuario, libro.getId()).isPresent()) {
            throw ApiException.conflict("Este libro ya esta en tu biblioteca");
        }
        BibliotecaUsuario b = new BibliotecaUsuario();
        b.setUsuario(u);
        b.setLibro(libro);
        aplicarCambios(b, req.estadoLectura() != null ? req.estadoLectura() : EstadoLectura.POR_LEER, req.paginaActual());
        return BibliotecaItemDto.from(biblioteca.save(b));
    }

    @Transactional
    public BibliotecaItemDto actualizar(Long idUsuario, Long id, ActualizarRequest req) {
        BibliotecaUsuario b = buscarPropio(idUsuario, id);
        aplicarCambios(b, req.estadoLectura() != null ? req.estadoLectura() : b.getEstadoLectura(), req.paginaActual());
        return BibliotecaItemDto.from(b);
    }

    @Transactional
    public void eliminar(Long idUsuario, Long id) {
        biblioteca.delete(buscarPropio(idUsuario, id));
    }

    /**
     * Reglas del estado de lectura:
     * - la pagina no puede superar el total de paginas del libro;
     * - al marcarlo LEIDO la pagina pasa al final; al marcarlo POR_LEER vuelve a 0.
     */
    static void aplicarCambios(BibliotecaUsuario b, EstadoLectura estado, Integer pagina) {
        Integer total = b.getLibro().getPaginas();
        if (pagina != null && total != null && total > 0 && pagina > total) {
            throw ApiException.badRequest("El libro solo tiene " + total + " paginas");
        }
        b.setEstadoLectura(estado);
        switch (estado) {
            case LEIDO -> b.setPaginaActual(total != null && total > 0 ? total : (pagina != null ? pagina : b.getPaginaActual()));
            case POR_LEER -> b.setPaginaActual(0);
            case LEYENDO -> {
                if (pagina != null) b.setPaginaActual(pagina);
                else if (b.getPaginaActual() == null) b.setPaginaActual(0);
            }
        }
    }

    /** Solo el dueno puede ver o cambiar su registro; para cualquier otro usuario "no existe". */
    private BibliotecaUsuario buscarPropio(Long idUsuario, Long id) {
        return biblioteca.findDeUsuario(id, idUsuario)
                .orElseThrow(() -> ApiException.notFound("Ese libro no esta en tu biblioteca"));
    }
}

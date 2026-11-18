package com.miestanteria.backend.service;

import com.miestanteria.backend.dto.ComunidadDtos.ResenaDto;
import com.miestanteria.backend.dto.Fechas;
import com.miestanteria.backend.dto.PerfilDto;
import com.miestanteria.backend.dto.PerfilDto.Conteo;
import com.miestanteria.backend.dto.PerfilDto.Estadisticas;
import com.miestanteria.backend.exception.ApiException;
import com.miestanteria.backend.model.BibliotecaUsuario;
import com.miestanteria.backend.model.EstadoLectura;
import com.miestanteria.backend.model.EstadoUsuario;
import com.miestanteria.backend.model.Usuario;
import com.miestanteria.backend.repository.BibliotecaUsuarioRepository;
import com.miestanteria.backend.repository.MeGustaResenaRepository;
import com.miestanteria.backend.repository.RecomendacionRepository;
import com.miestanteria.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Perfil y estadisticas de lectura de un usuario (RF08). */
@Service
public class PerfilService {

    private final UsuarioRepository usuarios;
    private final BibliotecaUsuarioRepository biblioteca;
    private final MeGustaResenaRepository meGustas;
    private final RecomendacionRepository recomendaciones;
    private final ResenaService resenaService;

    public PerfilService(UsuarioRepository usuarios, BibliotecaUsuarioRepository biblioteca, MeGustaResenaRepository meGustas,
                         RecomendacionRepository recomendaciones, ResenaService resenaService) {
        this.usuarios = usuarios;
        this.biblioteca = biblioteca;
        this.meGustas = meGustas;
        this.recomendaciones = recomendaciones;
        this.resenaService = resenaService;
    }

    @Transactional(readOnly = true)
    public PerfilDto perfil(Long idUsuarioActual, Long idPerfil) {
        Usuario u = usuarios.findById(idPerfil)
                .filter(x -> x.getEstado() == EstadoUsuario.ACTIVO)
                .orElseThrow(() -> ApiException.notFound("Ese lector no existe"));
        List<BibliotecaUsuario> libros = biblioteca.findAllDeUsuario(idPerfil);
        List<ResenaDto> resenas = resenaService.deUsuario(idUsuarioActual, idPerfil);
        boolean esMio = idPerfil.equals(idUsuarioActual);

        Estadisticas e = new Estadisticas(
                contar(libros, EstadoLectura.LEIDO),
                contar(libros, EstadoLectura.LEYENDO),
                contar(libros, EstadoLectura.POR_LEER),
                libros.stream().mapToInt(PerfilService::paginasLeidas).sum(),
                resenas.size(),
                Math.round(resenas.stream().mapToInt(ResenaDto::calificacion).average().orElse(0) * 10) / 10.0,
                meGustas.recibidosPor(idPerfil),
                esMio ? recomendaciones.countByReceptorId(idPerfil) : 0
        );

        return new PerfilDto(u.getId(), u.getNombreUsuario(), u.getFotoPerfilUrl(), u.getBiografia(),
                Fechas.iso(u.getFechaRegistro()), esMio, e, generosFavoritos(libros), leidosPorAnio(libros),
                resenas.stream().limit(20).toList());
    }

    private static int contar(List<BibliotecaUsuario> libros, EstadoLectura estado) {
        return (int) libros.stream().filter(b -> b.getEstadoLectura() == estado).count();
    }

    /** Paginas leidas: el libro completo si esta LEIDO, o la pagina actual si se esta leyendo. */
    static int paginasLeidas(BibliotecaUsuario b) {
        Integer total = b.getLibro().getPaginas();
        int actual = b.getPaginaActual() == null ? 0 : b.getPaginaActual();
        if (b.getEstadoLectura() == EstadoLectura.LEIDO) return total != null && total > 0 ? total : actual;
        if (b.getEstadoLectura() == EstadoLectura.LEYENDO) return actual;
        return 0;
    }

    /** Las 5 categorias con mas libros en la biblioteca. */
    static List<Conteo> generosFavoritos(List<BibliotecaUsuario> libros) {
        Map<String, Long> porGenero = libros.stream()
                .map(b -> b.getLibro().getCategoria())
                .filter(c -> c != null && !c.isBlank())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return porGenero.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry.comparingByKey()))
                .limit(5)
                .map(en -> new Conteo(en.getKey(), en.getValue().intValue()))
                .toList();
    }

    /** Libros leidos agrupados por el anio en que se registraron en la estanteria. */
    static List<Conteo> leidosPorAnio(List<BibliotecaUsuario> libros) {
        Map<Integer, Integer> porAnio = new TreeMap<>();
        libros.stream()
                .filter(b -> b.getEstadoLectura() == EstadoLectura.LEIDO && b.getFechaAgregado() != null)
                .forEach(b -> porAnio.merge(b.getFechaAgregado().getYear(), 1, Integer::sum));
        return porAnio.entrySet().stream().map(en -> new Conteo(String.valueOf(en.getKey()), en.getValue())).toList();
    }
}

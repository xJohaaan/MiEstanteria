package com.miestanteria.backend.service;

import com.miestanteria.backend.dto.ComunidadDtos.ActualizarResenaRequest;
import com.miestanteria.backend.dto.ComunidadDtos.CrearResenaRequest;
import com.miestanteria.backend.dto.ComunidadDtos.MeGustaDto;
import com.miestanteria.backend.dto.ComunidadDtos.ResenaDto;
import com.miestanteria.backend.dto.ComunidadDtos.ResenasLibroDto;
import com.miestanteria.backend.exception.ApiException;
import com.miestanteria.backend.model.Libro;
import com.miestanteria.backend.model.MeGustaResena;
import com.miestanteria.backend.model.Resena;
import com.miestanteria.backend.model.Usuario;
import com.miestanteria.backend.repository.BibliotecaUsuarioRepository;
import com.miestanteria.backend.repository.MeGustaResenaRepository;
import com.miestanteria.backend.repository.ResenaRepository;
import com.miestanteria.backend.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Resenas con calificacion (RF05) y reacciones "Me gusta" (RF06). */
@Service
public class ResenaService {

    private final ResenaRepository resenas;
    private final MeGustaResenaRepository meGustas;
    private final UsuarioRepository usuarios;
    private final BibliotecaUsuarioRepository biblioteca;
    private final LibroService libroService;

    public ResenaService(ResenaRepository resenas, MeGustaResenaRepository meGustas, UsuarioRepository usuarios,
                         BibliotecaUsuarioRepository biblioteca, LibroService libroService) {
        this.resenas = resenas;
        this.meGustas = meGustas;
        this.usuarios = usuarios;
        this.biblioteca = biblioteca;
        this.libroService = libroService;
    }

    /**
     * Publica una resena. Reglas: el libro debe estar en la biblioteca del usuario (RF05)
     * y cada usuario tiene una sola resena por libro (si ya existe, se edita).
     */
    @Transactional
    public ResenaDto crear(Long idUsuario, CrearResenaRequest req) {
        Usuario u = usuarios.findById(idUsuario).orElseThrow(() -> ApiException.unauthorized("La cuenta ya no existe"));
        Libro libro = libroService.obtenerOCrear(req.libro());
        if (biblioteca.findByUsuarioIdAndLibroId(idUsuario, libro.getId()).isEmpty()) {
            throw ApiException.badRequest("Agrega el libro a tu biblioteca para poder resenarlo");
        }
        if (resenas.findByUsuarioIdAndLibroId(idUsuario, libro.getId()).isPresent()) {
            throw ApiException.conflict("Ya resenaste este libro; puedes editar tu resena");
        }
        Resena r = new Resena();
        r.setUsuario(u);
        r.setLibro(libro);
        r.setCalificacion(req.calificacion());
        r.setComentario(limpiar(req.comentario()));
        resenas.save(r);
        return ResenaDto.from(r, 0, false, idUsuario);
    }

    @Transactional
    public ResenaDto actualizar(Long idUsuario, Long id, ActualizarResenaRequest req) {
        Resena r = propia(idUsuario, id);
        r.setCalificacion(req.calificacion());
        r.setComentario(limpiar(req.comentario()));
        return aDtos(List.of(r), idUsuario).get(0);
    }

    @Transactional
    public void eliminar(Long idUsuario, Long id) {
        resenas.delete(propia(idUsuario, id));
    }

    @Transactional(readOnly = true)
    public ResenasLibroDto deLibro(Long idUsuario, String apiId) {
        List<Resena> lista = resenas.findDeLibro(apiId);
        double promedio = lista.stream().mapToInt(Resena::getCalificacion).average().orElse(0);
        return new ResenasLibroDto(Math.round(promedio * 10) / 10.0, lista.size(), aDtos(lista, idUsuario));
    }

    @Transactional(readOnly = true)
    public List<ResenaDto> recientes(Long idUsuario, int limite) {
        return aDtos(resenas.findRecientes(PageRequest.of(0, limite)), idUsuario);
    }

    @Transactional(readOnly = true)
    public List<ResenaDto> deUsuario(Long idUsuarioActual, Long idAutor) {
        return aDtos(resenas.findDeUsuario(idAutor), idUsuarioActual);
    }

    /** Dar "Me gusta". No se permite en resenas propias; repetirlo no duplica la reaccion. */
    @Transactional
    public MeGustaDto darMeGusta(Long idUsuario, Long idResena) {
        Resena r = resenas.findCompleta(idResena).orElseThrow(() -> ApiException.notFound("La resena no existe"));
        if (r.getUsuario().getId().equals(idUsuario)) {
            throw ApiException.forbidden("No puedes dar Me gusta a tu propia resena");
        }
        if (meGustas.findByResenaIdAndUsuarioId(idResena, idUsuario).isEmpty()) {
            MeGustaResena mg = new MeGustaResena();
            mg.setResena(r);
            mg.setUsuario(usuarios.getReferenceById(idUsuario));
            meGustas.save(mg);
        }
        return estadoMeGusta(idUsuario, idResena);
    }

    @Transactional
    public MeGustaDto quitarMeGusta(Long idUsuario, Long idResena) {
        if (!resenas.existsById(idResena)) throw ApiException.notFound("La resena no existe");
        meGustas.findByResenaIdAndUsuarioId(idResena, idUsuario).ifPresent(meGustas::delete);
        meGustas.flush();
        return estadoMeGusta(idUsuario, idResena);
    }

    private MeGustaDto estadoMeGusta(Long idUsuario, Long idResena) {
        long total = meGustas.contarPorResena(List.of(idResena)).stream().mapToLong(f -> (Long) f[1]).sum();
        boolean mio = !meGustas.idsConMeGustaDe(idUsuario, List.of(idResena)).isEmpty();
        return new MeGustaDto(idResena, total, mio);
    }

    /** Convierte resenas a DTO calculando los "Me gusta" de todas con dos consultas (no una por resena). */
    List<ResenaDto> aDtos(List<Resena> lista, Long idUsuario) {
        if (lista.isEmpty()) return List.of();
        List<Long> ids = lista.stream().map(Resena::getId).toList();
        Map<Long, Long> conteo = new HashMap<>();
        for (Object[] fila : meGustas.contarPorResena(ids)) conteo.put((Long) fila[0], (Long) fila[1]);
        Set<Long> mios = new HashSet<>(meGustas.idsConMeGustaDe(idUsuario, ids));
        return lista.stream()
                .map(r -> ResenaDto.from(r, conteo.getOrDefault(r.getId(), 0L), mios.contains(r.getId()), idUsuario))
                .toList();
    }

    private Resena propia(Long idUsuario, Long id) {
        Resena r = resenas.findCompleta(id).orElseThrow(() -> ApiException.notFound("La resena no existe"));
        if (!r.getUsuario().getId().equals(idUsuario)) {
            throw ApiException.forbidden("Solo puedes modificar tus propias resenas");
        }
        return r;
    }

    private static String limpiar(String comentario) {
        if (comentario == null || comentario.isBlank()) return null;
        return comentario.trim();
    }
}

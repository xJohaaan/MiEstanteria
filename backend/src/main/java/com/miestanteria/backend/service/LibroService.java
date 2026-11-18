package com.miestanteria.backend.service;

import com.miestanteria.backend.dto.BibliotecaDtos.LibroEntrada;
import com.miestanteria.backend.model.Libro;
import com.miestanteria.backend.repository.LibroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Guarda en la tabla libros una copia de los libros que los usuarios usan (por su apiId). */
@Service
public class LibroService {

    private final LibroRepository libros;

    public LibroService(LibroRepository libros) {
        this.libros = libros;
    }

    /** Devuelve el libro con ese apiId; si no existe lo crea, y si existe completa los datos que le falten. */
    @Transactional
    public Libro obtenerOCrear(LibroEntrada e) {
        Libro l = libros.findByApiId(e.apiId()).orElseGet(Libro::new);
        l.setApiId(e.apiId());
        l.setTitulo(recortar(e.titulo(), 255));
        if (l.getAutores() == null || e.autores() != null) {
            l.setAutores(e.autores() != null && !e.autores().isBlank() ? recortar(e.autores(), 255) : "Autor desconocido");
        }
        if (e.editorial() != null) l.setEditorial(recortar(e.editorial(), 150));
        if (e.fechaPublicacion() != null) l.setFechaPublicacion(recortar(e.fechaPublicacion(), 20));
        if (e.portadaUrl() != null) l.setPortadaUrl(recortar(e.portadaUrl(), 500));
        if (e.descripcion() != null) l.setDescripcion(recortar(e.descripcion(), 8000));
        if (e.categoria() != null) l.setCategoria(recortar(e.categoria(), 50));
        if (e.paginas() != null && e.paginas() > 0) l.setPaginas(e.paginas());
        return libros.save(l);
    }

    private static String recortar(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}

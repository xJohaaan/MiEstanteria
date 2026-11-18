package com.miestanteria.backend.repository;

import com.miestanteria.backend.model.BibliotecaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BibliotecaUsuarioRepository extends JpaRepository<BibliotecaUsuario, Long> {

    /** Biblioteca completa de un usuario con los datos del libro (una sola consulta). */
    @Query("select b from BibliotecaUsuario b join fetch b.libro where b.usuario.id = :idUsuario order by b.fechaAgregado desc")
    List<BibliotecaUsuario> findAllDeUsuario(@Param("idUsuario") Long idUsuario);

    @Query("select b from BibliotecaUsuario b join fetch b.libro where b.id = :id and b.usuario.id = :idUsuario")
    Optional<BibliotecaUsuario> findDeUsuario(@Param("id") Long id, @Param("idUsuario") Long idUsuario);

    Optional<BibliotecaUsuario> findByUsuarioIdAndLibroId(Long idUsuario, Long idLibro);
}

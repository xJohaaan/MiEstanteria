package com.miestanteria.backend.repository;

import com.miestanteria.backend.model.Resena;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResenaRepository extends JpaRepository<Resena, Long> {

    @Query("select r from Resena r join fetch r.usuario join fetch r.libro where r.libro.apiId = :apiId order by r.fechaPublicacion desc")
    List<Resena> findDeLibro(@Param("apiId") String apiId);

    @Query("select r from Resena r join fetch r.usuario join fetch r.libro order by r.fechaPublicacion desc")
    List<Resena> findRecientes(Pageable pageable);

    @Query("select r from Resena r join fetch r.usuario join fetch r.libro where r.usuario.id = :idUsuario order by r.fechaPublicacion desc")
    List<Resena> findDeUsuario(@Param("idUsuario") Long idUsuario);

    @Query("select r from Resena r join fetch r.usuario join fetch r.libro where r.id = :id")
    Optional<Resena> findCompleta(@Param("id") Long id);

    Optional<Resena> findByUsuarioIdAndLibroId(Long idUsuario, Long idLibro);
}

package com.miestanteria.backend.repository;

import com.miestanteria.backend.model.Recomendacion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecomendacionRepository extends JpaRepository<Recomendacion, Long> {

    @Query("select r from Recomendacion r join fetch r.emisor join fetch r.libro where r.receptor.id = :idUsuario order by r.fechaEnvio desc")
    List<Recomendacion> findRecibidas(@Param("idUsuario") Long idUsuario, Pageable pageable);

    @Query("select r from Recomendacion r where r.id = :id and r.receptor.id = :idUsuario")
    Optional<Recomendacion> findRecibida(@Param("id") Long id, @Param("idUsuario") Long idUsuario);

    long countByReceptorIdAndVistoFalse(Long idUsuario);
}

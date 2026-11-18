package com.miestanteria.backend.repository;

import com.miestanteria.backend.model.MeGustaResena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MeGustaResenaRepository extends JpaRepository<MeGustaResena, Long> {

    Optional<MeGustaResena> findByResenaIdAndUsuarioId(Long idResena, Long idUsuario);

    /** Borrado directo: si ya no existe no falla (dos toques seguidos no producen error). */
    @org.springframework.data.jpa.repository.Modifying
    @Query("delete from MeGustaResena m where m.resena.id = :idResena and m.usuario.id = :idUsuario")
    int borrar(@Param("idResena") Long idResena, @Param("idUsuario") Long idUsuario);

    /** Cantidad de "Me gusta" de varias resenas en una sola consulta: filas [idResena, cantidad]. */
    @Query("select m.resena.id, count(m) from MeGustaResena m where m.resena.id in :ids group by m.resena.id")
    List<Object[]> contarPorResena(@Param("ids") Collection<Long> ids);

    /** Cuales de esas resenas ya tienen "Me gusta" del usuario. */
    @Query("select m.resena.id from MeGustaResena m where m.usuario.id = :idUsuario and m.resena.id in :ids")
    List<Long> idsConMeGustaDe(@Param("idUsuario") Long idUsuario, @Param("ids") Collection<Long> ids);

    @Query("select count(m) from MeGustaResena m where m.resena.usuario.id = :idUsuario")
    long recibidosPor(@Param("idUsuario") Long idUsuario);
}

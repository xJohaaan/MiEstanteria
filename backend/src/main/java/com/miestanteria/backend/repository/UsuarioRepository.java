package com.miestanteria.backend.repository;

import com.miestanteria.backend.model.Usuario;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    /** Lectores activos cuyo nombre contiene el texto (para elegir a quien recomendar). */
    @Query("select u from Usuario u where u.id <> :excluir and u.estado = com.miestanteria.backend.model.EstadoUsuario.ACTIVO " +
            "and lower(u.nombreUsuario) like lower(concat('%', :texto, '%')) order by u.nombreUsuario")
    List<Usuario> buscarPorNombre(@Param("texto") String texto, @Param("excluir") Long excluir, Pageable pageable);
}

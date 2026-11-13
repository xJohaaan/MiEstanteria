package com.miestanteria.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Tabla me_gusta_resenas: un "Me gusta" de un usuario sobre una resena. */
@Entity
@Table(name = "me_gusta_resenas",
        uniqueConstraints = @UniqueConstraint(name = "uq_resena_usuario", columnNames = {"id_resena", "id_usuario"}))
public class MeGustaResena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mg")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_resena")
    private Resena resena;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "fecha_reaccion", nullable = false)
    private LocalDateTime fechaReaccion;

    @PrePersist
    void alCrear() {
        if (fechaReaccion == null) fechaReaccion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Resena getResena() { return resena; }
    public void setResena(Resena resena) { this.resena = resena; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getFechaReaccion() { return fechaReaccion; }
    public void setFechaReaccion(LocalDateTime fechaReaccion) { this.fechaReaccion = fechaReaccion; }
}

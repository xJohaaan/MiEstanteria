package com.miestanteria.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Tabla biblioteca_usuario: un libro dentro de la estanteria personal de un usuario. */
@Entity
@Table(name = "biblioteca_usuario",
        uniqueConstraints = @UniqueConstraint(name = "uq_usuario_libro", columnNames = {"id_usuario", "id_libro"}))
public class BibliotecaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_biblioteca")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_libro")
    private Libro libro;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_lectura", nullable = false)
    private EstadoLectura estadoLectura = EstadoLectura.POR_LEER;

    @Column(name = "pagina_actual")
    private Integer paginaActual = 0;

    @Column(name = "fecha_agregado", nullable = false)
    private LocalDateTime fechaAgregado;

    @PrePersist
    void alCrear() {
        if (fechaAgregado == null) fechaAgregado = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }
    public EstadoLectura getEstadoLectura() { return estadoLectura; }
    public void setEstadoLectura(EstadoLectura estadoLectura) { this.estadoLectura = estadoLectura; }
    public Integer getPaginaActual() { return paginaActual; }
    public void setPaginaActual(Integer paginaActual) { this.paginaActual = paginaActual; }
    public LocalDateTime getFechaAgregado() { return fechaAgregado; }
    public void setFechaAgregado(LocalDateTime fechaAgregado) { this.fechaAgregado = fechaAgregado; }
}

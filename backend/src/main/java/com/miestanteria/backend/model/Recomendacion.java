package com.miestanteria.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Tabla recomendaciones: un usuario le recomienda un libro a otro. */
@Entity
@Table(name = "recomendaciones")
public class Recomendacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recomendacion")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_emisor")
    private Usuario emisor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario_receptor")
    private Usuario receptor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_libro")
    private Libro libro;

    @Column(length = 300)
    private String mensaje;

    @Column(nullable = false)
    private boolean visto = false;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @PrePersist
    void alCrear() {
        if (fechaEnvio == null) fechaEnvio = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getEmisor() { return emisor; }
    public void setEmisor(Usuario emisor) { this.emisor = emisor; }
    public Usuario getReceptor() { return receptor; }
    public void setReceptor(Usuario receptor) { this.receptor = receptor; }
    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public boolean isVisto() { return visto; }
    public void setVisto(boolean visto) { this.visto = visto; }
    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime fechaEnvio) { this.fechaEnvio = fechaEnvio; }
}

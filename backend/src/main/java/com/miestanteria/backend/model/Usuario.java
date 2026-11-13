package com.miestanteria.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Tabla usuarios: cuenta de cada lector registrado. */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(name = "nombre_usuario", nullable = false, length = 100)
    private String nombreUsuario;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    /** Contrasena cifrada con BCrypt; nunca se guarda en texto plano. */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "foto_perfil_url", length = 500)
    private String fotoPerfilUrl;

    @Column(length = 2000)
    private String biografia;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    @PrePersist
    void alCrear() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
        if (estado == null) estado = EstadoUsuario.ACTIVO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFotoPerfilUrl() { return fotoPerfilUrl; }
    public void setFotoPerfilUrl(String fotoPerfilUrl) { this.fotoPerfilUrl = fotoPerfilUrl; }
    public String getBiografia() { return biografia; }
    public void setBiografia(String biografia) { this.biografia = biografia; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public EstadoUsuario getEstado() { return estado; }
    public void setEstado(EstadoUsuario estado) { this.estado = estado; }
}

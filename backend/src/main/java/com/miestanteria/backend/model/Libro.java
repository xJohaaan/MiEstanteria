package com.miestanteria.backend.model;

import jakarta.persistence.*;

/** Tabla libros: copia local de los datos que vienen de Google Books. */
@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_libro")
    private Long id;

    /** Identificador del volumen en Google Books. */
    @Column(name = "api_id", nullable = false, unique = true, length = 100)
    private String apiId;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String autores;

    @Column(length = 150)
    private String editorial;

    @Column(name = "fecha_publicacion", length = 20)
    private String fechaPublicacion;

    @Column(name = "portada_url", length = 500)
    private String portadaUrl;

    @Column(length = 8000)
    private String descripcion;

    @Column(length = 50)
    private String categoria;

    /** Numero de paginas (para calcular el porcentaje de avance). */
    private Integer paginas;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getApiId() { return apiId; }
    public void setApiId(String apiId) { this.apiId = apiId; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getAutores() { return autores; }
    public void setAutores(String autores) { this.autores = autores; }
    public String getEditorial() { return editorial; }
    public void setEditorial(String editorial) { this.editorial = editorial; }
    public String getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(String fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    public String getPortadaUrl() { return portadaUrl; }
    public void setPortadaUrl(String portadaUrl) { this.portadaUrl = portadaUrl; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public Integer getPaginas() { return paginas; }
    public void setPaginas(Integer paginas) { this.paginas = paginas; }
}

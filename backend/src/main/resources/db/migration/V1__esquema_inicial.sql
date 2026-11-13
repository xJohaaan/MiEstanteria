-- Esquema inicial de Mi Estanteria (seccion 19.7 del documento de avance)

CREATE TABLE usuarios (
    id_usuario      INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario  VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    foto_perfil_url VARCHAR(500) NULL,
    biografia       TEXT         NULL,
    fecha_registro  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado          ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO'
) ENGINE=InnoDB;

CREATE TABLE libros (
    id_libro          INT AUTO_INCREMENT PRIMARY KEY,
    api_id            VARCHAR(100) NOT NULL UNIQUE,
    titulo            VARCHAR(255) NOT NULL,
    autores           VARCHAR(255) NOT NULL,
    editorial         VARCHAR(150) NULL,
    fecha_publicacion VARCHAR(20)  NULL,
    portada_url       VARCHAR(500) NULL,
    descripcion       TEXT         NULL,
    categoria         VARCHAR(50)  NULL,
    paginas           INT          NULL
) ENGINE=InnoDB;

CREATE TABLE biblioteca_usuario (
    id_biblioteca  INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario     INT NOT NULL,
    id_libro       INT NOT NULL,
    estado_lectura ENUM('POR_LEER','LEYENDO','LEIDO') NOT NULL DEFAULT 'POR_LEER',
    pagina_actual  INT NULL DEFAULT 0,
    fecha_agregado DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bib_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_bib_libro   FOREIGN KEY (id_libro)   REFERENCES libros(id_libro)     ON DELETE CASCADE,
    CONSTRAINT uq_usuario_libro UNIQUE (id_usuario, id_libro)
) ENGINE=InnoDB;

CREATE TABLE resenas (
    id_resena         INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario        INT NOT NULL,
    id_libro          INT NOT NULL,
    calificacion      TINYINT NOT NULL,
    comentario        TEXT NULL,
    fecha_publicacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_res_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_res_libro   FOREIGN KEY (id_libro)   REFERENCES libros(id_libro)     ON DELETE CASCADE,
    CONSTRAINT chk_calificacion CHECK (calificacion BETWEEN 1 AND 5)
) ENGINE=InnoDB;

CREATE TABLE recomendaciones (
    id_recomendacion    INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario_emisor   INT NOT NULL,
    id_usuario_receptor INT NOT NULL,
    id_libro            INT NOT NULL,
    mensaje             TEXT NULL,
    visto               BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_envio         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rec_emisor   FOREIGN KEY (id_usuario_emisor)   REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_rec_receptor FOREIGN KEY (id_usuario_receptor) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_rec_libro    FOREIGN KEY (id_libro)            REFERENCES libros(id_libro)     ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE me_gusta_resenas (
    id_mg          INT AUTO_INCREMENT PRIMARY KEY,
    id_resena      INT NOT NULL,
    id_usuario     INT NOT NULL,
    fecha_reaccion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mg_resena  FOREIGN KEY (id_resena)  REFERENCES resenas(id_resena)   ON DELETE CASCADE,
    CONSTRAINT fk_mg_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    CONSTRAINT uq_resena_usuario UNIQUE (id_resena, id_usuario)
) ENGINE=InnoDB;

CREATE INDEX idx_resenas_libro   ON resenas(id_libro);
CREATE INDEX idx_resenas_usuario ON resenas(id_usuario);
CREATE INDEX idx_bib_usuario     ON biblioteca_usuario(id_usuario);
CREATE INDEX idx_bib_estado      ON biblioteca_usuario(estado_lectura);
CREATE INDEX idx_rec_receptor    ON recomendaciones(id_usuario_receptor, visto);
CREATE INDEX idx_mg_resena       ON me_gusta_resenas(id_resena);

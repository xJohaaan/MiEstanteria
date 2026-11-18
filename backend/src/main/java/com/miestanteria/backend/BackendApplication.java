package com.miestanteria.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

/** Punto de entrada del servidor API REST de Mi Estanteria. */
@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        // Todas las fechas se manejan en UTC; cada celular las convierte a su hora local
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(BackendApplication.class, args);
    }
}

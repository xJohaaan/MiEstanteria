package com.miestanteria.backend.dto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Las fechas se guardan en UTC (el servidor fija su zona horaria en UTC al arrancar)
 * y se envian con la zona marcada ("2026-11-18T03:53:00Z") para que cada celular
 * las muestre en su hora local.
 */
public final class Fechas {

    private Fechas() {}

    public static String iso(LocalDateTime fechaUtc) {
        return fechaUtc == null ? null : fechaUtc.atOffset(ZoneOffset.UTC).toString();
    }
}

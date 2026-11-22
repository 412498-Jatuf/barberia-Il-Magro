package com.ilmagro.barberia.domain.agenda;

import com.ilmagro.barberia.domain.shared.DatoInvalidoException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Un cierre puntual de la agenda: un dia entero (feriado, viaje) o unas horas de un dia. No toca el
 * horario semanal: solo le saca lo bloqueado a esa fecha.
 */
@Entity
@Table(name = "bloqueo_agenda")
public class Bloqueo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate fecha;

    /** Si desde o hasta no estan, se bloquea el dia entero. */
    private LocalTime desde;
    private LocalTime hasta;

    @Column(length = 200)
    private String motivo;

    protected Bloqueo() {
    }

    private Bloqueo(LocalDate fecha, LocalTime desde, LocalTime hasta, String motivo) {
        this.fecha = fecha;
        this.desde = desde;
        this.hasta = hasta;
        this.motivo = motivo;
    }

    public static Bloqueo diaCompleto(LocalDate fecha, String motivo) {
        return nuevo(fecha, null, null, motivo);
    }

    public static Bloqueo horas(LocalDate fecha, LocalTime desde, LocalTime hasta, String motivo) {
        if (desde == null || hasta == null) {
            throw new DatoInvalidoException("Faltan las horas a bloquear");
        }
        new Tramo(desde, hasta); // valida que el fin sea posterior al inicio
        return nuevo(fecha, desde, hasta, motivo);
    }

    private static Bloqueo nuevo(LocalDate fecha, LocalTime desde, LocalTime hasta, String motivo) {
        if (fecha == null) {
            throw new DatoInvalidoException("Falta la fecha a bloquear");
        }
        String limpio = motivo == null || motivo.isBlank() ? null : motivo.trim();
        if (limpio != null && limpio.length() > 200) {
            throw new DatoInvalidoException("El motivo no puede superar los 200 caracteres");
        }
        return new Bloqueo(fecha, desde, hasta, limpio);
    }

    public boolean esDiaCompleto() {
        return desde == null || hasta == null;
    }

    /** Las horas que cierra este bloqueo (todo el dia si es dia completo). */
    public Tramo tramo() {
        return esDiaCompleto() ? new Tramo(LocalTime.MIN, LocalTime.MAX) : new Tramo(desde, hasta);
    }

    public Long getId() { return id; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getDesde() { return desde; }
    public LocalTime getHasta() { return hasta; }
    public String getMotivo() { return motivo; }
}

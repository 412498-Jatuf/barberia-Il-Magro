package com.ilmagro.barberia.domain.agenda;

import com.ilmagro.barberia.domain.shared.DatoInvalidoException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalTime;

/**
 * Un tramo del horario semanal: ese dia de la semana se atiende de {@code desde} a {@code hasta}.
 * Un dia con dos tramos tiene pausa (ej. 15 a 18 y 19 a 22); un dia sin tramos esta cerrado.
 */
@Entity
@Table(name = "horario_tramo")
public class TramoSemanal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Dia segun ISO-8601 (1 = lunes ... 7 = domingo). */
    @Column(nullable = false)
    private int diaSemana;

    @Column(nullable = false)
    private LocalTime desde;

    @Column(nullable = false)
    private LocalTime hasta;

    protected TramoSemanal() {
    }

    public TramoSemanal(int diaSemana, LocalTime desde, LocalTime hasta) {
        if (diaSemana < 1 || diaSemana > 7) {
            throw new DatoInvalidoException("El dia de la semana debe estar entre 1 y 7");
        }
        new Tramo(desde, hasta); // valida que el fin sea posterior al inicio
        this.diaSemana = diaSemana;
        this.desde = desde;
        this.hasta = hasta;
    }

    public Tramo tramo() {
        return new Tramo(desde, hasta);
    }

    public Long getId() { return id; }
    public int getDiaSemana() { return diaSemana; }
    public LocalTime getDesde() { return desde; }
    public LocalTime getHasta() { return hasta; }
}

package com.ilmagro.barberia.domain.catalogo;

import com.ilmagro.barberia.domain.shared.DatoInvalidoException;
import jakarta.persistence.*;

import java.math.BigDecimal;

/** Servicio que ofrece la barberia (corte, barba, combo...). */
@Entity
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    @Column(nullable = false)
    private int duracionMinutos;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    protected Servicio() {
    }

    public Servicio(String nombre, String descripcion, int duracionMinutos, BigDecimal precio) {
        if (duracionMinutos <= 0) {
            throw new DatoInvalidoException("La duracion del servicio debe ser positiva");
        }
        if (precio == null || precio.signum() < 0) {
            throw new DatoInvalidoException("El precio del servicio no puede ser negativo");
        }
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.duracionMinutos = duracionMinutos;
        this.precio = precio;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getDuracionMinutos() { return duracionMinutos; }
    public BigDecimal getPrecio() { return precio; }
}

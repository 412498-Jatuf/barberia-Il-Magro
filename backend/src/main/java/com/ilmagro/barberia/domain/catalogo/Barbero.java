package com.ilmagro.barberia.domain.catalogo;

import jakarta.persistence.*;

@Entity
public class Barbero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String especialidad;

    private String fotoUrl;

    protected Barbero() {
    }

    public Barbero(String nombre, String especialidad, String fotoUrl) {
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.fotoUrl = fotoUrl;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEspecialidad() { return especialidad; }
    public String getFotoUrl() { return fotoUrl; }
}

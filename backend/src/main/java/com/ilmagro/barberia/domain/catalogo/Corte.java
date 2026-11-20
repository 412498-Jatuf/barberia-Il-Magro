package com.ilmagro.barberia.domain.catalogo;

import jakarta.persistence.*;

/** Estilo de corte que se exhibe en la galeria. */
@Entity
public class Corte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    private String categoria;

    @Column(nullable = false)
    private String imagenUrl;

    protected Corte() {
    }

    public Corte(String nombre, String descripcion, String categoria, String imagenUrl) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.imagenUrl = imagenUrl;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public String getImagenUrl() { return imagenUrl; }
}

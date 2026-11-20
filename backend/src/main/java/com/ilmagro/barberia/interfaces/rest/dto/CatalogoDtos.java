package com.ilmagro.barberia.interfaces.rest.dto;

import com.ilmagro.barberia.domain.catalogo.Barbero;
import com.ilmagro.barberia.domain.catalogo.Corte;
import com.ilmagro.barberia.domain.catalogo.Servicio;

import java.math.BigDecimal;

/** DTOs de salida del catalogo: desacoplan el JSON publico de las entidades de dominio. */
public final class CatalogoDtos {

    private CatalogoDtos() {
    }

    public record CorteResponse(Long id, String nombre, String descripcion, String categoria, String imagenUrl) {
        public static CorteResponse de(Corte c) {
            return new CorteResponse(c.getId(), c.getNombre(), c.getDescripcion(), c.getCategoria(), c.getImagenUrl());
        }
    }

    public record ServicioResponse(Long id, String nombre, String descripcion, int duracionMinutos, BigDecimal precio) {
        public static ServicioResponse de(Servicio s) {
            return new ServicioResponse(s.getId(), s.getNombre(), s.getDescripcion(), s.getDuracionMinutos(), s.getPrecio());
        }
    }

    public record BarberoResponse(Long id, String nombre, String especialidad, String fotoUrl) {
        public static BarberoResponse de(Barbero b) {
            return new BarberoResponse(b.getId(), b.getNombre(), b.getEspecialidad(), b.getFotoUrl());
        }
    }
}

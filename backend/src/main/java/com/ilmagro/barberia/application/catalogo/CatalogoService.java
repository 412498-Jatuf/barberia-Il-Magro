package com.ilmagro.barberia.application.catalogo;

import com.ilmagro.barberia.domain.catalogo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Casos de uso de lectura del catalogo: galeria de cortes, servicios y equipo. */
@Service
@Transactional(readOnly = true)
public class CatalogoService {

    private final CorteRepository cortes;
    private final ServicioRepository servicios;
    private final BarberoRepository barberos;

    public CatalogoService(CorteRepository cortes, ServicioRepository servicios, BarberoRepository barberos) {
        this.cortes = cortes;
        this.servicios = servicios;
        this.barberos = barberos;
    }

    public List<Corte> listarCortes() {
        return cortes.buscarTodos();
    }

    public List<Servicio> listarServicios() {
        return servicios.buscarTodos();
    }

    public List<Barbero> listarBarberos() {
        return barberos.buscarTodos();
    }
}

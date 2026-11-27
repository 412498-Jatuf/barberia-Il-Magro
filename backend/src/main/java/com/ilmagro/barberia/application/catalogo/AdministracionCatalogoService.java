package com.ilmagro.barberia.application.catalogo;

import com.ilmagro.barberia.application.shared.RecursoNoEncontradoException;
import com.ilmagro.barberia.domain.catalogo.Servicio;
import com.ilmagro.barberia.domain.catalogo.ServicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Casos de uso del panel de Luca sobre el catalogo: por ahora, cambiar el precio de un servicio. La
 * lectura publica del catalogo esta aparte, en {@link CatalogoService}.
 */
@Service
@Transactional
public class AdministracionCatalogoService {

    private final ServicioRepository servicios;

    public AdministracionCatalogoService(ServicioRepository servicios) {
        this.servicios = servicios;
    }

    public Servicio cambiarPrecio(Long servicioId, BigDecimal nuevoPrecio) {
        Servicio servicio = servicios.buscarPorId(servicioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado"));
        servicio.cambiarPrecio(nuevoPrecio);
        return servicios.guardar(servicio);
    }
}

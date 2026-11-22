package com.ilmagro.barberia.infrastructure.seed;

import com.ilmagro.barberia.domain.catalogo.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Carga los datos de Il Magro la primera vez que arranca la app (base vacia).
 * Precios confirmados por la barberia (noviembre 2026). En una base ya cargada los actualiza
 * la migracion V3__precios_y_fotos_2026_11.sql.
 */
@Component
class DatosIniciales implements ApplicationRunner {

    private final CorteRepository cortes;
    private final ServicioRepository servicios;
    private final BarberoRepository barberos;

    DatosIniciales(CorteRepository cortes, ServicioRepository servicios, BarberoRepository barberos) {
        this.cortes = cortes;
        this.servicios = servicios;
        this.barberos = barberos;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (cortes.contar() == 0) {
            // Fotos reales de trabajos de Il Magro (frontend/public/assets/img).
            cortes.guardar(new Corte("Mullet ondulado", "Ondas con volumen arriba, largo atrás y taper bajo alrededor de la oreja.", "Mullet", "/assets/img/corte-mullet-ondulado.jpg"));
            cortes.guardar(new Corte("Mullet con burst fade", "Rulos con textura y un burst fade que rodea la oreja.", "Mullet", "/assets/img/corte-mullet-burst.jpg"));
            cortes.guardar(new Corte("Flequillo con mid fade", "Flequillo largo hacia el costado y degradado medio.", "Fade", "/assets/img/corte-flequillo-fade.jpg"));
            cortes.guardar(new Corte("Mullet con barba", "Flequillo, largo en la nuca, taper fade y barba perfilada.", "Mullet", "/assets/img/corte-mullet-barba.jpg"));
        }
        if (servicios.contar() == 0) {
            servicios.guardar(new Servicio("Corte", "A tijera o máquina, con lavado y peinado", 30, new BigDecimal("13000")));
            servicios.guardar(new Servicio("Barba", "Perfilado con navaja y toalla caliente", 30, new BigDecimal("5000")));
            servicios.guardar(new Servicio("Corte y barba", "Los dos servicios en un turno", 30, new BigDecimal("15000")));
        }
        if (barberos.contar() == 0) {
            barberos.guardar(new Barbero("Luca Fracassi", "Barbero", null));
        }
    }
}

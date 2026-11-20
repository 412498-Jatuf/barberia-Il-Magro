package com.ilmagro.barberia.infrastructure.seed;

import com.ilmagro.barberia.domain.catalogo.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Carga los datos de Il Magro la primera vez que arranca la app (base vacia).
 * Precios confirmados por la barberia (noviembre 2026).
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
            cortes.guardar(new Corte("Flequillo con fade", "Flequillo texturizado hacia adelante y degradado alto a los costados.", "Fade", "/assets/img/corte-flequillo.jpg"));
            cortes.guardar(new Corte("Mullet con taper", "Volumen arriba, largo atrás y un taper bajo alrededor de la oreja.", "Fade", "/assets/img/corte-mullet.jpg"));
            cortes.guardar(new Corte("Texturizado con fade", "Arriba con textura y movimiento, costados en degradado medio.", "Fade", "/assets/img/corte-texturizado.jpg"));
            cortes.guardar(new Corte("Bowl con fade", "Línea recta en el flequillo y nuca degradada a cero.", "Fade", "/assets/img/corte-bowl.jpg"));
            cortes.guardar(new Corte("Bob con nuca rapada", "Largo a la mandíbula, terminado a tijera, con la nuca rapada.", "Tijera", "/assets/img/corte-bob.jpg"));
        }
        if (servicios.contar() == 0) {
            servicios.guardar(new Servicio("Corte", "A tijera o máquina, con lavado y peinado", 30, new BigDecimal("14000")));
            servicios.guardar(new Servicio("Barba", "Perfilado con navaja y toalla caliente", 30, new BigDecimal("8000")));
            servicios.guardar(new Servicio("Corte y barba", "Los dos servicios en un turno", 60, new BigDecimal("16000")));
            servicios.guardar(new Servicio("Corte infantil", "Hasta 12 años", 30, new BigDecimal("10000")));
        }
        if (barberos.contar() == 0) {
            barberos.guardar(new Barbero("Luca Fracassi", "Barbero", null));
        }
    }
}

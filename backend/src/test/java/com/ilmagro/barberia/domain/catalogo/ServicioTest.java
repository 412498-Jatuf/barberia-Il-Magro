package com.ilmagro.barberia.domain.catalogo;

import com.ilmagro.barberia.domain.shared.DatoInvalidoException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** El precio de un servicio se puede cambiar, pero nunca a un valor negativo o vacio. */
class ServicioTest {

    private Servicio corte() {
        return new Servicio("Corte", null, 30, new BigDecimal("12000"));
    }

    @Test
    void cambiaElPrecio() {
        Servicio servicio = corte();

        servicio.cambiarPrecio(new BigDecimal("15500"));

        assertThat(servicio.getPrecio()).isEqualByComparingTo("15500");
    }

    @Test
    void noAceptaUnPrecioNegativoNiVacioYConservaElAnterior() {
        Servicio servicio = corte();

        assertThatThrownBy(() -> servicio.cambiarPrecio(new BigDecimal("-1"))).isInstanceOf(DatoInvalidoException.class);
        assertThatThrownBy(() -> servicio.cambiarPrecio(null)).isInstanceOf(DatoInvalidoException.class);
        assertThat(servicio.getPrecio()).isEqualByComparingTo("12000");
    }
}

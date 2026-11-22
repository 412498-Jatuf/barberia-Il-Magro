package com.ilmagro.barberia.interfaces.web;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/** El freno contra el spam corta despues del limite, por IP, y se libera al pasar la hora. */
class LimiteDePedidosFilterTest {

    private static class Reloj extends Clock {
        Instant ahora = Instant.parse("2026-11-23T12:00:00Z");
        public ZoneOffset getZone() { return ZoneOffset.UTC; }
        public Clock withZone(java.time.ZoneId z) { return this; }
        public Instant instant() { return ahora; }
    }

    private final Reloj reloj = new Reloj();
    private final LimiteDePedidosFilter filtro = new LimiteDePedidosFilter(3, 30, 5, reloj);

    private int reservarDesde(String ip) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/turnos");
        req.setRemoteAddr(ip);
        MockHttpServletResponse res = new MockHttpServletResponse();
        filtro.doFilter(req, res, new MockFilterChain());
        return res.getStatus();
    }

    @Test
    void cortaLasReservasDeUnaIpDespuesDelLimite() throws Exception {
        assertThat(reservarDesde("1.1.1.1")).isEqualTo(200);
        assertThat(reservarDesde("1.1.1.1")).isEqualTo(200);
        assertThat(reservarDesde("1.1.1.1")).isEqualTo(200);
        assertThat(reservarDesde("1.1.1.1")).isEqualTo(429);

        // otra IP no se ve afectada
        assertThat(reservarDesde("2.2.2.2")).isEqualTo(200);

        // pasada la hora, vuelve a poder
        reloj.ahora = reloj.ahora.plus(Duration.ofMinutes(61));
        assertThat(reservarDesde("1.1.1.1")).isEqualTo(200);
    }

    @Test
    void cortaLosIntentosDeLoginDeUnaIp() throws Exception {
        for (int i = 0; i < 5; i++) {
            assertThat(loginDesde("3.3.3.3")).isEqualTo(200);
        }
        assertThat(loginDesde("3.3.3.3")).isEqualTo(429);
        assertThat(loginDesde("4.4.4.4")).isEqualTo(200);
    }

    private int loginDesde(String ip) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/admin/login");
        req.setRemoteAddr(ip);
        MockHttpServletResponse res = new MockHttpServletResponse();
        filtro.doFilter(req, res, new MockFilterChain());
        return res.getStatus();
    }

    @Test
    void noFrenaLoQueNoEsReservaNiCancelacion() throws Exception {
        for (int i = 0; i < 20; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/turnos/disponibles");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filtro.doFilter(req, res, new MockFilterChain());
            assertThat(res.getStatus()).isEqualTo(200);
        }
    }
}

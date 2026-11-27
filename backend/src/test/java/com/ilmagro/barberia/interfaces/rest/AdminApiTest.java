package com.ilmagro.barberia.interfaces.rest;

import com.ilmagro.barberia.domain.catalogo.BarberoRepository;
import com.ilmagro.barberia.domain.catalogo.ServicioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * El panel de Luca con pedidos HTTP reales: sin sesion no se puede nada, el login valida la clave, los
 * cambios exigen el token CSRF, y lo que Luca hace (cancelar, cambiar horario, cerrar dias) se refleja
 * en lo que ve el cliente.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AdminApiTest {

    // Lunes 23/11/2026 08:00 en Buenos Aires; se trabaja con el viernes 27 y el sabado 28
    static final Instant AHORA = ZonedDateTime.of(2026, 11, 23, 8, 0, 0, 0,
            ZoneId.of("America/Argentina/Buenos_Aires")).toInstant();

    static final String SEMANA_NORMAL = """
            {"dias":[{"dia":1,"tramos":[]},{"dia":2,"tramos":[{"desde":"15:00","hasta":"22:00"}]},
            {"dia":3,"tramos":[{"desde":"15:00","hasta":"22:00"}]},{"dia":4,"tramos":[{"desde":"15:00","hasta":"22:00"}]},
            {"dia":5,"tramos":[{"desde":"15:00","hasta":"22:00"}]},{"dia":6,"tramos":[{"desde":"15:00","hasta":"22:00"}]},
            {"dia":7,"tramos":[]}]}""";

    @TestConfiguration
    static class RelojFijo {
        @Bean
        @Primary
        Clock relojFijo() {
            return Clock.fixed(AHORA, ZoneOffset.UTC);
        }
    }

    @Value("${local.server.port}")
    int puerto;
    @Autowired
    BarberoRepository barberos;
    @Autowired
    ServicioRepository servicios;

    CookieManager cookies;
    HttpClient http;

    @BeforeEach
    void cliente() {
        cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        http = HttpClient.newBuilder().cookieHandler(cookies).build();
    }

    @Test
    void sinSesionNoSePuedeEntrarAlPanel() throws Exception {
        assertThat(pedir("GET", "/api/admin/turnos", null, false).statusCode()).isEqualTo(401);
        assertThat(pedir("GET", "/api/admin/semana", null, false).statusCode()).isEqualTo(401);
        assertThat(pedir("PUT", "/api/admin/semana", SEMANA_NORMAL, false).statusCode()).isIn(401, 403);
        // lo publico sigue siendo publico
        assertThat(pedir("GET", "/api/horario", null, false).statusCode()).isEqualTo(200);
    }

    @Test
    void unaClaveIncorrectaNoEntraYNoDiceCualFallo() throws Exception {
        HttpResponse<String> mala = pedir("POST", "/api/admin/login",
                "{\"usuario\":\"luca-test\",\"clave\":\"clave-equivocada\"}", false);
        HttpResponse<String> inexistente = pedir("POST", "/api/admin/login",
                "{\"usuario\":\"otro\",\"clave\":\"clave-de-prueba-123\"}", false);

        assertThat(mala.statusCode()).isEqualTo(401);
        assertThat(mala.body()).isEqualTo(inexistente.body()).contains("Usuario o clave incorrectos");
        assertThat(pedir("GET", "/api/admin/turnos", null, false).statusCode()).isEqualTo(401);
    }

    @Test
    void lucaVeLosTurnosYLosCancelaLiberandoElHorario() throws Exception {
        iniciarSesion();
        String turno = reservarComoCliente("2026-11-27", "21:00");
        long id = id(turno);
        assertThat(horariosLibres("2026-11-27")).doesNotContain("\"21:00\"");

        HttpResponse<String> lista = pedir("GET", "/api/admin/turnos?desde=2026-11-27&hasta=2026-11-27", null, false);
        assertThat(lista.statusCode()).isEqualTo(200);
        assertThat(lista.body()).contains("Ana Gomez").contains("351 555 0101").contains("\"cancelable\":true");

        // cambiar algo sin el token CSRF se rechaza; con el token, se cancela
        assertThat(pedir("POST", "/api/admin/turnos/" + id + "/cancelar", null, false).statusCode()).isEqualTo(403);
        HttpResponse<String> cancelado = pedir("POST", "/api/admin/turnos/" + id + "/cancelar", null, true);
        assertThat(cancelado.statusCode()).isEqualTo(200);
        assertThat(cancelado.body()).contains("CANCELADO");
        assertThat(horariosLibres("2026-11-27")).contains("\"21:00\"");
    }

    @Test
    void unaPausaEnElHorarioSeVeEnLasReservasYSeAvisanLosTurnosQueQuedanAfuera() throws Exception {
        iniciarSesion();
        String turno = reservarComoCliente("2026-11-27", "17:00");
        try {
            String conPausa = SEMANA_NORMAL.replace("{\"dia\":5,\"tramos\":[{\"desde\":\"15:00\",\"hasta\":\"22:00\"}]}",
                    "{\"dia\":5,\"tramos\":[{\"desde\":\"15:00\",\"hasta\":\"17:00\"},{\"desde\":\"18:00\",\"hasta\":\"22:00\"}]}");
            HttpResponse<String> guardado = pedir("PUT", "/api/admin/semana", conPausa, true);

            assertThat(guardado.statusCode()).isEqualTo(200);
            // el turno de las 17:00 ya estaba reservado y ahora cae en la pausa: se avisa, no se cancela
            assertThat(guardado.body()).contains("\"turnosAfectados\":[{").contains("Ana Gomez");
            assertThat(pedir("GET", "/api/admin/turnos?desde=2026-11-27&hasta=2026-11-27", null, false).body())
                    .contains("\"estado\":\"CONFIRMADO\"");
            assertThat(horariosLibres("2026-11-27")).contains("\"16:30\"", "\"18:00\"")
                    .doesNotContain("\"17:00\"", "\"17:30\"");
            assertThat(pedir("GET", "/api/horario", null, false).body())
                    .contains("{\"dia\":5,\"tramos\":[{\"desde\":\"15:00\",\"hasta\":\"17:00\"},{\"desde\":\"18:00\",\"hasta\":\"22:00\"}]}");
        } finally {
            pedir("PUT", "/api/admin/semana", SEMANA_NORMAL, true);
            pedir("POST", "/api/admin/turnos/" + id(turno) + "/cancelar", null, true);
        }
        assertThat(horariosLibres("2026-11-27")).contains("\"17:00\"");
    }

    @Test
    void unHorarioConTramosSuperpuestosSeRechaza() throws Exception {
        iniciarSesion();
        String superpuesto = SEMANA_NORMAL.replace("{\"dia\":5,\"tramos\":[{\"desde\":\"15:00\",\"hasta\":\"22:00\"}]}",
                "{\"dia\":5,\"tramos\":[{\"desde\":\"15:00\",\"hasta\":\"18:00\"},{\"desde\":\"17:00\",\"hasta\":\"22:00\"}]}");

        HttpResponse<String> res = pedir("PUT", "/api/admin/semana", superpuesto, true);

        assertThat(res.statusCode()).isEqualTo(400);
        assertThat(res.body()).contains("se superponen");
        assertThat(horariosLibres("2026-11-27")).contains("\"17:00\"");
    }

    @Test
    void cerrarHorasOUnDiaSacaLosHorariosYAvisaLosTurnosReservados() throws Exception {
        iniciarSesion();
        String turno = reservarComoCliente("2026-11-28", "20:00");
        long bloqueoHoras = 0;
        try {
            HttpResponse<String> horas = pedir("POST", "/api/admin/bloqueos",
                    "{\"fecha\":\"2026-11-28\",\"desde\":\"19:30\",\"hasta\":\"21:00\",\"motivo\":\"Tramite\"}", true);
            assertThat(horas.statusCode()).isEqualTo(201);
            assertThat(horas.body()).contains("Ana Gomez"); // el turno de las 20:00 cae en el cierre
            bloqueoHoras = id(horas.body());
            assertThat(horariosLibres("2026-11-28")).contains("\"19:00\"", "\"21:00\"")
                    .doesNotContain("\"19:30\"", "\"20:30\"");
            assertThat(pedir("GET", "/api/horario", null, false).body()).contains("\"cerrados\":[]");
            assertThat(pedir("DELETE", "/api/admin/bloqueos/" + bloqueoHoras, null, true).statusCode()).isEqualTo(204);

            HttpResponse<String> dia = pedir("POST", "/api/admin/bloqueos",
                    "{\"fecha\":\"2026-11-28\",\"motivo\":\"Feriado\"}", true);
            assertThat(dia.statusCode()).isEqualTo(201);
            assertThat(horariosLibres("2026-11-28")).isEqualTo("[]");
            assertThat(pedir("GET", "/api/horario", null, false).body()).contains("\"cerrados\":[\"2026-11-28\"]");
            assertThat(pedir("DELETE", "/api/admin/bloqueos/" + id(dia.body()), null, true).statusCode()).isEqualTo(204);
        } finally {
            pedir("POST", "/api/admin/turnos/" + id(turno) + "/cancelar", null, true);
        }
        assertThat(horariosLibres("2026-11-28")).contains("\"19:00\"", "\"20:00\"");
    }

    @Test
    void sinSesionNoSePuedeCambiarUnPrecio() throws Exception {
        Long id = servicios.buscarTodos().getFirst().getId();
        BigDecimal original = servicios.buscarTodos().getFirst().getPrecio();

        assertThat(pedir("PUT", "/api/admin/servicios/" + id + "/precio", "{\"precio\":1}", false).statusCode())
                .isIn(401, 403);
        assertThat(servicios.buscarPorId(id).orElseThrow().getPrecio()).isEqualByComparingTo(original);
    }

    @Test
    void lucaCambiaElPrecioYLosClientesVenElNuevo() throws Exception {
        iniciarSesion();
        Long id = servicios.buscarTodos().getFirst().getId();
        BigDecimal original = servicios.buscarTodos().getFirst().getPrecio();
        String ruta = "/api/admin/servicios/" + id + "/precio";
        String turno = null;
        try {
            // sin el token CSRF se rechaza; con el token, se guarda
            assertThat(pedir("PUT", ruta, "{\"precio\":9500}", false).statusCode()).isEqualTo(403);
            HttpResponse<String> guardado = pedir("PUT", ruta, "{\"precio\":9500}", true);
            assertThat(guardado.statusCode()).isEqualTo(200);
            assertThat(guardado.body()).contains("\"id\":" + id).contains("9500");

            // lo ve cualquiera en la lista publica y lo paga quien reserva desde ahora
            assertThat(pedir("GET", "/api/servicios", null, false).body()).contains("9500");
            turno = reservarComoCliente("2026-11-27", "21:00");
            assertThat(turno).contains("\"precio\":9500");
        } finally {
            if (turno != null) pedir("POST", "/api/admin/turnos/" + id(turno) + "/cancelar", null, true);
            pedir("PUT", ruta, "{\"precio\":" + original.toPlainString() + "}", true);
        }
        assertThat(servicios.buscarPorId(id).orElseThrow().getPrecio()).isEqualByComparingTo(original);
    }

    @Test
    void unPrecioInvalidoOUnServicioInexistenteSeRechazan() throws Exception {
        iniciarSesion();
        Long id = servicios.buscarTodos().getFirst().getId();
        BigDecimal original = servicios.buscarTodos().getFirst().getPrecio();
        String ruta = "/api/admin/servicios/" + id + "/precio";

        for (String malo : new String[]{"{\"precio\":0}", "{\"precio\":-500}", "{\"precio\":123456789}",
                "{\"precio\":1500.555}", "{}", "{\"precio\":\"mucho\"}"}) {
            assertThat(pedir("PUT", ruta, malo, true).statusCode()).as(malo).isEqualTo(400);
        }
        assertThat(pedir("PUT", "/api/admin/servicios/999999/precio", "{\"precio\":5000}", true).statusCode())
                .isEqualTo(404);
        assertThat(servicios.buscarPorId(id).orElseThrow().getPrecio()).isEqualByComparingTo(original);
    }

    // ---------- ayudas ----------

    private void iniciarSesion() throws Exception {
        HttpResponse<String> res = pedir("POST", "/api/admin/login",
                "{\"usuario\":\"luca-test\",\"clave\":\"clave-de-prueba-123\"}", false);
        assertThat(res.statusCode()).isEqualTo(200);
        assertThat(pedir("GET", "/api/admin/yo", null, false).body()).contains("luca-test");
    }

    private String reservarComoCliente(String fecha, String hora) throws Exception {
        Long barberoId = barberos.buscarTodos().getFirst().getId();
        Long corteId = servicios.buscarTodos().getFirst().getId();
        HttpResponse<String> res = pedir("POST", "/api/turnos", """
                {"clienteNombre":"Ana Gomez","clienteTelefono":"351 555 0101","clienteEmail":null,
                 "servicioId":%d,"barberoId":%d,"fecha":"%s","hora":"%s","notas":null}"""
                .formatted(corteId, barberoId, fecha, hora), false);
        assertThat(res.statusCode()).isEqualTo(201);
        return res.body();
    }

    private String horariosLibres(String fecha) throws Exception {
        Long barberoId = barberos.buscarTodos().getFirst().getId();
        Long corteId = servicios.buscarTodos().getFirst().getId();
        return pedir("GET", "/api/turnos/disponibles?fecha=" + fecha + "&barberoId=" + barberoId
                + "&servicioId=" + corteId, null, false).body();
    }

    private static long id(String json) {
        Matcher m = Pattern.compile("\"id\":(\\d+)").matcher(json);
        assertThat(m.find()).isTrue();
        return Long.parseLong(m.group(1));
    }

    /** conCsrf: manda el token de la cookie XSRF-TOKEN en X-XSRF-TOKEN, como hace Angular. */
    private HttpResponse<String> pedir(String metodo, String ruta, String json, boolean conCsrf) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .header("Content-Type", "application/json");
        if (conCsrf) {
            cookies.getCookieStore().getCookies().stream().filter(c -> c.getName().equals("XSRF-TOKEN")).findFirst()
                    .ifPresent(c -> req.header("X-XSRF-TOKEN", c.getValue()));
        }
        req.method(metodo, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json));
        return http.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }
}

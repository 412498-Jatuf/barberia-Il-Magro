package com.ilmagro.barberia.interfaces.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Freno contra el spam: limita cuantas reservas y cancelaciones puede hacer una misma IP en un rato.
 * Sin esto, un script podria llenar la agenda de Luca con turnos falsos o probar codigos de cancelacion.
 *
 * <p>Reservas: barberia.seguridad.reservas-por-hora (por defecto 5 por hora).
 * Cancelaciones (ver y cancelar): barberia.seguridad.cancelaciones-por-hora (por defecto 30 por hora).
 * Intentos de login al panel: barberia.seguridad.logins-por-hora (por defecto 10 por hora).
 * Para limitar otra ruta alcanza con sumar una {@link Regla} a la lista.
 *
 * <p>La IP es la de la conexion. Detras de un proxy (Nginx, Cloudflare, Render...) hay que poner
 * FORWARD_HEADERS=framework para que Spring use la IP real del cliente; sin proxy NO, porque
 * cualquiera podria inventar el encabezado X-Forwarded-For y saltarse el limite.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class LimiteDePedidosFilter extends OncePerRequestFilter {

    /** Una ruta limitada: a que pedidos aplica, cuantos por hora deja pasar y que se le dice al que se pasa. */
    record Regla(String nombre, Predicate<HttpServletRequest> aplica, int limite, String mensaje) {
    }

    private final List<Regla> reglas;
    private final VentanaDeslizante ventana;

    public LimiteDePedidosFilter(@Value("${barberia.seguridad.reservas-por-hora:5}") int reservasPorHora,
                                 @Value("${barberia.seguridad.cancelaciones-por-hora:30}") int cancelacionesPorHora,
                                 @Value("${barberia.seguridad.logins-por-hora:10}") int loginsPorHora,
                                 Clock clock) {
        this.reglas = List.of(
                new Regla("reserva",
                        req -> "POST".equals(req.getMethod()) && req.getRequestURI().equals("/api/turnos"),
                        reservasPorHora,
                        "Hiciste muchas reservas seguidas. Si necesitás otro turno, escribinos por WhatsApp."),
                new Regla("cancelacion",
                        req -> req.getRequestURI().startsWith("/api/turnos/cancelacion/"),
                        cancelacionesPorHora,
                        "Demasiados intentos. Probá de nuevo más tarde."),
                new Regla("login",
                        req -> "POST".equals(req.getMethod()) && req.getRequestURI().equals("/api/admin/login"),
                        loginsPorHora,
                        "Demasiados intentos de ingreso. Probá de nuevo más tarde."));
        this.ventana = new VentanaDeslizante(Duration.ofHours(1), clock);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        Optional<Regla> regla = reglas.stream().filter(r -> r.aplica().test(req)).findFirst();
        if (regla.isPresent() && !ventana.permitir(regla.get().nombre() + "|" + req.getRemoteAddr(), regla.get().limite())) {
            responderDemasiados(res, regla.get().mensaje());
            return;
        }
        chain.doFilter(req, res);
    }

    private void responderDemasiados(HttpServletResponse res, String mensaje) throws IOException {
        res.setStatus(429);
        res.setHeader("Retry-After", String.valueOf(ventana.ventana().toSeconds()));
        res.setContentType("application/json;charset=UTF-8");
        // mismo formato que ApiExceptionHandler; el mensaje es un texto fijo nuestro, no viene del usuario
        res.getWriter().write("{\"mensaje\":\"" + mensaje + "\",\"campos\":{}}");
    }
}

package com.ilmagro.barberia.interfaces.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Encabezados de seguridad en todas las respuestas (el sitio y la API):
 * <ul>
 *   <li>Referrer-Policy: no-referrer, para que el link secreto de cancelacion (/cancelar/...) no se
 *       filtre a otros sitios (Google Maps, WhatsApp, las fuentes) cuando se sale de la pagina.</li>
 *   <li>X-Content-Type-Options y X-Frame-Options / frame-ancestors: nadie puede meter el sitio
 *       dentro de otra pagina para engañar a alguien con clics (clickjacking).</li>
 *   <li>Strict-Transport-Security: una vez que el navegador entro por HTTPS, siempre usa HTTPS.</li>
 *   <li>La API no se guarda en caches compartidos (tiene datos de turnos).</li>
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EncabezadosDeSeguridadFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        res.setHeader("Referrer-Policy", "no-referrer");
        res.setHeader("X-Content-Type-Options", "nosniff");
        res.setHeader("X-Frame-Options", "DENY");
        res.setHeader("Content-Security-Policy", "frame-ancestors 'none'; base-uri 'self'; object-src 'none'");
        res.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=(), payment=()");
        if (req.isSecure()) {
            res.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        }
        if (req.getRequestURI().startsWith("/api/")) {
            res.setHeader("Cache-Control", "no-store");
        }
        chain.doFilter(req, res);
    }
}

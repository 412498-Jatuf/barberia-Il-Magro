package com.ilmagro.barberia.infrastructure.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Seguridad del sitio: todo es publico salvo /api/admin/**, que pide haber iniciado sesion.
 *
 * <ul>
 *   <li>Sesion por cookie (http-only, same-site strict). El login lo hace {@code AdminAuthController}.</li>
 *   <li>CSRF: los pedidos que cambian algo en el panel llevan el token de la cookie XSRF-TOKEN en el
 *       encabezado X-XSRF-TOKEN (Angular lo hace solo). La reserva y la cancelacion por link de los
 *       clientes son publicas y no llevan sesion, por eso no lo usan.</li>
 *   <li>Las claves se guardan con BCrypt en memoria; nunca se escriben en el codigo ni en la base.</li>
 * </ul>
 */
@Configuration
public class SeguridadConfig {

    private static final Logger log = LoggerFactory.getLogger(SeguridadConfig.class);

    @Bean
    SecurityFilterChain cadenaDeSeguridad(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(cookieCsrf())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/api/turnos/**", "/api/admin/login"))
                .addFilterAfter(new CookieCsrfFilter(), BasicAuthenticationFilter.class)
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/admin/login").permitAll()
                        .requestMatchers("/api/admin/**").authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .logout(l -> l.logoutUrl("/api/admin/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()))
                // los encabezados y el cache los pone EncabezadosDeSeguridadFilter
                .headers(h -> h.cacheControl(c -> c.disable()))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }

    private static CookieCsrfTokenRepository cookieCsrf() {
        CookieCsrfTokenRepository repo = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repo.setCookieCustomizer(c -> c.sameSite("Strict"));
        return repo;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService usuariosDelPanel(AdminProperties admin, PasswordEncoder encoder) {
        List<UserDetails> usuarios = new ArrayList<>();
        agregar(usuarios, admin.usuario1(), admin.clave1(), encoder);
        agregar(usuarios, admin.usuario2(), admin.clave2(), encoder);
        if (usuarios.isEmpty()) {
            log.warn("Panel de administracion DESHABILITADO: faltan ADMIN_USUARIO_1 y ADMIN_CLAVE_1 en el .env");
        } else {
            log.info("Panel de administracion habilitado para: {}", usuarios.stream().map(UserDetails::getUsername).toList());
        }
        return new InMemoryUserDetailsManager(usuarios);
    }

    private static void agregar(List<UserDetails> usuarios, String usuario, String clave, PasswordEncoder encoder) {
        if (usuario == null || usuario.isBlank() || clave == null || clave.isBlank()) {
            return;
        }
        if (clave.length() < AdminProperties.CLAVE_MINIMA) {
            log.error("La clave de '{}' tiene menos de {} caracteres: ese usuario NO se habilita", usuario,
                    AdminProperties.CLAVE_MINIMA);
            return;
        }
        usuarios.add(User.withUsername(usuario.trim()).password(encoder.encode(clave)).roles("ADMIN").build());
    }

    @Bean
    AuthenticationManager authenticationManager(UserDetailsService usuarios, PasswordEncoder encoder) {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(usuarios);
        proveedor.setPasswordEncoder(encoder);
        return new ProviderManager(proveedor);
    }

    /**
     * Spring Security solo arma el token CSRF cuando alguien lo pide; esto lo pide en cada respuesta para
     * que el navegador siempre tenga la cookie XSRF-TOKEN antes de su primer pedido que cambia algo.
     */
    static final class CookieCsrfFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                throws ServletException, IOException {
            CsrfToken token = (CsrfToken) req.getAttribute(CsrfToken.class.getName());
            if (token != null) {
                token.getToken();
            }
            chain.doFilter(req, res);
        }
    }
}

package com.ilmagro.barberia.interfaces.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Entrar al panel de administracion y saber si la sesion sigue abierta. (Salir lo hace Spring Security.) */
@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    public record LoginRequest(@NotBlank @Size(max = 100) String usuario, @NotBlank @Size(max = 200) String clave) {
    }

    public record Sesion(String usuario) {
    }

    private final AuthenticationManager autenticador;
    private final SecurityContextRepository sesiones = new HttpSessionSecurityContextRepository();

    public AdminAuthController(AuthenticationManager autenticador) {
        this.autenticador = autenticador;
    }

    /** Un usuario o clave incorrectos terminan en {@code ApiExceptionHandler} (401, sin decir cual de los dos fallo). */
    @PostMapping("/login")
    public Sesion login(@Valid @RequestBody LoginRequest login, HttpServletRequest req, HttpServletResponse res) {
        Authentication autenticado = autenticador.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(login.usuario().trim(), login.clave()));
        if (req.getSession(false) != null) {
            req.changeSessionId(); // una sesion nueva al entrar, para que no se pueda fijar de antemano
        }
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticado);
        SecurityContextHolder.setContext(contexto);
        sesiones.saveContext(contexto, req, res);
        return new Sesion(autenticado.getName());
    }

    @GetMapping("/yo")
    public Sesion yo(Authentication autenticado) {
        return new Sesion(autenticado.getName());
    }
}

package com.ilmagro.barberia.infrastructure.seguridad;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Quienes pueden entrar al panel /admin (barberia.admin.* en application.properties). Son dos lugares,
 * con el mismo permiso: Luca y el dueño de la pagina. Las claves NO estan en el codigo ni en el repo:
 * van en backend/.env (ADMIN_USUARIO_1, ADMIN_CLAVE_1, ADMIN_USUARIO_2, ADMIN_CLAVE_2).
 * Un usuario sin clave, o con una clave de menos de {@value #CLAVE_MINIMA} caracteres, no se habilita.
 */
@ConfigurationProperties("barberia.admin")
public record AdminProperties(String usuario1, String clave1, String usuario2, String clave2) {

    public static final int CLAVE_MINIMA = 10;
}

package com.ilmagro.barberia.interfaces.rest;

import com.ilmagro.barberia.application.catalogo.AdministracionCatalogoService;
import com.ilmagro.barberia.interfaces.rest.dto.AdminDtos.PrecioRequest;
import com.ilmagro.barberia.interfaces.rest.dto.CatalogoDtos.ServicioResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Catalogo en el panel de Luca. Pide sesion iniciada (todo /api/admin/** la pide, ver SeguridadConfig).
 * La lista de servicios con sus precios es la publica: GET /api/servicios.
 */
@RestController
@RequestMapping("/api/admin/servicios")
public class AdminCatalogoController {

    private final AdministracionCatalogoService catalogo;

    public AdminCatalogoController(AdministracionCatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    /** Cambia el precio de un servicio; las reservas nuevas ya usan el precio nuevo. */
    @PutMapping("/{id}/precio")
    public ServicioResponse cambiarPrecio(@PathVariable Long id, @Valid @RequestBody PrecioRequest request) {
        return ServicioResponse.de(catalogo.cambiarPrecio(id, request.precio()));
    }
}

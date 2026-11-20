package com.ilmagro.barberia.interfaces.rest;

import com.ilmagro.barberia.application.catalogo.CatalogoService;
import com.ilmagro.barberia.interfaces.rest.dto.CatalogoDtos.BarberoResponse;
import com.ilmagro.barberia.interfaces.rest.dto.CatalogoDtos.CorteResponse;
import com.ilmagro.barberia.interfaces.rest.dto.CatalogoDtos.ServicioResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoService catalogo;

    public CatalogoController(CatalogoService catalogo) {
        this.catalogo = catalogo;
    }

    @GetMapping("/cortes")
    public List<CorteResponse> cortes() {
        return catalogo.listarCortes().stream().map(CorteResponse::de).toList();
    }

    @GetMapping("/servicios")
    public List<ServicioResponse> servicios() {
        return catalogo.listarServicios().stream().map(ServicioResponse::de).toList();
    }

    @GetMapping("/barberos")
    public List<BarberoResponse> barberos() {
        return catalogo.listarBarberos().stream().map(BarberoResponse::de).toList();
    }
}

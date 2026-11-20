package com.ilmagro.barberia.interfaces.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * En produccion Spring Boot sirve el build de Angular desde /static.
 * Las rutas del frontend se redirigen a index.html para que las resuelva el router de Angular.
 */
@Controller
public class SpaController {

    @GetMapping({"/turnos", "/turnos/", "/cancelar/{codigo}"})
    public String spa() {
        return "forward:/index.html";
    }
}

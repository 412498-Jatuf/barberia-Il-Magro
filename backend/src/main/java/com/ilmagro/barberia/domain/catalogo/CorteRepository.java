package com.ilmagro.barberia.domain.catalogo;

import java.util.List;

/** Puerto de persistencia de cortes (lo implementa la infraestructura). */
public interface CorteRepository {

    List<Corte> buscarTodos();

    Corte guardar(Corte corte);

    long contar();
}

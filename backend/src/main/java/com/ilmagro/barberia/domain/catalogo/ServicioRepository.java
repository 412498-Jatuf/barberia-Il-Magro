package com.ilmagro.barberia.domain.catalogo;

import java.util.List;
import java.util.Optional;

public interface ServicioRepository {

    List<Servicio> buscarTodos();

    Optional<Servicio> buscarPorId(Long id);

    Servicio guardar(Servicio servicio);

    long contar();
}

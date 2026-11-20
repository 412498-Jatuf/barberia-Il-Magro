package com.ilmagro.barberia.domain.catalogo;

import java.util.List;
import java.util.Optional;

public interface BarberoRepository {

    List<Barbero> buscarTodos();

    Optional<Barbero> buscarPorId(Long id);

    /**
     * Obtiene el barbero bloqueando su agenda hasta que termine la transaccion,
     * para que dos reservas simultaneas no tomen el mismo horario.
     */
    Optional<Barbero> buscarParaReservar(Long id);

    boolean existe(Long id);

    Barbero guardar(Barbero barbero);

    long contar();
}

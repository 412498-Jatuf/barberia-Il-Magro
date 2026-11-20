package com.ilmagro.barberia.domain.agenda;

import com.ilmagro.barberia.domain.catalogo.Barbero;
import com.ilmagro.barberia.domain.catalogo.Servicio;
import com.ilmagro.barberia.domain.shared.DatoInvalidoException;
import jakarta.persistence.*;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;

/** Aggregate root de la agenda: un turno reservado por un cliente con un barbero. */
@Entity
@Table(indexes = @Index(name = "idx_turno_barbero_fecha", columnList = "barbero_id, fecha"))
public class Turno {

    private static final SecureRandom AZAR = new SecureRandom();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Cliente cliente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Servicio servicio;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Barbero barbero;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTurno estado;

    @Column(length = 500)
    private String notas;

    @Column(nullable = false)
    private LocalDateTime creadoEn;

    /** Secreto del link de cancelacion: 24 caracteres al azar, imposible de adivinar. */
    @Column(length = 32, unique = true)
    private String codigoCancelacion;

    private LocalDateTime canceladoEn;

    protected Turno() {
    }

    private Turno(Cliente cliente, Servicio servicio, Barbero barbero, LocalDate fecha, LocalTime horaInicio,
                  String notas, LocalDateTime creadoEn) {
        this.cliente = cliente;
        this.servicio = servicio;
        this.barbero = barbero;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaInicio.plusMinutes(servicio.getDuracionMinutos());
        this.notas = notas;
        this.estado = EstadoTurno.CONFIRMADO;
        this.creadoEn = creadoEn;
        this.codigoCancelacion = nuevoCodigo();
    }

    private static String nuevoCodigo() {
        byte[] bytes = new byte[18];
        AZAR.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Crea un turno confirmado. La disponibilidad del horario la valida {@link AgendaService}
     * antes de llamar a este metodo.
     */
    public static Turno reservar(Cliente cliente, Servicio servicio, Barbero barbero, LocalDate fecha,
                                 LocalTime hora, String notas, LocalDateTime ahora) {
        if (cliente == null || servicio == null || barbero == null || fecha == null || hora == null) {
            throw new DatoInvalidoException("Faltan datos para reservar el turno");
        }
        String notasLimpias = notas == null || notas.isBlank() ? null : notas.trim();
        if (notasLimpias != null && notasLimpias.length() > 500) {
            throw new DatoInvalidoException("Las notas no pueden superar los 500 caracteres");
        }
        return new Turno(cliente, servicio, barbero, fecha, hora, notasLimpias, ahora);
    }

    /** Indica si este turno ocupa alguna parte del intervalo [inicio, fin). */
    public boolean ocupa(LocalTime inicio, LocalTime fin) {
        return estado == EstadoTurno.CONFIRMADO && inicio.isBefore(horaFin) && fin.isAfter(horaInicio);
    }

    /**
     * Cancela el turno y libera su horario. Cancelar dos veces no hace nada; un turno que ya empezo
     * o paso no se puede cancelar.
     */
    public void cancelar(LocalDateTime ahora) {
        if (estado == EstadoTurno.CANCELADO) {
            return;
        }
        if (!sePuedeCancelar(ahora)) {
            throw new DatoInvalidoException("El turno ya pasó, no se puede cancelar");
        }
        this.estado = EstadoTurno.CANCELADO;
        this.canceladoEn = ahora;
    }

    /** Confirmado y todavia no empezo. */
    public boolean sePuedeCancelar(LocalDateTime ahora) {
        return estado == EstadoTurno.CONFIRMADO && fecha.atTime(horaInicio).isAfter(ahora);
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Servicio getServicio() { return servicio; }
    public Barbero getBarbero() { return barbero; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public EstadoTurno getEstado() { return estado; }
    public String getNotas() { return notas; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public String getCodigoCancelacion() { return codigoCancelacion; }
    public LocalDateTime getCanceladoEn() { return canceladoEn; }
}

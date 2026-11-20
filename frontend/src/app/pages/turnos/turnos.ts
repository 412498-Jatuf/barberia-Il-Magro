import { Component, DestroyRef, OnInit, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription, forkJoin } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { BARBERIA } from '../../core/barberia.config';
import { aIsoLocal, diaIso, fechaLarga, formatearPrecio } from '../../core/formato';
import { ApiError, Barbero, Horario, Servicio, TurnoReservado } from '../../core/models';
import { linkWhatsapp, mensajeTurno } from '../../core/mensaje-turno';

interface DiaOpcion {
  iso: string;
  diaSemana: string;
  numero: number;
  mes: string;
}

@Component({
  selector: 'app-turnos',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './turnos.html',
  styleUrl: './turnos.scss',
})
export class Turnos implements OnInit {
  private readonly api = inject(ApiService);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  /** ?servicio=ID desde la landing (bindeado por withComponentInputBinding). */
  readonly servicio = input<string>();

  protected readonly b = BARBERIA;
  protected readonly precio = formatearPrecio;
  protected readonly fechaLarga = fechaLarga;

  protected readonly cargandoDatos = signal(true);
  protected readonly errorDatos = signal(false);
  protected readonly servicios = signal<Servicio[]>([]);
  protected readonly barberos = signal<Barbero[]>([]);
  protected readonly horario = signal<Horario | null>(null);

  protected readonly servicioSel = signal<Servicio | null>(null);
  protected readonly barberoSel = signal<Barbero | null>(null);
  protected readonly variosBarberos = computed(() => this.barberos().length > 1);
  protected readonly fechaSel = signal<string | null>(null);
  protected readonly horaSel = signal<string | null>(null);

  protected readonly horarios = signal<string[]>([]);
  protected readonly cargandoHorarios = signal(false);
  protected readonly errorHorarios = signal(false);

  protected readonly enviando = signal(false);
  protected readonly errorEnvio = signal<string | null>(null);
  protected readonly confirmado = signal<TurnoReservado | null>(null);
  /** El chat de WhatsApp con Luca y el turno ya escrito (se arma al confirmar). */
  protected readonly linkTurno = signal<string | null>(null);
  /** Si el navegador dejo abrir WhatsApp solo; si no, la confirmacion muestra el boton primero. */
  protected readonly wspAbierto = signal(false);

  private horariosSub?: Subscription;

  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    telefono: ['', [Validators.required, Validators.pattern(/^[+]?[0-9 ()-]{6,20}$/)]],
    email: ['', [Validators.email, Validators.maxLength(150)]],
    notas: ['', [Validators.maxLength(500)]],
  });

  /** Proximos dias en los que la barberia atiende. */
  protected readonly dias = computed<DiaOpcion[]>(() => {
    const h = this.horario();
    if (!h) return [];
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const opciones: DiaOpcion[] = [];
    for (let i = 0; i <= h.diasAnticipacion; i++) {
      const d = new Date(hoy);
      d.setDate(hoy.getDate() + i);
      if (!h.diasAtencion.includes(diaIso(d))) continue;
      opciones.push({
        iso: aIsoLocal(d),
        diaSemana: i === 0 ? 'Hoy' : i === 1 ? 'Mañana' : d.toLocaleDateString('es-AR', { weekday: 'short' }),
        numero: d.getDate(),
        mes: d.toLocaleDateString('es-AR', { month: 'short' }),
      });
    }
    return opciones;
  });

  protected readonly listoParaConfirmar = computed(
    () => !!(this.servicioSel() && this.barberoSel() && this.fechaSel() && this.horaSel()),
  );

  ngOnInit(): void {
    forkJoin({ servicios: this.api.servicios(), barberos: this.api.barberos(), horario: this.api.horario() })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ servicios, barberos, horario }) => {
          this.servicios.set(servicios);
          this.barberos.set(barberos);
          this.horario.set(horario);
          this.cargandoDatos.set(false);

          const preseleccion = servicios.find((s) => String(s.id) === this.servicio());
          if (preseleccion) this.servicioSel.set(preseleccion);
          if (barberos.length === 1) this.barberoSel.set(barberos[0]);
        },
        error: () => {
          this.cargandoDatos.set(false);
          this.errorDatos.set(true);
        },
      });
  }

  protected elegirServicio(s: Servicio): void {
    this.servicioSel.set(s);
    this.refrescarHorarios();
  }

  protected elegirBarbero(b: Barbero): void {
    this.barberoSel.set(b);
    this.refrescarHorarios();
  }

  protected elegirFecha(iso: string): void {
    this.fechaSel.set(iso);
    this.refrescarHorarios();
  }

  protected refrescarHorarios(): void {
    this.horaSel.set(null);
    this.horarios.set([]);
    this.errorHorarios.set(false);
    this.horariosSub?.unsubscribe();

    const s = this.servicioSel();
    const b = this.barberoSel();
    const f = this.fechaSel();
    if (!s || !b || !f) return;

    this.cargandoHorarios.set(true);
    this.horariosSub = this.api
      .horariosDisponibles(f, b.id, s.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (h) => {
          this.horarios.set(h);
          this.cargandoHorarios.set(false);
        },
        error: () => {
          this.errorHorarios.set(true);
          this.cargandoHorarios.set(false);
        },
      });
  }

  protected invalido(campo: 'nombre' | 'telefono' | 'email' | 'notas'): boolean {
    const c = this.form.controls[campo];
    return c.invalid && (c.touched || c.dirty);
  }

  protected confirmar(): void {
    this.form.markAllAsTouched();
    const s = this.servicioSel();
    const b = this.barberoSel();
    const f = this.fechaSel();
    const h = this.horaSel();
    if (this.form.invalid || !s || !b || !f || !h || this.enviando()) return;

    const v = this.form.getRawValue();
    this.enviando.set(true);
    this.errorEnvio.set(null);

    this.api
      .reservar({
        clienteNombre: v.nombre.trim(),
        clienteTelefono: v.telefono.trim(),
        clienteEmail: v.email.trim() || null,
        servicioId: s.id,
        barberoId: b.id,
        fecha: f,
        hora: h,
        notas: v.notas.trim() || null,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (turno) => {
          this.enviando.set(false);
          this.confirmado.set(turno);
          this.enviarALuca(turno, {
            telefono: v.telefono.trim(),
            email: v.email.trim() || null,
            notas: v.notas.trim() || null,
          }, s.duracionMinutos);
          window.scrollTo({ top: 0, behavior: 'smooth' });
        },
        error: (err: HttpErrorResponse) => {
          this.enviando.set(false);
          const api = err.error as ApiError | null;
          this.errorEnvio.set(api?.mensaje ?? 'No pudimos registrar el turno. Probá de nuevo en unos minutos.');
          if (err.status === 409) this.refrescarHorarios();
        },
      });
  }

  protected nuevoTurno(): void {
    this.confirmado.set(null);
    this.linkTurno.set(null);
    this.wspAbierto.set(false);
    this.horaSel.set(null);
    this.form.controls.notas.reset();
    this.refrescarHorarios();
  }

  /**
   * Abre WhatsApp con el mensaje del turno para Luca (el cliente solo toca Enviar). Se llama apenas
   * responde el backend, todavia dentro del permiso del click; si el navegador igual lo bloquea,
   * la confirmacion muestra el boton grande para abrirlo a mano.
   */
  private enviarALuca(turno: TurnoReservado, cliente: Parameters<typeof mensajeTurno>[1], duracion: number): void {
    if (!this.b.whatsapp) return;
    const url = linkWhatsapp(this.b.whatsapp, mensajeTurno(turno, cliente, duracion, this.b.nombre));
    this.linkTurno.set(url);
    const ventana = window.open(url, '_blank');
    if (ventana) ventana.opener = null;
    this.wspAbierto.set(!!ventana);
  }
}

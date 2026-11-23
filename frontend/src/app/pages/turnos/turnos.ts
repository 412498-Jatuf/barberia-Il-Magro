import { Component, DestroyRef, OnInit, computed, inject, input, linkedSignal, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { BARBERIA } from '../../core/barberia.config';
import { DatosService } from '../../core/datos.service';
import { proximosDias } from '../../core/dias-de-turno';
import { fechaLarga, formatearPrecio } from '../../core/formato';
import { DatosCliente, mensajeTurno } from '../../core/mensaje-turno';
import { ApiError, AvisoPor, Barbero, Servicio, TurnoReservado } from '../../core/models';
import { WhatsappService } from '../../core/whatsapp.service';

/**
 * Pagina de reserva: el cliente elige servicio, dia y hora, deja sus datos y al confirmar se le abre
 * WhatsApp con el turno escrito para Luca. Los datos de la barberia vienen de DatosService (cargados
 * una sola vez para todo el sitio); los dias salen de proximosDias y el chat lo abre WhatsappService.
 */
@Component({
  selector: 'app-turnos',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './turnos.html',
  styleUrl: './turnos.scss',
})
export class Turnos implements OnInit {
  private readonly api = inject(ApiService);
  private readonly datos = inject(DatosService);
  private readonly whatsapp = inject(WhatsappService);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  /** ?servicio=ID desde la landing (bindeado por withComponentInputBinding). */
  readonly servicio = input<string>();

  protected readonly b = BARBERIA;
  protected readonly precio = formatearPrecio;
  protected readonly fechaLarga = fechaLarga;

  protected readonly cargandoDatos = computed(() => !this.datos.listo());
  protected readonly errorDatos = this.datos.error;
  protected readonly servicios = this.datos.servicios;
  protected readonly barberos = this.datos.barberos;
  protected readonly variosBarberos = computed(() => this.barberos().length > 1);
  /** Proximos dias en los que la barberia atiende. */
  protected readonly dias = computed(() => {
    const h = this.datos.horario();
    return h ? proximosDias(h) : [];
  });

  /** Arranca con el servicio que vino de la landing; despues lo cambia el cliente. */
  protected readonly servicioSel = linkedSignal<Servicio | null>(
    () => this.servicios().find((s) => String(s.id) === this.servicio()) ?? null,
  );
  /** Si hay un solo barbero queda elegido de entrada. */
  protected readonly barberoSel = linkedSignal<Barbero | null>(() => {
    const todos = this.barberos();
    return todos.length === 1 ? todos[0] : null;
  });
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

  /** El canal con el que se confirmo el turno (para el mensaje de la confirmacion). */
  protected readonly avisoElegido = signal<AvisoPor>('whatsapp');

  private horariosSub?: Subscription;

  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    telefono: ['', [Validators.required, Validators.pattern(/^[+]?[0-9 ()-]{6,20}$/)]],
    email: ['', [Validators.email, Validators.maxLength(150)]],
    notas: ['', [Validators.maxLength(500)]],
    avisoPor: ['whatsapp' as AvisoPor],
  });

  /** Solo el nombre de pila del barbero ("Luca"), para hablarle al cliente sin el apellido. */
  protected readonly primerNombreBarbero = computed(() => (this.barberoSel()?.nombre ?? 'Luca').split(' ')[0]);

  protected readonly listoParaConfirmar = computed(
    () => !!(this.servicioSel() && this.barberoSel() && this.fechaSel() && this.horaSel()),
  );

  ngOnInit(): void {
    // la raiz ya los pide; si se entra directo a /turnos, esto no repite el pedido
    this.datos.cargar();
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

    const nombre = this.form.controls.nombre.value.trim();
    const cliente = this.datosCliente();
    const avisoPor = this.form.controls.avisoPor.value;
    this.enviando.set(true);
    this.errorEnvio.set(null);

    this.api
      .reservar({
        clienteNombre: nombre,
        clienteTelefono: cliente.telefono,
        clienteEmail: cliente.email,
        servicioId: s.id,
        barberoId: b.id,
        fecha: f,
        hora: h,
        notas: cliente.notas,
        avisoPor,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (turno) => {
          this.enviando.set(false);
          this.confirmado.set(turno);
          this.avisoElegido.set(avisoPor);
          if (avisoPor === 'whatsapp') this.enviarALuca(turno, cliente, s.duracionMinutos);
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

  /** Los datos del formulario ya limpios (sin espacios de mas, vacios como null). */
  private datosCliente(): DatosCliente {
    const v = this.form.getRawValue();
    return {
      telefono: v.telefono.trim(),
      email: v.email.trim() || null,
      notas: v.notas.trim() || null,
    };
  }

  /**
   * Abre WhatsApp con el mensaje del turno para Luca (el cliente solo toca Enviar). Se llama apenas
   * responde el backend, todavia dentro del permiso del click; si el navegador igual lo bloquea,
   * la confirmacion muestra el boton grande para abrirlo a mano.
   */
  private enviarALuca(turno: TurnoReservado, cliente: DatosCliente, duracion: number): void {
    if (!this.b.whatsapp) return;
    const chat = this.whatsapp.abrirChat(this.b.whatsapp, mensajeTurno(turno, cliente, duracion, this.b.nombre));
    this.linkTurno.set(chat.url);
    this.wspAbierto.set(chat.abierto);
  }
}

import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpErrorResponse } from '@angular/common/http';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AdminService } from '../../core/admin.service';
import { ApiError } from '../../core/models';

/** Ingreso al panel con usuario y clave. Al entrar, AdminService avisa y el panel se muestra solo. */
@Component({
  selector: 'app-admin-login',
  imports: [ReactiveFormsModule],
  templateUrl: './admin-login.html',
})
export class AdminLogin {
  private readonly admin = inject(AdminService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly verClave = signal(false);

  protected readonly form = inject(NonNullableFormBuilder).group({
    usuario: ['', [Validators.required, Validators.maxLength(100)]],
    clave: ['', [Validators.required, Validators.maxLength(200)]],
  });

  protected entrar(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.enviando()) return;
    const { usuario, clave } = this.form.getRawValue();
    this.enviando.set(true);
    this.error.set(null);
    this.admin
      .entrar(usuario.trim(), clave)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        error: (err: HttpErrorResponse) => {
          this.enviando.set(false);
          const api = err.error as ApiError | null;
          this.error.set(api?.mensaje ?? 'No pudimos iniciar sesión. Probá de nuevo en unos minutos.');
          this.form.controls.clave.reset();
        },
      });
  }
}

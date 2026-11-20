import { Injectable } from '@angular/core';

/**
 * Carga el kit de diseño (public/assets: system.js, motion.js, layout.js y los "moments")
 * DESPUES de que Angular renderizo la landing, porque esos scripts leen el DOM al iniciar.
 * La segunda vez (volver a la home desde /turnos) solo re-monta los moments y el motion.
 */
@Injectable({ providedIn: 'root' })
export class KitService {
  private cargado = false;

  iniciar(): void {
    if (!this.cargado) {
      this.cargado = true;
      for (const src of ['assets/system.js', 'assets/motion/motion.js', 'assets/layout/layout.js']) {
        const s = document.createElement('script');
        s.type = 'module';
        s.src = src;
        document.body.appendChild(s);
      }
      return;
    }
    void importar('/assets/motion/motion.js').then((m) => m.refresh?.());
    void importar('/assets/moments/moments.js').then((m) => m.mountAll?.(document));
  }

  desmontarMoments(): void {
    if (!this.cargado) return;
    void importar('/assets/moments/moments.js').then((m) => m.unmountAll?.(document));
  }
}

// import() de un archivo servido tal cual (no lo empaqueta el build de Angular).
function importar(url: string): Promise<any> {
  return import(/* @vite-ignore */ url);
}

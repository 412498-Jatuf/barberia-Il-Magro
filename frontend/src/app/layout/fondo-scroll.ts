import { Component, DestroyRef, ElementRef, afterNextRender, inject } from '@angular/core';

/**
 * Fondo animado con el scroll, detras de todo el contenido.
 *
 * - Pelitos cortados en un <canvas>: al bajar caen, al subir suben, y frenan solos al parar.
 *   Cada uno tiene su "profundidad" (los cercanos son mas grandes, oscuros y rapidos).
 * - Una maquina de cortar grande y tenue recorre la pantalla segun cuanto bajaste, zumba mientras
 *   se mueve y barre los pelitos que tiene cerca de la cuchilla.
 * - El loop solo corre mientras algo se mueve. Con "reducir movimiento" queda quieto.
 * - Montado sobre <div class="fondo-scroll"> del index.html (primer hijo del body).
 */
@Component({
  selector: 'app-fondo-scroll',
  template: `
    <canvas class="fondo__pelos"></canvas>
    <div class="fondo__maquina">
      <svg viewBox="0 0 28 96" aria-hidden="true">
        <rect x="13" y="-70" width="2" height="79" class="fq-cable" />
        <rect x="3" y="8" width="22" height="66" rx="9" class="fq-carcasa" />
        <rect x="11" y="20" width="6" height="14" rx="3" class="fq-switch" />
        <rect x="12" y="21.5" width="4" height="5" rx="2" class="fq-perilla" />
        <path d="M7 40h14M7 44h14M7 48h14" class="fq-grip" />
        <rect x="3" y="56" width="22" height="5" class="fq-banda" />
        <rect x="2" y="72" width="24" height="10" rx="2" class="fq-cuchilla" />
        <path d="M2 82h24l-1.5 7-1.5-5-1.5 5-1.5-5-1.5 5-1.5-5-1.5 5-1.5-5-1.5 5-1.5-5-1.5 5-1.5-5-1.5 5-1.5-5-1.5 5z" class="fq-dientes" />
      </svg>
    </div>
  `,
})
export class FondoScroll {
  private readonly host: HTMLElement = inject(ElementRef).nativeElement;

  constructor() {
    const destroyRef = inject(DestroyRef);

    afterNextRender(() => {
      const el = this.host;
      const canvas = el.querySelector<HTMLCanvasElement>('canvas')!;
      const maquina = el.querySelector<HTMLElement>('.fondo__maquina')!;
      const ctx = canvas.getContext('2d')!;
      const quieto = matchMedia('(prefers-reduced-motion: reduce)');

      interface Pelo { x: number; y: number; vx: number; vy: number; a: number; va: number; z: number; largo: number; curva: number; tinta: string }
      let w = 0;
      let h = 0;
      let pelos: Pelo[] = [];
      let ultimoY = scrollY;
      let loop = 0;
      let apagar = 0;
      // punta de la cuchilla en pantalla, para barrer los pelitos cercanos
      let cuchilla = { x: -999, y: -999 };

      // marfil y dorado sobre el negro del smoking
      const tintas = ['244 239 228', '244 239 228', '244 239 228', '217 184 119', '201 169 110'];

      const crear = (): Pelo => {
        const z = 0.35 + Math.random() * 0.65;
        return {
          x: Math.random() * w,
          y: Math.random() * h,
          vx: 0, vy: 0,
          a: Math.random() * Math.PI * 2,
          va: 0,
          z,
          largo: (7 + Math.random() * 12) * (0.6 + z * 0.6),
          curva: (Math.random() - 0.5) * 8,
          tinta: tintas[Math.floor(Math.random() * tintas.length)],
        };
      };

      const medir = () => {
        const dpr = Math.min(devicePixelRatio || 1, 2);
        w = innerWidth;
        h = innerHeight;
        canvas.width = Math.round(w * dpr);
        canvas.height = Math.round(h * dpr);
        ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
        // densidad pareja: ~1 pelito cada 20.000 px2, entre 24 y 80
        const n = Math.max(24, Math.min(80, Math.round((w * h) / 20000)));
        while (pelos.length < n) pelos.push(crear());
        pelos.length = n;
      };

      const dibujar = () => {
        ctx.clearRect(0, 0, w, h);
        ctx.lineCap = 'round';
        for (const p of pelos) {
          const c = Math.cos(p.a) * p.largo / 2;
          const s = Math.sin(p.a) * p.largo / 2;
          ctx.strokeStyle = `rgb(${p.tinta} / ${0.12 + p.z * 0.26})`;
          ctx.lineWidth = 0.8 + p.z * 0.9;
          ctx.beginPath();
          ctx.moveTo(p.x - c, p.y - s);
          ctx.quadraticCurveTo(p.x - s * p.curva / 6, p.y + c * p.curva / 6, p.x + c, p.y + s);
          ctx.stroke();
        }
      };

      const paso = () => {
        loop = 0;
        let energia = 0;
        for (const p of pelos) {
          // la cuchilla los empuja hacia afuera
          const dx = p.x - cuchilla.x;
          const dy = p.y - cuchilla.y;
          const d2 = dx * dx + dy * dy;
          if (d2 < 90 * 90 && d2 > 1) {
            const f = (1 - Math.sqrt(d2) / 90) * 1.6;
            const d = Math.sqrt(d2);
            p.vx += (dx / d) * f;
            p.vy += (dy / d) * f;
            p.va += (Math.random() - 0.5) * 0.08;
          }
          p.x += p.vx;
          p.y += p.vy;
          p.a += p.va;
          // rozamiento: frenan suave (no de golpe) cuando dejas de scrollear
          p.vx *= 0.93;
          p.vy *= 0.93;
          p.va *= 0.94;
          if (p.y > h + 20) { p.y = -20; p.x = Math.random() * w; }
          else if (p.y < -20) { p.y = h + 20; p.x = Math.random() * w; }
          if (p.x > w + 20) p.x = -20;
          else if (p.x < -20) p.x = w + 20;
          energia += Math.abs(p.vx) + Math.abs(p.vy) + Math.abs(p.va) * 10;
        }
        dibujar();
        if (energia > 0.4) loop = requestAnimationFrame(paso);
      };

      const ubicarMaquina = () => {
        const max = document.documentElement.scrollHeight - innerHeight;
        const t = max > 0 ? Math.min(1, Math.max(0, scrollY / max)) : 0;
        // baja de arriba a abajo zigzagueando de un costado al otro
        const alto = maquina.offsetHeight;
        const ancho = maquina.offsetWidth;
        const x = (0.5 + 0.38 * Math.sin(t * Math.PI * 3)) * w - ancho / 2;
        const y = -alto * 0.35 + t * (h - alto * 0.3);
        const giro = -14 * Math.cos(t * Math.PI * 3);
        maquina.style.transform = `translate3d(${x.toFixed(1)}px, ${y.toFixed(1)}px, 0) rotate(${giro.toFixed(2)}deg)`;
        // punta de la cuchilla (abajo del dibujo), rotada con la maquina
        const rad = (giro * Math.PI) / 180;
        const cx = x + ancho / 2;
        const cy = y + alto / 2;
        const r = alto * 0.45;
        cuchilla = { x: cx - Math.sin(rad) * r, y: cy + Math.cos(rad) * r };
      };

      const alScroll = () => {
        const delta = Math.max(-140, Math.min(140, scrollY - ultimoY));
        ultimoY = scrollY;
        ubicarMaquina();
        if (quieto.matches) return;
        // el Fiat del hero avanza y gira las ruedas con el scroll
        document.documentElement.style.setProperty('--sy', String(Math.min(scrollY, 1000)));

        for (const p of pelos) {
          // bajando caen, subiendo suben; los cercanos (z alto) se mueven mas
          p.vy += delta * 0.045 * p.z;
          p.vx += (Math.random() - 0.5) * Math.abs(delta) * 0.012;
          p.va += (Math.random() - 0.5) * Math.abs(delta) * 0.0016;
        }
        el.classList.add('on');
        clearTimeout(apagar);
        apagar = window.setTimeout(() => el.classList.remove('on'), 180);
        if (!loop) loop = requestAnimationFrame(paso);
      };

      const alResize = () => {
        medir();
        ubicarMaquina();
        dibujar();
      };

      medir();
      ubicarMaquina();
      dibujar();
      addEventListener('scroll', alScroll, { passive: true });
      addEventListener('resize', alResize, { passive: true });
      // la pagina cambia de alto al navegar o al cargar datos
      const ro = new ResizeObserver(() => ubicarMaquina());
      ro.observe(document.body);

      destroyRef.onDestroy(() => {
        removeEventListener('scroll', alScroll);
        removeEventListener('resize', alResize);
        ro.disconnect();
        cancelAnimationFrame(loop);
        clearTimeout(apagar);
      });
    });
  }
}

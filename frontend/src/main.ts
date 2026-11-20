import { Type, createComponent } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import { Header } from './app/layout/header';
import { Footer } from './app/layout/footer';
import { FondoScroll } from './app/layout/fondo-scroll';

// El kit de diseño espera body > header.nav, main, footer.closing: Angular toma esos mismos
// elementos como host (main es el componente raiz; header y footer se montan encima).
bootstrapApplication(App, appConfig)
  .then((appRef) => {
    const hosts: [Type<unknown>, string][] = [
      [Header, 'body > header.nav'],
      [Footer, 'body > footer.closing'],
      [FondoScroll, 'body > .fondo-scroll'],
    ];
    for (const [componente, selector] of hosts) {
      const host = document.querySelector(selector);
      if (!host) continue;
      const ref = createComponent(componente, { environmentInjector: appRef.injector, hostElement: host });
      appRef.attachView(ref.hostView);
    }
  })
  .catch((err) => console.error(err));

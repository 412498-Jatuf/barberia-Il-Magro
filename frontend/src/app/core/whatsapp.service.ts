import { DOCUMENT } from '@angular/common';
import { Injectable, inject } from '@angular/core';
import { linkWhatsapp } from './mensaje-turno';

/** Resultado de intentar abrir el chat: el link (para el boton de respaldo) y si el navegador lo abrio. */
export interface ChatWhatsapp {
  url: string;
  abierto: boolean;
}

/**
 * Abre un chat de WhatsApp con el mensaje ya escrito. Los componentes no tocan window directamente:
 * dependen de este servicio, que en los tests se reemplaza por uno falso.
 */
@Injectable({ providedIn: 'root' })
export class WhatsappService {
  private readonly ventana = inject(DOCUMENT).defaultView;

  abrirChat(numero: string, texto: string): ChatWhatsapp {
    const url = linkWhatsapp(numero, texto);
    const pestana = this.ventana?.open(url, '_blank') ?? null;
    if (pestana) pestana.opener = null;
    return { url, abierto: !!pestana };
  }
}

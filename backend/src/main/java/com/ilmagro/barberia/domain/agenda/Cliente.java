package com.ilmagro.barberia.domain.agenda;

import com.ilmagro.barberia.domain.shared.DatoInvalidoException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.regex.Pattern;

/** Value object con los datos de contacto de quien reserva. */
@Embeddable
public class Cliente {

    private static final Pattern TELEFONO = Pattern.compile("^[+]?[0-9 ()-]{6,20}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Column(name = "cliente_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "cliente_telefono", nullable = false, length = 20)
    private String telefono;

    @Column(name = "cliente_email", length = 150)
    private String email;

    protected Cliente() {
    }

    public Cliente(String nombre, String telefono, String email) {
        String n = nombre == null ? "" : nombre.trim();
        String t = telefono == null ? "" : telefono.trim();
        String e = email == null || email.isBlank() ? null : email.trim();

        if (n.length() < 2 || n.length() > 100) {
            throw new DatoInvalidoException("El nombre debe tener entre 2 y 100 caracteres");
        }
        if (!TELEFONO.matcher(t).matches()) {
            throw new DatoInvalidoException("El telefono no es valido");
        }
        if (e != null && !EMAIL.matcher(e).matches()) {
            throw new DatoInvalidoException("El email no es valido");
        }
        this.nombre = n;
        this.telefono = t;
        this.email = e;
    }

    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
}

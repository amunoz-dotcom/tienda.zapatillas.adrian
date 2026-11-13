package com.adrian.tienda.zapatillas.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "tabla_usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotEmpty(message = "Debes indicar un nombre")
    private String nombre;

    @NotEmpty(message = "Debes indicar un email")
    @Email(message = "El formato del email no es válido")
    @Column(unique = true)
    private String email;

    @NotEmpty(message = "Debes indicar una contraseña")
    @Size(min = 4, max = 100, message = "La contraseña debe tener entre 4 y 100 caracteres")
    private String pass;

    @NotEmpty(message = "Debes indicar una dirección")
    private String direccion;

    @NotEmpty(message = "Debes indicar el puesto")
    private String puesto;

    @NotNull(message = "Debes indicar un salario")
    @Min(value = 0, message = "El salario mínimo no puede ser negativo")
    private double salario;

    @Lob
    private byte[] avatar;

    public Usuario() {
    }

    public Usuario(String nombre, String email, String pass, String direccion, String puesto, double salario) {
        this.nombre = nombre;
        this.email = email;
        this.pass = pass;
        this.direccion = direccion;
        this.puesto = puesto;
        this.salario = salario;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPuesto() {
        return puesto;
    }

    public void setPuesto(String puesto) {
        this.puesto = puesto;
    }

    public double getSalario() {
        return salario;
    }

    public void setSalario(double salario) {
        this.salario = salario;
    }

    public byte[] getAvatar() {
        return avatar;
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", pass='" + pass + '\'' +
                ", direccion='" + direccion + '\'' +
                ", puesto='" + puesto + '\'' +
                ", salario=" + salario +
                '}';
    }
}
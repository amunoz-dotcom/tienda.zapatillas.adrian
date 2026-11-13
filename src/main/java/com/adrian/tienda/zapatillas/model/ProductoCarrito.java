package com.adrian.tienda.zapatillas.model;

import jakarta.persistence.*;

@Entity
public class ProductoCarrito {

    @ManyToOne
    @JoinColumn(name = "zapatilla_id")
    private Zapatilla zapatilla;

    @ManyToOne
    @JoinColumn(name = "carrito_id")
    private Carrito carrito;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int cantidad;

    public ProductoCarrito() {
    }

    public ProductoCarrito(Zapatilla zapatilla, Carrito carrito, int cantidad) {
        this.zapatilla = zapatilla;
        this.carrito = carrito;
        this.cantidad = cantidad;
    }

    public Zapatilla getZapatilla() {
        return zapatilla;
    }

    public void setZapatilla(Zapatilla zapatilla) {
        this.zapatilla = zapatilla;
    }

    public Carrito getCarrito() {
        return carrito;
    }

    public void setCarrito(Carrito carrito) {
        this.carrito = carrito;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
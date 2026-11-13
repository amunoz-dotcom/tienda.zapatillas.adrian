package com.adrian.tienda.zapatillas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "imagen_producto")
public class ImagenProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zapatilla_id", nullable = false)
    private Zapatilla zapatilla;

    @Column(nullable = false, unique = true, length = 100)
    private String archivo;

    @Column(nullable = false, unique = true, length = 100)
    private String miniatura;

    @Column(nullable = false)
    private boolean principal;

    public Long getId() {
        return id;
    }

    public Zapatilla getZapatilla() {
        return zapatilla;
    }

    public void setZapatilla(Zapatilla zapatilla) {
        this.zapatilla = zapatilla;
    }

    public String getArchivo() {
        return archivo;
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }

    public String getMiniatura() {
        return miniatura;
    }

    public void setMiniatura(String miniatura) {
        this.miniatura = miniatura;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }
}

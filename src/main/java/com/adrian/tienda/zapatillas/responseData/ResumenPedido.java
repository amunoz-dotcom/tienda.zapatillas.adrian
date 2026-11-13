package com.adrian.tienda.zapatillas.responseData;

import java.util.List;
import java.util.Map;

public class ResumenPedido {

    // datos paso 1:
    private String nombreCompleto;
    private String direccion;
    private String poblacion;
    private String provincia;
    private String codigoPostal;
    private String telefono;

    // datos paso 2:
    private String titularTarjeta;
    private String numeroTarjeta;
    private String tipoTarjeta;
    private String fechaCaducidad;

    // datos preferencias:
    private String regaloPromocional;
    private String tipoEmbalaje;
    private String tallaCalcetinesRegalo;

    private List<Map<String, Object>> zapatillas;

    public ResumenPedido() {
    }

    public ResumenPedido(String nombreCompleto, String direccion, String poblacion,
                         String provincia, String codigoPostal, String telefono,
                         String titularTarjeta, String numeroTarjeta, String tipoTarjeta,
                         String fechaCaducidad, String regaloPromocional, String tipoEmbalaje,
                         String tallaCalcetinesRegalo, List<Map<String, Object>> zapatillas) {
        this.nombreCompleto = nombreCompleto;
        this.direccion = direccion;
        this.poblacion = poblacion;
        this.provincia = provincia;
        this.codigoPostal = codigoPostal;
        this.telefono = telefono;
        this.titularTarjeta = titularTarjeta;
        this.numeroTarjeta = numeroTarjeta;
        this.tipoTarjeta = tipoTarjeta;
        this.fechaCaducidad = fechaCaducidad;
        this.regaloPromocional = regaloPromocional;
        this.tipoEmbalaje = tipoEmbalaje;
        this.tallaCalcetinesRegalo = tallaCalcetinesRegalo;
        this.zapatillas = zapatillas;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(String poblacion) {
        this.poblacion = poblacion;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getTitularTarjeta() {
        return titularTarjeta;
    }

    public void setTitularTarjeta(String titularTarjeta) {
        this.titularTarjeta = titularTarjeta;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    public String getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(String fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }

    public List<Map<String, Object>> getZapatillas() {
        return zapatillas;
    }

    public void setZapatillas(List<Map<String, Object>> zapatillas) {
        this.zapatillas = zapatillas;
    }

    public String getRegaloPromocional() {
        return regaloPromocional;
    }

    public void setRegaloPromocional(String regaloPromocional) {
        this.regaloPromocional = regaloPromocional;
    }

    public String getTipoEmbalaje() {
        return tipoEmbalaje;
    }

    public void setTipoEmbalaje(String tipoEmbalaje) {
        this.tipoEmbalaje = tipoEmbalaje;
    }

    public String getTallaCalcetinesRegalo() {
        return tallaCalcetinesRegalo;
    }

    public void setTallaCalcetinesRegalo(String tallaCalcetinesRegalo) {
        this.tallaCalcetinesRegalo = tallaCalcetinesRegalo;
    }
}
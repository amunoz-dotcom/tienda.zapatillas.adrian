package com.adrian.tienda.zapatillas.servicios; // Ajusta el paquete según tu proyecto

import java.util.List;
import java.util.Map;

public interface ServicioCarrito {

    void agregarProducto(long idUsuario, long idProducto, int cantidad);

    // Operaciones para modificar y eliminar del carrito
    void borrarProducto(long idUsuario, long idProducto);

    void actualizarCantidad(long idUsuario, long idProducto, int cantidad);

    // Operaciones que devuelven JSON:
    List<Map<String, Object>> obtenerProductosCarrito(long idUsuario);

}
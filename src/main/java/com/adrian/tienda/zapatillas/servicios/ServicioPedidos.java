package com.adrian.tienda.zapatillas.servicios;

import com.adrian.tienda.zapatillas.model.Pedido;
import com.adrian.tienda.zapatillas.responseData.ResumenPedido;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ServicioPedidos {

    //gestion en administracion
    List<Pedido> obtenerPedidos();
    Page<Pedido> obtenerPedidos(int pagina, int tamano);
    List<Pedido> obtenerPedidosUsuario(long idUsuario);
    Pedido obtenerPedidoPorId(long id);
    void actualizarEstadoPedido(long id, String estado);

    //operaciones para la parte publica - ajax/api rest
    void procesarPaso1(String nombreCompleto, String direccion, String poblacion,
                       String provincia, String codigoPostal, String telefono, long idUsuario);
    void procesarPaso2(String titular, String numero, String tipoTarjeta, String fechaCaducidad, long idUsuario);
    void procesarPasoPreferencias(String regaloPromocional, String tipoEmbalaje,
                                  String tallaCalcetinesRegalo, long idUsuario);
    ResumenPedido obtenerResumenPedido(long idUsuario);
    void confirmarPedido(long idUsuario);

}

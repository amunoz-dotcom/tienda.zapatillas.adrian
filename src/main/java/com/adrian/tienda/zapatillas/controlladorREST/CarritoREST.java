package com.adrian.tienda.zapatillas.controlladorREST;

import com.adrian.tienda.zapatillas.model.Usuario;
import com.adrian.tienda.zapatillas.servicios.ServicioCarrito;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/restCarrito")
public class CarritoREST {

    @Autowired
    private ServicioCarrito servicioCarrito;

    @RequestMapping("agregarProducto")
    public String agregarProducto(
            @RequestParam("id") long id,
            @RequestParam("cantidad") int cantidad,
            HttpServletRequest request){
        String respuesta = "";
        //comprobar que el usuario esta identificado...
        //la informacion del usuario esta metida en una variable
        //de sesion -> consultar UsuariosREST/identificarUsuario
        Usuario u = (Usuario)
                request.getSession().getAttribute("usuario_identificado");
        respuesta = "producto agregado (consultar h2): id producto: " + id +
                " cantidad: " + cantidad + " al usuario de id:" +
                u.getId();
        servicioCarrito.agregarProducto(
                u.getId(),
                id,
                cantidad
        );
        return respuesta;
    }//end agregarProducto

    @RequestMapping("obtenerProductos")
    public List<Map<String,Object>> obtenerProductos(HttpServletRequest request){
        return servicioCarrito.obtenerProductosCarrito(
                ((Usuario)request.getSession().
                        getAttribute("usuario_identificado")).getId()
        );
    }

    @PostMapping("borrarProducto")
    public String borrarProducto(
            @RequestParam("id") long id,
            HttpServletRequest request) {

        Usuario u = (Usuario) request.getSession().getAttribute("usuario_identificado");
        if (u == null) {
            return "Usuario no identificado";
        }

        servicioCarrito.borrarProducto(u.getId(), id);
        return "Producto eliminado del carrito";
    }

    @PostMapping("actualizarCantidad")
    public String actualizarCantidad(
            @RequestParam("id") long id,
            @RequestParam("cantidad") int cantidad,
            HttpServletRequest request) {

        Usuario u = (Usuario) request.getSession().getAttribute("usuario_identificado");
        if (u == null) {
            return "Usuario no identificado";
        }

        servicioCarrito.actualizarCantidad(u.getId(), id, cantidad);
        return "Cantidad actualizada";
    }
}
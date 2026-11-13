package com.adrian.tienda.zapatillas.controlladorREST;

import com.adrian.tienda.zapatillas.model.Usuario;
import com.adrian.tienda.zapatillas.responseData.ResumenPedido;
import com.adrian.tienda.zapatillas.servicios.ServicioPedidos;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/restPedidos")
public class PedidosREST {

    @Autowired
    private ServicioPedidos servicioPedidos;

    @PostMapping("/paso1")
    public ResponseEntity<String> procesarPaso1(
            @RequestParam String nombreCompleto,
            @RequestParam String direccion,
            @RequestParam String poblacion,
            @RequestParam String provincia,
            @RequestParam String codigoPostal,
            @RequestParam String telefono,
            HttpServletRequest request) {
        Usuario usuario = usuarioIdentificado(request);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debes identificarte para realizar un pedido.");
        }
        try {
            servicioPedidos.procesarPaso1(nombreCompleto, direccion, poblacion, provincia,
                    codigoPostal, telefono, usuario.getId());
            return ResponseEntity.ok("Datos de envío guardados.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/paso2")
    public ResponseEntity<String> procesarPaso2(
            @RequestParam String titular,
            @RequestParam String numero,
            @RequestParam String tipoTarjeta,
            @RequestParam String fechaCaducidad,
            HttpServletRequest request) {
        Usuario usuario = usuarioIdentificado(request);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debes identificarte para realizar un pedido.");
        }
        try {
            servicioPedidos.procesarPaso2(titular, numero, tipoTarjeta, fechaCaducidad, usuario.getId());
            return ResponseEntity.ok("Datos de pago guardados.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/preferencias")
    public ResponseEntity<String> procesarPreferencias(
            @RequestParam String regaloPromocional,
            @RequestParam String tipoEmbalaje,
            @RequestParam String tallaCalcetinesRegalo,
            HttpServletRequest request) {
        Usuario usuario = usuarioIdentificado(request);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debes identificarte para realizar un pedido.");
        }
        try {
            servicioPedidos.procesarPasoPreferencias(regaloPromocional, tipoEmbalaje,
                    tallaCalcetinesRegalo, usuario.getId());
            return ResponseEntity.ok("Preferencias guardadas.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/resumen")
    public ResponseEntity<?> obtenerResumen(HttpServletRequest request) {
        Usuario usuario = usuarioIdentificado(request);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debes identificarte para realizar un pedido.");
        }
        try {
            return ResponseEntity.ok(servicioPedidos.obtenerResumenPedido(usuario.getId()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/confirmar")
    public ResponseEntity<String> confirmarPedido(HttpServletRequest request) {
        Usuario usuario = usuarioIdentificado(request);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Debes identificarte para realizar un pedido.");
        }
        try {
            servicioPedidos.confirmarPedido(usuario.getId());
            return ResponseEntity.ok("Pedido confirmado correctamente.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<?> obtenerMisPedidos(HttpServletRequest request) {
        Usuario usuario = usuarioIdentificado(request);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Debes identificarte para consultar tus pedidos.");
        }

        List<Map<String, Object>> pedidos = new ArrayList<>();
        for (var pedido : servicioPedidos.obtenerPedidosUsuario(usuario.getId())) {
            Map<String, Object> pedidoJson = new LinkedHashMap<>();
            pedidoJson.put("id", pedido.getId());
            pedidoJson.put("estado", pedido.getEstado());
            List<Map<String, Object>> productos = new ArrayList<>();
            int totalUnidades = 0;
            for (var productoPedido : pedido.getProductosPedido()) {
                Map<String, Object> productoJson = new LinkedHashMap<>();
                productoJson.put("zapatillaId", productoPedido.getZapatilla().getId());
                productoJson.put("modelo", productoPedido.getZapatilla().getModelo());
                productoJson.put("marca", productoPedido.getZapatilla().getMarca());
                productoJson.put("talla", productoPedido.getZapatilla().getTalla());
                productoJson.put("cantidad", productoPedido.getCantidad());
                totalUnidades += productoPedido.getCantidad();
                productos.add(productoJson);
            }
            pedidoJson.put("productos", productos);
            pedidoJson.put("totalUnidades", totalUnidades);
            pedidos.add(pedidoJson);
        }
        return ResponseEntity.ok(pedidos);
    }

    private Usuario usuarioIdentificado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Usuario) session.getAttribute("usuario_identificado");
    }
}

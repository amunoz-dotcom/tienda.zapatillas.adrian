package com.adrian.tienda.zapatillas.controllers;


import com.adrian.tienda.zapatillas.model.Pedido;
import com.adrian.tienda.zapatillas.servicios.ServicioPedidos;
import com.adrian.tienda.zapatillas.constantesPedidos.EstadosPedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.context.MessageSource;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("admin/")
public class ControladorPedidos {

    @Autowired
    private ServicioPedidos servicioPedidos;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("gestionarPedidos")
    public String gestionarPedidos(@RequestParam(defaultValue = "0") int pagina,
                                   Model model,
                                   Locale locale){
        Page<Pedido> pedidos = servicioPedidos.obtenerPedidos(pagina, 10);
        model.addAttribute("pedidos", pedidos.getContent());
        model.addAttribute("pagina", pedidos);
        model.addAttribute("estados", obtenerEstados(locale));
        return "admin/gestionar_pedidos";//el html
    }

    @GetMapping("verDetallesPedido")
    public String verDetallesPedido(@RequestParam("id") Long id,
                                    Model model,
                                    Locale locale){
        Pedido p = servicioPedidos.obtenerPedidoPorId(id);
        model.addAttribute("pedido", p);
        model.addAttribute("estados", obtenerEstados(locale));
        return "admin/pedidos_detalle";
    }

    @PostMapping("actualizarEstadoPedido")
    public String actualizarEstadoPedido(@RequestParam("id") long id,
                                         @RequestParam("estado") String estado,
                                         @RequestParam(defaultValue = "0") int pagina,
                                         @RequestParam(defaultValue = "false") boolean volverAlListado) {
        servicioPedidos.actualizarEstadoPedido(id, estado);
        if (volverAlListado) {
            return "redirect:/admin/gestionarPedidos?pagina=" + Math.max(0, pagina);
        }
        return "redirect:/admin/verDetallesPedido?id=" + id;
    }

    private Map<String, String> obtenerEstados(Locale locale) {
        Map<String, String> estados = new LinkedHashMap<>();
        for (EstadosPedido estado : EstadosPedido.values()) {
            String clave = "admin.order.state." + estado.name();
            estados.put(estado.name(), messageSource.getMessage(clave, null, locale));
        }
        return estados;
    }

}

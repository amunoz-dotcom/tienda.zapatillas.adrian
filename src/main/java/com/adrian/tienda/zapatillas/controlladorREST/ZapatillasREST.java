package com.adrian.tienda.zapatillas.controlladorREST;


import com.adrian.tienda.zapatillas.servicios.ServicioZapatillas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("restZapatillas/")
public class ZapatillasREST {

    @Autowired
    private ServicioZapatillas servicioZapatillas;

    @RequestMapping("listar")
    public Map<String, Object> listar(
            @RequestParam(defaultValue = "") String busqueda,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "10") int tamano){
        Page<Map<String, Object>> resultado = servicioZapatillas.obtenerJSONzapatillas(busqueda, pagina, tamano);
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("zapatillas", resultado.getContent());
        respuesta.put("total", resultado.getTotalElements());
        respuesta.put("pagina", resultado.getNumber());
        respuesta.put("totalPaginas", resultado.getTotalPages());
        return respuesta;
    }

    @RequestMapping("obtener-zapatilla")
    public Map<String,Object> obtenerZapatilla(@RequestParam("id") Long id){
        return servicioZapatillas.obtenerJSONZapatillasPorId(id);
    }

}

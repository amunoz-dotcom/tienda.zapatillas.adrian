package com.adrian.tienda.zapatillas.controlladorImagen;


import com.adrian.tienda.zapatillas.servicios.ServicioZapatillas;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;

@Controller
public class ControlladorImagen {

    @Autowired
    private ServicioZapatillas servicioZapatillas;

    @GetMapping("mostrar_imagen")
    public void mostrarImagen(@RequestParam("id") Long id, HttpServletResponse response) throws IOException {

        byte[] info = servicioZapatillas.obtenerImagenPrincipal(id, false);
        if (info == null ){
            return;
        }
        String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(info));
        response.setContentType(contentType != null ? contentType : "application/octet-stream");
        response.getOutputStream().write(info);
        response.getOutputStream().close();


    }

    @GetMapping("mostrar_miniatura")
    public void mostrarMiniatura(@RequestParam("id") Long id, HttpServletResponse response) throws IOException {
        escribirImagen(servicioZapatillas.obtenerImagenPrincipal(id, true), response);
    }

    @GetMapping("imagenes-producto/{id}")
    public void mostrarImagenProducto(@org.springframework.web.bind.annotation.PathVariable("id") Long id,
                                      HttpServletResponse response) throws IOException {
        escribirImagen(servicioZapatillas.obtenerImagen(id, false), response);
    }

    @GetMapping("miniaturas-producto/{id}")
    public void mostrarMiniaturaProducto(@org.springframework.web.bind.annotation.PathVariable("id") Long id,
                                         HttpServletResponse response) throws IOException {
        escribirImagen(servicioZapatillas.obtenerImagen(id, true), response);
    }

    private void escribirImagen(byte[] info, HttpServletResponse response) throws IOException {
        if (info == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String contentType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(info));
        response.setContentType(contentType != null ? contentType : "image/jpeg");
        response.setHeader("Cache-Control", "public, max-age=86400");
        response.getOutputStream().write(info);
        response.getOutputStream().close();
    }

}

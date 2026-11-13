package com.adrian.tienda.zapatillas.servicios;

import org.springframework.web.multipart.MultipartFile;

public interface ServicioImagenesProducto {

    ArchivosImagen procesarYGuardar(MultipartFile archivo);

    byte[] generarMiniatura(byte[] imagen);

    byte[] leer(String nombreArchivo);

    void borrar(String nombreArchivo);

    record ArchivosImagen(String archivo, String miniatura) {
    }
}

package com.adrian.tienda.zapatillas.serviciosImpl;

import com.adrian.tienda.zapatillas.servicios.ServicioImagenesProducto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ServicioImagenesProductoImplTest {

    @TempDir
    Path directorioTemporal;

    @Test
    void procesaImagenYGeneraVersionCompletaYMiniaturaSinDistorsion() throws IOException {
        ServicioImagenesProductoImpl servicio = crearServicio();
        MockMultipartFile archivo = imagen(800, 400);

        ServicioImagenesProducto.ArchivosImagen nombres = servicio.procesarYGuardar(archivo);

        BufferedImage originalProcesada = ImageIO.read(
                new java.io.ByteArrayInputStream(servicio.leer(nombres.archivo())));
        BufferedImage miniatura = ImageIO.read(
                new java.io.ByteArrayInputStream(servicio.leer(nombres.miniatura())));
        assertEquals(600, originalProcesada.getWidth());
        assertEquals(300, originalProcesada.getHeight());
        assertEquals(300, miniatura.getWidth());
        assertEquals(150, miniatura.getHeight());
        servicio.borrar(nombres.archivo());
        servicio.borrar(nombres.miniatura());
    }

    @Test
    void rechazaImagenConDimensionesMenoresAlMinimo() throws IOException {
        ServicioImagenesProductoImpl servicio = crearServicio();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> servicio.procesarYGuardar(imagen(299, 400)));

        assertTrue(error.getMessage().contains("300 x 300"));
    }

    @Test
    void rechazaImagenConDimensionesMayoresAlMaximo() throws IOException {
        ServicioImagenesProductoImpl servicio = crearServicio();

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> servicio.procesarYGuardar(imagen(4001, 400)));

        assertTrue(error.getMessage().contains("4000 x 4000"));
    }

    @Test
    void rechazaArchivoQueNoEsUnaImagen() {
        ServicioImagenesProductoImpl servicio = crearServicio();
        MockMultipartFile archivo = new MockMultipartFile(
                "imagenes", "documento.txt", "image/jpeg", "no es una imagen".getBytes());

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> servicio.procesarYGuardar(archivo));

        assertTrue(error.getMessage().contains("Formato no permitido"));
    }

    @Test
    void admiteWebpRealAunqueElNombreYMimeSeanEnganosos() throws IOException {
        BufferedImage imagen = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(imagen, "webp", salida));
        MockMultipartFile archivo = new MockMultipartFile(
                "imagenes", "archivo.txt", "text/plain", salida.toByteArray());
        ServicioImagenesProductoImpl servicio = crearServicio();

        ServicioImagenesProducto.ArchivosImagen nombres = servicio.procesarYGuardar(archivo);

        assertNotNull(ImageIO.read(new java.io.ByteArrayInputStream(servicio.leer(nombres.archivo()))));
        servicio.borrar(nombres.archivo());
        servicio.borrar(nombres.miniatura());
    }

    @Test
    void rechazaArchivoQueSuperaElPesoConfigurado() throws IOException {
        ServicioImagenesProductoImpl servicio = new ServicioImagenesProductoImpl(
                directorioTemporal.toString(), 1, 300, 300, 4000, 4000, 600, 600, 300, 300);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> servicio.procesarYGuardar(imagen(400, 400)));

        assertTrue(error.getMessage().contains("peso máximo"));
    }

    private ServicioImagenesProductoImpl crearServicio() {
        return new ServicioImagenesProductoImpl(
                directorioTemporal.toString(), 10_000_000, 300, 300,
                4000, 4000, 600, 600, 300, 300);
    }

    private MockMultipartFile imagen(int ancho, int alto) throws IOException {
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(imagen, "jpg", salida);
        return new MockMultipartFile("imagenes", "producto.jpg", "image/jpeg", salida.toByteArray());
    }
}

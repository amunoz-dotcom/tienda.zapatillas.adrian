package com.adrian.tienda.zapatillas.serviciosImpl;

import com.adrian.tienda.zapatillas.servicios.ServicioImagenesProducto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

@Service
public class ServicioImagenesProductoImpl implements ServicioImagenesProducto {

    private static final Logger logger = LoggerFactory.getLogger(ServicioImagenesProductoImpl.class);
    private final Path directorio;
    private final long pesoMaximo;
    private final int anchoMinimo;
    private final int altoMinimo;
    private final int anchoMaximo;
    private final int altoMaximo;
    private final int anchoSalida;
    private final int altoSalida;
    private final int anchoMiniatura;
    private final int altoMiniatura;

    public ServicioImagenesProductoImpl(
            @Value("${app.product-images.directory:./data/imagenes-productos}") String directorio,
            @Value("${app.product-images.max-file-size:10485760}") long pesoMaximo,
            @Value("${app.product-images.min-width:300}") int anchoMinimo,
            @Value("${app.product-images.min-height:300}") int altoMinimo,
            @Value("${app.product-images.max-width:4000}") int anchoMaximo,
            @Value("${app.product-images.max-height:4000}") int altoMaximo,
            @Value("${app.product-images.max-width-output:1200}") int anchoSalida,
            @Value("${app.product-images.max-height-output:1200}") int altoSalida,
            @Value("${app.product-images.thumbnail-max-width:300}") int anchoMiniatura,
            @Value("${app.product-images.thumbnail-max-height:300}") int altoMiniatura) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
        this.pesoMaximo = pesoMaximo;
        this.anchoMinimo = anchoMinimo;
        this.altoMinimo = altoMinimo;
        this.anchoMaximo = anchoMaximo;
        this.altoMaximo = altoMaximo;
        this.anchoSalida = anchoSalida;
        this.altoSalida = altoSalida;
        this.anchoMiniatura = anchoMiniatura;
        this.altoMiniatura = altoMiniatura;
    }

    @Override
    public ArchivosImagen procesarYGuardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Selecciona una imagen para subir.");
        }
        if (archivo.getSize() > pesoMaximo) {
            throw new IllegalArgumentException("La imagen supera el peso máximo permitido de "
                    + (pesoMaximo / 1024 / 1024) + " MB.");
        }

        try {
            byte[] contenido = archivo.getBytes();
            BufferedImage imagen = decodificar(contenido);
            int ancho = imagen.getWidth();
            int alto = imagen.getHeight();
            if (ancho < anchoMinimo || alto < altoMinimo) {
                throw new IllegalArgumentException("La imagen debe medir como mínimo "
                        + anchoMinimo + " x " + altoMinimo + " píxeles.");
            }
            if (ancho > anchoMaximo || alto > altoMaximo) {
                throw new IllegalArgumentException("La imagen no puede superar "
                        + anchoMaximo + " x " + altoMaximo + " píxeles.");
            }

            byte[] imagenProcesada = codificar(redimensionar(imagen, anchoSalida, altoSalida));
            byte[] miniaturaProcesada = codificar(redimensionar(imagen, anchoMiniatura, altoMiniatura));
            String identificador = UUID.randomUUID().toString();
            String nombreImagen = "originales/" + identificador + ".jpg";
            String nombreMiniatura = "miniaturas/" + identificador + ".jpg";
            guardar(nombreImagen, imagenProcesada);
            try {
                guardar(nombreMiniatura, miniaturaProcesada);
            } catch (RuntimeException error) {
                borrar(nombreImagen);
                throw error;
            }
            return new ArchivosImagen(nombreImagen, nombreMiniatura);
        } catch (IOException error) {
            throw new IllegalArgumentException("No se pudo leer o procesar la imagen subida.", error);
        }
    }

    @Override
    public byte[] generarMiniatura(byte[] contenido) {
        try {
            BufferedImage imagen = ImageIO.read(new ByteArrayInputStream(contenido));
            if (imagen == null) {
                throw new IllegalStateException("La imagen heredada del producto no es válida.");
            }
            return codificar(redimensionar(imagen, anchoMiniatura, altoMiniatura));
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo generar la miniatura de una imagen heredada.", error);
        }
    }

    private BufferedImage decodificar(byte[] contenido) throws IOException {
        try (ImageInputStream entrada = ImageIO.createImageInputStream(new ByteArrayInputStream(contenido))) {
            if (entrada == null) {
                throw new IllegalArgumentException("El archivo no contiene una imagen válida.");
            }
            Iterator<ImageReader> lectores = ImageIO.getImageReaders(entrada);
            if (!lectores.hasNext()) {
                throw new IllegalArgumentException("Formato no permitido. Usa JPEG, JPG, PNG o WEBP.");
            }
            ImageReader lector = lectores.next();
            try {
                String formato = lector.getFormatName().toLowerCase(Locale.ROOT);
                if (!formato.equals("jpeg") && !formato.equals("jpg")
                        && !formato.equals("png") && !formato.equals("webp")) {
                    throw new IllegalArgumentException("Formato no permitido. Usa JPEG, JPG, PNG o WEBP.");
                }
                lector.setInput(entrada, true, true);
                int ancho = lector.getWidth(0);
                int alto = lector.getHeight(0);
                if (ancho < anchoMinimo || alto < altoMinimo) {
                    throw new IllegalArgumentException("La imagen debe medir como mínimo "
                            + anchoMinimo + " x " + altoMinimo + " píxeles.");
                }
                if (ancho > anchoMaximo || alto > altoMaximo) {
                    throw new IllegalArgumentException("La imagen no puede superar "
                            + anchoMaximo + " x " + altoMaximo + " píxeles.");
                }
                BufferedImage imagen = lector.read(0);
                if (imagen == null) {
                    throw new IllegalArgumentException("El archivo no contiene una imagen válida.");
                }
                return imagen;
            } finally {
                lector.dispose();
            }
        }
    }

    private BufferedImage redimensionar(BufferedImage original, int maxAncho, int maxAlto) {
        double escala = Math.min(1.0, Math.min(
                (double) maxAncho / original.getWidth(),
                (double) maxAlto / original.getHeight()));
        int ancho = Math.max(1, (int) Math.round(original.getWidth() * escala));
        int alto = Math.max(1, (int) Math.round(original.getHeight() * escala));
        BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D graficos = resultado.createGraphics();
        try {
            graficos.setColor(Color.WHITE);
            graficos.fillRect(0, 0, ancho, alto);
            graficos.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graficos.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graficos.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graficos.drawImage(original, 0, 0, ancho, alto, null);
        } finally {
            graficos.dispose();
        }
        return resultado;
    }

    private byte[] codificar(BufferedImage imagen) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        if (!ImageIO.write(imagen, "jpg", salida)) {
            throw new IOException("No hay un codificador JPEG disponible.");
        }
        return salida.toByteArray();
    }

    private void guardar(String nombreArchivo, byte[] contenido) {
        Path ruta = resolver(nombreArchivo);
        Path temporal = ruta.resolveSibling(ruta.getFileName() + "." + UUID.randomUUID() + ".tmp");
        try {
            Files.createDirectories(ruta.getParent());
            Files.write(temporal, contenido);
            try {
                Files.move(temporal, ruta, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException error) {
                Files.move(temporal, ruta);
            }
        } catch (IOException error) {
            try {
                Files.deleteIfExists(temporal);
            } catch (IOException limpiezaError) {
                error.addSuppressed(limpiezaError);
            }
            throw new IllegalStateException("No se pudo guardar una imagen del producto.", error);
        }
    }

    @Override
    public byte[] leer(String nombreArchivo) {
        try {
            return Files.readAllBytes(resolver(nombreArchivo));
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo leer una imagen almacenada.", error);
        }
    }

    @Override
    public void borrar(String nombreArchivo) {
        try {
            Files.deleteIfExists(resolver(nombreArchivo));
        } catch (IOException error) {
            logger.error("No se pudo eliminar el archivo de imagen {}", nombreArchivo, error);
        }
    }

    private Path resolver(String nombreArchivo) {
        Path ruta = directorio.resolve(nombreArchivo).normalize();
        if (!ruta.startsWith(directorio)) {
            throw new IllegalArgumentException("La ruta de imagen no es válida.");
        }
        return ruta;
    }
}

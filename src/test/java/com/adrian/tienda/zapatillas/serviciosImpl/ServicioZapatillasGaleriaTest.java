package com.adrian.tienda.zapatillas.serviciosImpl;

import com.adrian.tienda.zapatillas.model.Categoria;
import com.adrian.tienda.zapatillas.model.ImagenProducto;
import com.adrian.tienda.zapatillas.model.Zapatilla;
import com.adrian.tienda.zapatillas.servicios.ServicioZapatillas;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:galeria-crud;DB_CLOSE_DELAY=-1",
        "app.product-images.directory=target/test-product-images"
})
class ServicioZapatillasGaleriaTest {

    @Autowired
    private ServicioZapatillas servicioZapatillas;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    @org.springframework.transaction.annotation.Transactional
    void creaActualizaSeleccionaYEliminaImagenesDelProducto() throws Exception {
        Categoria categoria = new Categoria("GAL", "Categoría de pruebas");
        entityManager.persist(categoria);
        entityManager.flush();

        Zapatilla nueva = new Zapatilla("Modelo galería", "Marca", 80, "Negro", 42, "Prueba");
        nueva.setIdCategoria(categoria.getId());
        servicioZapatillas.registrarZapatilla(nueva,
                new MockMultipartFile[]{imagen("una.jpg"), imagen("dos.jpg")}, "new:1");
        entityManager.flush();

        Zapatilla guardada = servicioZapatillas.obtenerZapatillaPorid(nueva.getId());
        assertEquals(2, guardada.getImagenes().size());
        assertEquals(1, guardada.getImagenes().stream().filter(ImagenProducto::isPrincipal).count());
        assertNotNull(servicioZapatillas.obtenerImagenPrincipal(nueva.getId(), false));
        assertNotNull(servicioZapatillas.obtenerImagenPrincipal(nueva.getId(), true));

        Zapatilla cambios = new Zapatilla("Modelo galería actualizado", "Marca", 85, "Negro", 42, "Prueba");
        cambios.setId(nueva.getId());
        cambios.setIdCategoria(categoria.getId());
        servicioZapatillas.guardarCambiosZapatilla(cambios,
                new MockMultipartFile[]{imagen("tres.jpg")}, "existing:" + guardada.getImagenes().get(0).getId());
        entityManager.flush();

        Zapatilla actualizada = servicioZapatillas.obtenerZapatillaPorid(nueva.getId());
        assertEquals("Modelo galería actualizado", actualizada.getModelo());
        assertEquals(3, actualizada.getImagenes().size());
        assertEquals(1, actualizada.getImagenes().stream().filter(ImagenProducto::isPrincipal).count());
        assertTrue(actualizada.getImagenes().stream()
                .anyMatch(imagen -> imagen.getId().equals(guardada.getImagenes().get(0).getId())
                        && imagen.isPrincipal()));

        Long imagenNoPrincipal = actualizada.getImagenes().stream()
                .filter(imagen -> !imagen.isPrincipal()).findFirst().orElseThrow().getId();
        servicioZapatillas.eliminarImagen(nueva.getId(), imagenNoPrincipal.toString());
        entityManager.flush();
        assertEquals(2, servicioZapatillas.obtenerZapatillaPorid(nueva.getId()).getImagenes().size());

        servicioZapatillas.eliminarImagen(nueva.getId(), actualizada.getImagenes().stream()
                .filter(ImagenProducto::isPrincipal).findFirst().orElseThrow().getId().toString());
        entityManager.flush();
        List<ImagenProducto> restantes = servicioZapatillas.obtenerZapatillaPorid(nueva.getId()).getImagenes();
        assertEquals(1, restantes.stream().filter(ImagenProducto::isPrincipal).count());

        Long ultimaImagen = restantes.get(0).getId();
        assertThrows(IllegalArgumentException.class,
                () -> servicioZapatillas.eliminarImagen(nueva.getId(), ultimaImagen.toString()));

        servicioZapatillas.borrarZapatilla(nueva.getId());
        entityManager.flush();
        assertNull(entityManager.find(Zapatilla.class, nueva.getId()));
    }

    @Test
    void eliminaArchivosFisicosCuandoSeConfirmaElBorradoDelProducto() throws IOException {
        TransactionTemplate transaccion = new TransactionTemplate(transactionManager);
        Categoria categoria = new Categoria("GAL-FILES", "Prueba de borrado físico");
        Integer idCategoria = transaccion.execute(estado -> {
            entityManager.persist(categoria);
            entityManager.flush();
            return categoria.getId();
        });
        Zapatilla producto = new Zapatilla("Modelo borrado", "Marca", 80, "Negro", 42, "Prueba");
        producto.setIdCategoria(idCategoria);
        long idProducto = 0;
        boolean productoEliminado = false;
        try {
            servicioZapatillas.registrarZapatilla(producto,
                    new MockMultipartFile[]{imagen("borrar.jpg")}, "new:0");
            idProducto = producto.getId();
            ImagenProducto imagen = servicioZapatillas.obtenerZapatillaPorid(idProducto)
                    .getImagenes().get(0);
            Path directorioImagenes = Path.of("target/test-product-images").toAbsolutePath();
            Path archivo = directorioImagenes.resolve(imagen.getArchivo());
            Path miniatura = directorioImagenes.resolve(imagen.getMiniatura());
            assertTrue(Files.exists(archivo));
            assertTrue(Files.exists(miniatura));

            servicioZapatillas.borrarZapatilla(idProducto);
            productoEliminado = true;

            assertFalse(Files.exists(archivo));
            assertFalse(Files.exists(miniatura));
        } finally {
            if (idProducto != 0 && !productoEliminado) {
                servicioZapatillas.borrarZapatilla(idProducto);
            }
            transaccion.execute(estado -> {
                Categoria persistida = entityManager.find(Categoria.class, idCategoria);
                if (persistida != null) {
                    entityManager.remove(persistida);
                }
                return null;
            });
        }
    }

    private MockMultipartFile imagen(String nombre) throws IOException {
        BufferedImage imagen = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(imagen, "jpg", salida);
        return new MockMultipartFile("imagenes", nombre, "image/jpeg", salida.toByteArray());
    }
}

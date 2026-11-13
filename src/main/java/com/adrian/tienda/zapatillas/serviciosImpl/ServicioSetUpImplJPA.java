package com.adrian.tienda.zapatillas.serviciosImpl;

import com.adrian.tienda.zapatillas.model.Carrito;
import com.adrian.tienda.zapatillas.model.Categoria;
import com.adrian.tienda.zapatillas.model.Configuracion;
import com.adrian.tienda.zapatillas.model.ProductoCarrito;
import com.adrian.tienda.zapatillas.model.Usuario;
import com.adrian.tienda.zapatillas.model.Zapatilla;
import com.adrian.tienda.zapatillas.servicios.ServicioPedidos;
import com.adrian.tienda.zapatillas.servicios.ServicioSetUp;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class ServicioSetUpImplJPA implements ServicioSetUp {

    private static final Map<String, String> IMAGENES_POR_MODELO = Map.ofEntries(
            Map.entry("Air Max 90", "Air-Max-90.jpg"),
            Map.entry("Pegasus 40", "Pegasus-40.jpg"),
            Map.entry("Air Jordan 1", "Sneaker-Street.jpg"),
            Map.entry("Old Skool", "Old-Skool.jpg"),
            Map.entry("Air Zoom Tempo", "Sneaker-Red.jpg"),
            Map.entry("Fresh Foam 1080", "Sneaker-White.jpg"),
            Map.entry("Gel Nimbus 26", "Sneaker-Red.jpg"),
            Map.entry("Ultraboost Light", "Sneaker-White.jpg"),
            Map.entry("Wave Rider 27", "Sneaker-Red.jpg"),
            Map.entry("Clifton 9", "Sneaker-White.jpg"),
            Map.entry("Pegasus Trail 4", "Pegasus-40.jpg"),
            Map.entry("Cloudsurfer", "Sneaker-Classic.jpg"),
            Map.entry("Classic Leather", "Sneaker-Classic.jpg"),
            Map.entry("Chuck Taylor All Star", "Old-Skool.jpg"),
            Map.entry("Stan Smith", "Sneaker-Classic.jpg"),
            Map.entry("574 Core", "Sneaker-White.jpg"),
            Map.entry("Suede Classic", "Sneaker-Red.jpg"),
            Map.entry("Club C 85", "Sneaker-Classic.jpg"),
            Map.entry("Air Force 1 Low", "Sneaker-Classic.jpg"),
            Map.entry("Forum Low", "Sneaker-Street.jpg"),
            Map.entry("Dame 8", "Sneaker-Street.jpg"),
            Map.entry("Curry Flow 10", "Sneaker-Red.jpg"),
            Map.entry("LeBron Witness 8", "Sneaker-Street.jpg"),
            Map.entry("Two Wxy v4", "Sneaker-White.jpg"),
            Map.entry("Ja 1", "Sneaker-Red.jpg"),
            Map.entry("Hardwood Classic", "Sneaker-Street.jpg"),
            Map.entry("Old Skool Pro", "Old-Skool.jpg"),
            Map.entry("Busenitz", "Sneaker-Street.jpg")
    );

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ServicioPedidos servicioPedidos;

    @Override
    public void prepararRegistrosIniciales() {
        Configuracion configuracion = null;

        try {
            configuracion = (Configuracion) entityManager.createQuery(
                            "select c from Configuracion c")
                    .getSingleResult();
        } catch (NoResultException e) {
            System.out.println(
                    "no se encontro un unico registro en la tabla de Configuracion");
        }

        if (configuracion == null || !configuracion.isRegistrosIniciales()) {
            // Categorias
            Categoria cRunning = new Categoria("RUN", "Zapatillas de Running");
            Categoria cCasual = new Categoria("CAS", "Zapatillas Casual / Lifestyle");
            Categoria cBasket = new Categoria("BSK", "Zapatillas de Baloncesto");
            Categoria cSkate = new Categoria("SKT", "Zapatillas de Skate");
            entityManager.persist(cRunning);
            entityManager.persist(cCasual);
            entityManager.persist(cBasket);
            entityManager.persist(cSkate);

            // Productos
            Zapatilla zAirMax = new Zapatilla("Air Max 90", "Nike", 149.99, "Blanco", 42.5, "Iconicas zapatillas urbanas con amortiguacion de aire");
            zAirMax.setCategoria(cCasual);
            zAirMax.setImagenPortada(leerImagenInicial(IMAGENES_POR_MODELO.get(zAirMax.getModelo())));
            entityManager.persist(zAirMax);

            Zapatilla zPegasus = new Zapatilla("Pegasus 40", "Nike", 129.95, "Negro", 41.0, "Zapatillas de running ideales para entrenamiento diario");
            zPegasus.setCategoria(cRunning);
            zPegasus.setImagenPortada(leerImagenInicial(IMAGENES_POR_MODELO.get(zPegasus.getModelo())));
            entityManager.persist(zPegasus);

            Zapatilla zJordan = new Zapatilla("Air Jordan 1", "Nike", 179.99, "Rojo", 43.0, "Zapatillas clasicas de baloncesto de tiro alto");
            zJordan.setCategoria(cBasket);
            zJordan.setImagenPortada(leerImagenInicial(IMAGENES_POR_MODELO.get(zJordan.getModelo())));
            entityManager.persist(zJordan);

            Zapatilla zOldSkool = new Zapatilla("Old Skool", "Vans", 75.0, "Negro", 40.0, "Zapatillas clasicas de skate con banda lateral");
            zOldSkool.setCategoria(cSkate);
            zOldSkool.setImagenPortada(leerImagenInicial(IMAGENES_POR_MODELO.get(zOldSkool.getModelo())));
            entityManager.persist(zOldSkool);

            // Usuario
            Usuario u1 = new Usuario();
            u1.setNombre("Pepe");
            u1.setPass("123456");
            u1.setEmail("pepe@gmail.com");
            u1.setDireccion("Calle Mayor 12, Madrid");
            u1.setPuesto("Cliente");
            entityManager.persist(u1);

            // Carrito
            Carrito c1 = new Carrito();
            c1.setUsuario(u1);
            entityManager.persist(c1);

            ProductoCarrito pc1 = new ProductoCarrito();
            pc1.setCarrito(c1);
            pc1.setZapatilla(zAirMax);
            pc1.setCantidad(3);
            c1.getProductosCarrito().add(pc1);
            entityManager.persist(pc1);

            ProductoCarrito pc2 = new ProductoCarrito();
            pc2.setCarrito(c1);
            pc2.setZapatilla(zPegasus);
            pc2.setCantidad(1);
            c1.getProductosCarrito().add(pc2);
            entityManager.persist(pc2);

            // Sincronizar datos con la base de datos antes de consultar el carrito
            entityManager.flush();

            // Paso 1: Dirección y Datos Personales
            servicioPedidos.procesarPaso1(
                    "Pedro Fernandez", "Arguelles 12", "Madrid",
                    "Madrid", "28008", "600123456", u1.getId()
            );

            // Paso Preferencias Sneaker
            servicioPedidos.procesarPasoPreferencias(
                    "Spray protector de zapatillas",
                    "Caja doble reinforced para coleccionistas",
                    "M (39-42)",
                    u1.getId()
            );

            // Paso 2: Pago con Tarjeta (incluye fechaCaducidad)
            servicioPedidos.procesarPaso2(
                    "Pedro Fernandez Del Soto", "4111111111111111",
                    "visa", "12/28", u1.getId()
            );

            servicioPedidos.confirmarPedido(u1.getId());

            // Marcar configuracion como realizada
            configuracion = new Configuracion();
            configuracion.setRegistrosIniciales(true);
            entityManager.persist(configuracion);
        }

        crearProductosDemostracion();
        completarImagenesProductosSinImagen();
    }

    private void crearProductosDemostracion() {
        List<Categoria> categoriasExistentes = entityManager.createQuery(
                "select c from Categoria c", Categoria.class).getResultList();
        Map<String, Categoria> categoriasPorCodigo = new HashMap<>();
        for (Categoria categoria : categoriasExistentes) {
            categoriasPorCodigo.put(categoria.getNombre(), categoria);
        }
        if (!categoriasPorCodigo.keySet().containsAll(List.of("RUN", "CAS", "BSK", "SKT"))) {
            throw new IllegalStateException("Faltan categorías necesarias para cargar los productos de demostración.");
        }

        Set<String> skusExistentes = new HashSet<>(entityManager.createQuery(
                "select z.sku from Zapatilla z where z.sku like 'DEMO-%'", String.class).getResultList());
        String[][] productos = {
                {"Air Zoom Tempo", "Nike", "124.90", "Azul", "42", "Running ligero para entrenamientos rápidos", "RUN"},
                {"Fresh Foam 1080", "New Balance", "169.95", "Gris", "41.5", "Amortiguación suave para largas distancias", "RUN"},
                {"Gel Nimbus 26", "Asics", "159.99", "Negro", "43", "Comodidad y estabilidad para correr a diario", "RUN"},
                {"Ultraboost Light", "Adidas", "179.00", "Blanco", "42.5", "Zapatilla de running con retorno de energía", "RUN"},
                {"Wave Rider 27", "Mizuno", "139.90", "Verde", "40.5", "Pisada equilibrada para corredores habituales", "RUN"},
                {"Clifton 9", "Hoka", "149.95", "Naranja", "44", "Diseño ligero con amortiguación de alto rendimiento", "RUN"},
                {"Pegasus Trail 4", "Nike", "139.99", "Marrón", "42", "Tracción versátil para senderos y asfalto", "RUN"},
                {"Cloudsurfer", "On", "159.00", "Blanco", "39.5", "Transiciones suaves y comodidad diaria", "RUN"},
                {"Classic Leather", "Reebok", "89.90", "Blanco", "40", "Silueta retro de cuero para uso diario", "CAS"},
                {"Chuck Taylor All Star", "Converse", "74.95", "Negro", "39", "Lona clásica con estilo urbano atemporal", "CAS"},
                {"Stan Smith", "Adidas", "99.99", "Blanco", "42", "Diseño minimalista para combinar con todo", "CAS"},
                {"574 Core", "New Balance", "109.00", "Azul", "41", "Estilo casual con soporte y comodidad", "CAS"},
                {"Suede Classic", "Puma", "84.90", "Rojo", "43", "Acabado de ante y perfil clásico", "CAS"},
                {"Club C 85", "Reebok", "89.00", "Beige", "40.5", "Inspiración vintage para el día a día", "CAS"},
                {"Air Force 1 Low", "Nike", "119.99", "Blanco", "44", "Icono urbano con suela de amortiguación", "CAS"},
                {"Forum Low", "Adidas", "109.95", "Blanco", "42", "Estilo de baloncesto clásico actualizado", "CAS"},
                {"Dame 8", "Adidas", "129.00", "Negro", "44", "Agarre y respuesta para cambios de dirección", "BSK"},
                {"Curry Flow 10", "Under Armour", "160.00", "Amarillo", "43", "Zapatilla ligera para juego rápido en pista", "BSK"},
                {"LeBron Witness 8", "Nike", "119.99", "Morado", "45", "Soporte y amortiguación para la cancha", "BSK"},
                {"Two Wxy v4", "New Balance", "119.95", "Azul", "42.5", "Estabilidad para jugadores polivalentes", "BSK"},
                {"Ja 1", "Nike", "119.00", "Rosa", "43", "Diseño reactivo para movimientos explosivos", "BSK"},
                {"Hardwood Classic", "Puma", "99.90", "Verde", "41", "Perfil bajo con buen agarre en pista", "BSK"},
                {"Old Skool Pro", "Vans", "84.95", "Negro", "40", "Refuerzo y agarre para sesiones de skate", "SKT"},
                {"Busenitz", "Adidas", "94.99", "Marrón", "42", "Control y resistencia para patinar", "SKT"}
        };

        int numero = 1;
        for (String[] datos : productos) {
            String sku = String.format("DEMO-%03d", numero++);
            if (skusExistentes.contains(sku)) {
                continue;
            }

            Zapatilla producto = new Zapatilla(
                    datos[0], datos[1], Double.parseDouble(datos[2]), datos[3],
                    Double.parseDouble(datos[4]), datos[5]);
            producto.setSku(sku);
            producto.setCategoria(categoriasPorCodigo.get(datos[6]));
            producto.setImagenPortada(leerImagenInicial(IMAGENES_POR_MODELO.get(producto.getModelo())));
            entityManager.persist(producto);
            skusExistentes.add(sku);
        }
    }

    private void completarImagenesProductosSinImagen() {
        List<Zapatilla> productosSinImagen = entityManager.createQuery(
                "select z from Zapatilla z where z.imagenPortada is null", Zapatilla.class).getResultList();
        for (Zapatilla producto : productosSinImagen) {
            String nombreImagen = IMAGENES_POR_MODELO.get(producto.getModelo());
            if (nombreImagen == null) {
                nombreImagen = imagenPorCategoria(producto.getCategoria().getNombre());
            }
            producto.setImagenPortada(leerImagenInicial(nombreImagen));
        }
    }

    private String imagenPorCategoria(String categoria) {
        return switch (categoria) {
            case "RUN" -> "Sneaker-Red.jpg";
            case "CAS" -> "Sneaker-Classic.jpg";
            case "BSK" -> "Sneaker-Street.jpg";
            case "SKT" -> "Old-Skool.jpg";
            default -> "Sneaker-Classic.jpg";
        };
    }

    private byte[] leerImagenInicial(String nombreArchivo) {
        if (nombreArchivo == null) {
            throw new IllegalStateException("No hay una imagen asignada al producto.");
        }
        try (InputStream imagen = getClass().getResourceAsStream(
                "/static/imagenes_productos_iniciales/" + nombreArchivo)) {
            if (imagen == null) {
                throw new IllegalStateException("No se encontró la imagen del producto: " + nombreArchivo);
            }
            return IOUtils.toByteArray(imagen);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la imagen del producto: " + nombreArchivo, e);
        }
    }
}
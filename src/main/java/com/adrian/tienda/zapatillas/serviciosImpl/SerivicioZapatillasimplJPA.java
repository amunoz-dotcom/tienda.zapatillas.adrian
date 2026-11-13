package com.adrian.tienda.zapatillas.serviciosImpl;

import com.adrian.tienda.zapatillas.constantesSQL.ConstantesSQL;
import com.adrian.tienda.zapatillas.model.Categoria;
import com.adrian.tienda.zapatillas.model.ImagenProducto;
import com.adrian.tienda.zapatillas.model.Zapatilla;
import com.adrian.tienda.zapatillas.servicios.ServicioImagenesProducto;
import com.adrian.tienda.zapatillas.servicios.ServicioZapatillas;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.hibernate.query.sql.internal.NativeQueryImpl;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

@Service
@Transactional
public class SerivicioZapatillasimplJPA implements ServicioZapatillas {



    @PersistenceContext
    private EntityManager entityManager;

    @org.springframework.beans.factory.annotation.Autowired
    private ServicioImagenesProducto servicioImagenes;

    @Override
    public void registrarZapatilla(Zapatilla v) {
        MultipartFile[] imagenes = v.getFotoSubida() == null
                ? new MultipartFile[0] : new MultipartFile[]{v.getFotoSubida()};
        registrarZapatilla(v, imagenes, "new:0");
    }

    @Override
    public void registrarZapatilla(Zapatilla zapatilla, MultipartFile[] archivos, String imagenPrincipal) {
        List<MultipartFile> imagenes = archivosValidos(archivos);
        if (imagenes.isEmpty()) {
            throw new IllegalArgumentException("Selecciona al menos una imagen para el producto.");
        }
        if (zapatilla.getIdCategoria() == null) {
            throw new IllegalArgumentException("Selecciona una categoría para el producto.");
        }
        Categoria categoria = entityManager.find(Categoria.class, zapatilla.getIdCategoria());
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría seleccionada no existe.");
        }
        zapatilla.setCategoria(categoria);
        zapatilla.setImagenPortada(null);
        entityManager.persist(zapatilla);
        List<ImagenProducto> nuevasImagenes = guardarArchivos(zapatilla, imagenes);
        seleccionarImagenPrincipal(zapatilla, nuevasImagenes, imagenPrincipal, true);
    }

    @Override
    public List<Zapatilla> obtenerZapatillas() {
        return entityManager.createQuery("select v from Zapatilla v").getResultList();
    }

    @Override
    public Page<Zapatilla> buscarZapatillas(String busqueda, int pagina, int tamano) {
        String termino = busqueda == null ? "" : busqueda.trim().toLowerCase(Locale.ROOT);
        String coincidencia = "%" + termino + "%";
        String filtro = "(:termino = '' or lower(z.modelo) like :coincidencia "
                + "or lower(z.marca) like :coincidencia or lower(z.descripcion) like :coincidencia "
                + "or lower(z.color) like :coincidencia or lower(z.sku) like :coincidencia "
                + "or lower(c.nombre) like :coincidencia)";
        Long total = entityManager.createQuery(
                        "select count(z) from Zapatilla z join z.categoria c where " + filtro, Long.class)
                .setParameter("termino", termino)
                .setParameter("coincidencia", coincidencia)
                .getSingleResult();

        int tamanoPagina = Math.max(1, Math.min(tamano, 50));
        int paginaSolicitada = Math.max(0, pagina);
        int ultimaPagina = total == 0 ? 0 : (int) ((total - 1) / tamanoPagina);
        int paginaActual = Math.min(paginaSolicitada, ultimaPagina);
        List<Zapatilla> zapatillas = entityManager.createQuery(
                        "select z from Zapatilla z join fetch z.categoria c where " + filtro + " order by z.id",
                        Zapatilla.class)
                .setParameter("termino", termino)
                .setParameter("coincidencia", coincidencia)
                .setFirstResult(paginaActual * tamanoPagina)
                .setMaxResults(tamanoPagina)
                .getResultList();
        return new PageImpl<>(zapatillas, PageRequest.of(paginaActual, tamanoPagina), total);
    }




    @Override
    public Zapatilla obtenerZapatillaPorid(long id) {
        List<Zapatilla> resultado = entityManager.createQuery(
                        "select distinct z from Zapatilla z left join fetch z.imagenes where z.id = :id",
                        Zapatilla.class)
                .setParameter("id", id)
                .getResultList();
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    @Override
    public void borrarZapatilla(long id) {
        // En JPA la forma mas comun de borrar un registro,
        // Es pedirlo, comprobar que existe y luego borrarlo
        Zapatilla zapatillaAborrar = entityManager.find(Zapatilla.class, id);
        if (zapatillaAborrar != null) {
            for (ImagenProducto imagen : new ArrayList<>(zapatillaAborrar.getImagenes())) {
                eliminarDespuesDeConfirmar(imagen.getArchivo(), imagen.getMiniatura());
            }
            entityManager.remove(zapatillaAborrar);
        } else {
            System.out.println("id no encontrada");
        }
    }

    @Override
    public void guardarCambiosZapatilla(Zapatilla zapatillaEditar) throws Exception {
        MultipartFile[] archivos = zapatillaEditar.getFotoSubida() == null
                ? new MultipartFile[0] : new MultipartFile[]{zapatillaEditar.getFotoSubida()};
        guardarCambiosZapatilla(zapatillaEditar, archivos, null);
    }

    @Override
    public void guardarCambiosZapatilla(Zapatilla recibido, MultipartFile[] archivos,
                                        String imagenPrincipal) {
        Zapatilla zapatilla = obtenerZapatillaPorid(recibido.getId());
        if (zapatilla == null) {
            throw new IllegalArgumentException("No se encontró el producto que quieres editar.");
        }
        if (recibido.getIdCategoria() == null) {
            throw new IllegalArgumentException("Selecciona una categoría para el producto.");
        }
        Categoria categoria = entityManager.find(Categoria.class, recibido.getIdCategoria());
        if (categoria == null) {
            throw new IllegalArgumentException("La categoría seleccionada no existe.");
        }
        zapatilla.setModelo(recibido.getModelo());
        zapatilla.setMarca(recibido.getMarca());
        zapatilla.setPrecio(recibido.getPrecio());
        zapatilla.setColor(recibido.getColor());
        zapatilla.setTalla(recibido.getTalla());
        zapatilla.setDescripcion(recibido.getDescripcion());
        zapatilla.setSku(recibido.getSku());
        zapatilla.setCategoria(categoria);

        List<ImagenProducto> nuevasImagenes = guardarArchivos(zapatilla, archivosValidos(archivos));
        if (imagenPrincipal != null && !imagenPrincipal.isBlank()) {
            seleccionarImagenPrincipal(zapatilla, nuevasImagenes, imagenPrincipal, false);
        } else if (zapatilla.getImagenes().stream().noneMatch(ImagenProducto::isPrincipal)
                && zapatilla.getImagenPortada() == null && !zapatilla.getImagenes().isEmpty()) {
            zapatilla.getImagenes().get(0).setPrincipal(true);
        }
    }

    private List<MultipartFile> archivosValidos(MultipartFile[] archivos) {
        if (archivos == null) {
            return List.of();
        }
        return java.util.Arrays.stream(archivos)
                .filter(archivo -> archivo != null && !archivo.isEmpty())
                .toList();
    }

    private List<ImagenProducto> guardarArchivos(Zapatilla zapatilla, List<MultipartFile> archivos) {
        List<ImagenProducto> imagenes = new ArrayList<>();
        for (MultipartFile archivo : archivos) {
            ServicioImagenesProducto.ArchivosImagen guardados = servicioImagenes.procesarYGuardar(archivo);
            registrarLimpiezaSiHayRollback(guardados.archivo(), guardados.miniatura());
            ImagenProducto imagen = new ImagenProducto();
            imagen.setZapatilla(zapatilla);
            imagen.setArchivo(guardados.archivo());
            imagen.setMiniatura(guardados.miniatura());
            zapatilla.getImagenes().add(imagen);
            entityManager.persist(imagen);
            imagenes.add(imagen);
        }
        return imagenes;
    }

    private void seleccionarImagenPrincipal(Zapatilla zapatilla, List<ImagenProducto> nuevasImagenes,
                                            String seleccion, boolean crear) {
        String opcion = seleccion == null || seleccion.isBlank()
                ? (crear ? "new:0" : null) : seleccion;
        if (opcion == null) {
            return;
        }
        if ("legacy".equals(opcion)) {
            if (zapatilla.getImagenPortada() == null) {
                throw new IllegalArgumentException("La imagen heredada ya no está disponible.");
            }
            zapatilla.getImagenes().forEach(imagen -> imagen.setPrincipal(false));
            return;
        }

        ImagenProducto principal;
        if (opcion.startsWith("existing:")) {
            long idImagen = leerIdentificador(opcion.substring("existing:".length()));
            principal = zapatilla.getImagenes().stream()
                    .filter(imagen -> imagen.getId() != null && imagen.getId() == idImagen)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("La imagen principal seleccionada no pertenece a este producto."));
        } else if (opcion.startsWith("new:")) {
            int indice = (int) leerIdentificador(opcion.substring("new:".length()));
            if (indice < 0 || indice >= nuevasImagenes.size()) {
                throw new IllegalArgumentException("La imagen principal seleccionada no es válida.");
            }
            principal = nuevasImagenes.get(indice);
        } else {
            throw new IllegalArgumentException("La imagen principal seleccionada no es válida.");
        }
        zapatilla.getImagenes().forEach(imagen -> imagen.setPrincipal(imagen == principal));
    }

    private long leerIdentificador(String valor) {
        try {
            return Long.parseLong(valor);
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException("La imagen seleccionada no es válida.", error);
        }
    }

    @Override
    public void eliminarImagen(long idZapatilla, String identificadorImagen) {
        Zapatilla zapatilla = obtenerZapatillaPorid(idZapatilla);
        if (zapatilla == null) {
            throw new IllegalArgumentException("No se encontró el producto.");
        }
        if ("legacy".equals(identificadorImagen)) {
            if (zapatilla.getImagenes().isEmpty()) {
                throw new IllegalArgumentException("El producto debe conservar al menos una imagen.");
            }
            zapatilla.setImagenPortada(null);
            if (zapatilla.getImagenes().stream().noneMatch(ImagenProducto::isPrincipal)) {
                zapatilla.getImagenes().get(0).setPrincipal(true);
            }
            return;
        }
        long idImagen = leerIdentificador(identificadorImagen);
        ImagenProducto imagen = zapatilla.getImagenes().stream()
                .filter(candidata -> candidata.getId() != null && candidata.getId() == idImagen)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La imagen no pertenece a este producto."));
        if (zapatilla.getImagenes().size() == 1 && zapatilla.getImagenPortada() == null) {
            throw new IllegalArgumentException("El producto debe conservar al menos una imagen.");
        }
        boolean eraPrincipal = imagen.isPrincipal();
        zapatilla.getImagenes().remove(imagen);
        entityManager.remove(imagen);
        eliminarDespuesDeConfirmar(imagen.getArchivo(), imagen.getMiniatura());
        if (eraPrincipal && zapatilla.getImagenPortada() == null && !zapatilla.getImagenes().isEmpty()) {
            zapatilla.getImagenes().get(0).setPrincipal(true);
        }
    }

    private void registrarLimpiezaSiHayRollback(String... archivos) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int estado) {
                    if (estado != STATUS_COMMITTED) {
                        for (String archivo : archivos) {
                            servicioImagenes.borrar(archivo);
                        }
                    }
                }
            });
        }
    }

    private void eliminarDespuesDeConfirmar(String... archivos) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (String archivo : archivos) {
                    servicioImagenes.borrar(archivo);
                }
            }
        });
    }

    @Override
    public byte[] obtenerImagenPrincipal(long idZapatilla, boolean miniatura) {
        List<ImagenProducto> candidatas = entityManager.createQuery(
                        "select i from ImagenProducto i where i.zapatilla.id = :id order by i.id",
                        ImagenProducto.class)
                .setParameter("id", idZapatilla)
                .getResultList();
        ImagenProducto principal = candidatas.stream().filter(ImagenProducto::isPrincipal)
                .findFirst().orElse(null);
        if (principal != null) {
            return servicioImagenes.leer(miniatura ? principal.getMiniatura() : principal.getArchivo());
        }
        Zapatilla zapatilla = entityManager.find(Zapatilla.class, idZapatilla);
        if (zapatilla == null) {
            return null;
        }
        if (zapatilla.getImagenPortada() != null) {
            byte[] imagenLegada = zapatilla.getImagenPortada();
            return miniatura ? servicioImagenes.generarMiniatura(imagenLegada) : imagenLegada;
        }
        if (!candidatas.isEmpty()) {
            ImagenProducto primera = candidatas.get(0);
            return servicioImagenes.leer(miniatura ? primera.getMiniatura() : primera.getArchivo());
        }
        return null;
    }

    @Override
    public byte[] obtenerImagen(long idImagen, boolean miniatura) {
        ImagenProducto imagen = entityManager.find(ImagenProducto.class, idImagen);
        if (imagen == null) {
            return null;
        }
        return servicioImagenes.leer(miniatura ? imagen.getMiniatura() : imagen.getArchivo());
    }


    @Override
    public List<Map<String, Object>> obtenerJSONzapatillas() {
        // con JPA hemos asociado categoria a producto
        // problema: el json recibido en portada no es correcto
        // solucion: dejar claro en una consulta a base de datos que informacion
        // va a formar el json que necesitamos en portada

        //Map de String object es un conjunto de elementos
        // donde cada elemento tiene un string y un Object
        // un Object en java es cualquier tipo de dato
        // Object es la clase padre de todas las clases en java
//        Map<String,Object> json =  new HashMap<>();
//        json.put("titulo", "GTAV");
//        json.put("precio",45);
        // json.put(56,50); //da error porque lo primero no es un String
        //para tener el control total, vamos a usar sql
        // asi preparo una sql para JPA
        Query consulta = entityManager.createNativeQuery(
                        ConstantesSQL.SQL_OBTENER_ZAPATILLAS_JSON
                );
        //usar la consulta directamente es mucho mas lioso
        //que lo siguiente
        NativeQueryImpl nativeQuery = (NativeQueryImpl) consulta;
        nativeQuery.setTupleTransformer(AliasToEntityMapResultTransformer.INSTANCE);
        return nativeQuery.getResultList();
    }

    @Override
    public Page<Map<String, Object>> obtenerJSONzapatillas(String busqueda, int pagina, int tamano) {
        Page<Zapatilla> resultado = buscarZapatillas(busqueda, pagina, tamano);
        List<Map<String, Object>> zapatillas = resultado.getContent().stream()
                .map(this::convertirZapatillaAJson)
                .toList();
        return new PageImpl<>(zapatillas, resultado.getPageable(), resultado.getTotalElements());
    }

    private Map<String, Object> convertirZapatillaAJson(Zapatilla zapatilla) {
        Map<String, Object> json = new HashMap<>();
        json.put("ID", zapatilla.getId());
        json.put("MODELO", zapatilla.getModelo());
        json.put("MARCA", zapatilla.getMarca());
        json.put("PRECIO", zapatilla.getPrecio());
        json.put("COLOR", zapatilla.getColor());
        json.put("TALLA", zapatilla.getTalla());
        json.put("DESCRIPCION", zapatilla.getDescripcion());
        json.put("SKU", zapatilla.getSku());
        json.put("CATEGORIA", zapatilla.getCategoria().getNombre());
        json.put("IMAGENES", obtenerGaleriaJson(zapatilla));
        return json;
    }

    private List<Map<String, Object>> obtenerGaleriaJson(Zapatilla zapatilla) {
        List<Map<String, Object>> galeria = new ArrayList<>();
        if (zapatilla.getImagenPortada() != null) {
            Map<String, Object> imagenLegada = new HashMap<>();
            imagenLegada.put("IMAGEN", "/mostrar_imagen?id=" + zapatilla.getId());
            imagenLegada.put("MINIATURA", "/mostrar_miniatura?id=" + zapatilla.getId());
            imagenLegada.put("PRINCIPAL", zapatilla.getImagenes().stream().noneMatch(ImagenProducto::isPrincipal));
            galeria.add(imagenLegada);
        }
        for (ImagenProducto imagen : zapatilla.getImagenes()) {
            Map<String, Object> item = new HashMap<>();
            item.put("IMAGEN", "/imagenes-producto/" + imagen.getId());
            item.put("MINIATURA", "/miniaturas-producto/" + imagen.getId());
            item.put("PRINCIPAL", imagen.isPrincipal());
            galeria.add(item);
        }
        return galeria;
    }



    @Override
    public Map<String, Object> obtenerJSONZapatillasPorId(Long id) {
        Zapatilla zapatilla = obtenerZapatillaPorid(id);
        if (zapatilla == null) {
            throw new IllegalArgumentException("No se encontró el producto solicitado.");
        }
        return convertirZapatillaAJson(zapatilla);
    }





}

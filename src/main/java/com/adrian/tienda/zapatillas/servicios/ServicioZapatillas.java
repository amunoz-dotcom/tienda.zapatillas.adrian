package com.adrian.tienda.zapatillas.servicios;

import com.adrian.tienda.zapatillas.model.Zapatilla;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ServicioZapatillas {

    void registrarZapatilla(Zapatilla v);

    void registrarZapatilla(Zapatilla zapatilla, MultipartFile[] imagenes, String imagenPrincipal);

    List<Zapatilla> obtenerZapatillas();

    Page<Zapatilla> buscarZapatillas(String busqueda, int pagina, int tamano);

    Zapatilla obtenerZapatillaPorid(long id);

    void borrarZapatilla(long id);

    void guardarCambiosZapatilla(Zapatilla zapatillaEditar) throws Exception;

    void guardarCambiosZapatilla(Zapatilla zapatillaEditar, MultipartFile[] imagenes,
                                 String imagenPrincipal) throws Exception;

    void eliminarImagen(long idZapatilla, String identificadorImagen);

    byte[] obtenerImagenPrincipal(long idZapatilla, boolean miniatura);

    byte[] obtenerImagen(long idImagen, boolean miniatura);


    List<Map<String, Object>> obtenerJSONzapatillas();

    Page<Map<String, Object>> obtenerJSONzapatillas(String busqueda, int pagina, int tamano);


    Map<String, Object> obtenerJSONZapatillasPorId(Long id);
}

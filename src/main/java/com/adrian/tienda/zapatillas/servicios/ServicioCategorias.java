package com.adrian.tienda.zapatillas.servicios;

import com.adrian.tienda.zapatillas.model.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ServicioCategorias {

    List<Categoria> obtenerCategorias();

    Page<Categoria> obtenerCategorias(int pagina, int tamano);

    void registrarCategoria(Categoria categoria);

    void borrarCategoria(int id);

    Categoria obtenerCategoriaPorId(int id);
}

package com.adrian.tienda.zapatillas.serviciosImpl;

import com.adrian.tienda.zapatillas.model.Categoria;
import com.adrian.tienda.zapatillas.servicios.ServicioCategorias;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Service
@Transactional
public class ServicioCategoriasImplJPA implements ServicioCategorias {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Categoria> obtenerCategorias() {
        return entityManager.createQuery("select c from Categoria c", Categoria.class).getResultList();
    }

    @Override
    public Page<Categoria> obtenerCategorias(int pagina, int tamano) {
        Long total = entityManager.createQuery("select count(c) from Categoria c", Long.class).getSingleResult();
        int tamanoPagina = Math.max(1, Math.min(tamano, 50));
        int ultimaPagina = total == 0 ? 0 : (int) ((total - 1) / tamanoPagina);
        int paginaActual = Math.min(Math.max(0, pagina), ultimaPagina);
        List<Categoria> categorias = entityManager.createQuery(
                        "select c from Categoria c order by c.id", Categoria.class)
                .setFirstResult(paginaActual * tamanoPagina)
                .setMaxResults(tamanoPagina)
                .getResultList();
        return new PageImpl<>(categorias, PageRequest.of(paginaActual, tamanoPagina), total);
    }

    @Override
    public void registrarCategoria(Categoria c) {
        if (c.getId() == 0) {
            entityManager.persist(c); // Nueva categoría
        } else {
            entityManager.merge(c);   // Edición de categoría existente
        }
    }

    @Override
    public void borrarCategoria(int id) {
        Categoria c = obtenerCategoriaPorId(id);
        if (c != null) {
            entityManager.remove(c);
        }
    }

    @Override
    public Categoria obtenerCategoriaPorId(int id) {
        return entityManager.find(Categoria.class, id);
    }
}
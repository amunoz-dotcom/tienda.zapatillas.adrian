package com.adrian.tienda.zapatillas.serviciosImpl;

import com.adrian.tienda.zapatillas.constantesSQL.ConstantesSQL;
import com.adrian.tienda.zapatillas.model.Carrito;
import com.adrian.tienda.zapatillas.model.ProductoCarrito;
import com.adrian.tienda.zapatillas.model.Usuario;
import com.adrian.tienda.zapatillas.model.Zapatilla;
import com.adrian.tienda.zapatillas.servicios.ServicioCarrito;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.hibernate.query.sql.internal.NativeQueryImpl;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ServicioCarritoImplJPA implements ServicioCarrito {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void agregarProducto(long idUsuario, long idProducto, int cantidad) {
        Usuario uBaseDatos = entityManager.find(Usuario.class, idUsuario);

        // Uso de getResultList() para evitar NoResultException si el carrito no existe aún
        List<Carrito> carritos = entityManager.createQuery(
                        "SELECT c FROM Carrito c WHERE c.usuario.id = :idUsuario", Carrito.class)
                .setParameter("idUsuario", idUsuario)
                .getResultList();

        Carrito c = carritos.isEmpty() ? null : carritos.get(0);

        if (c == null) {
            c = new Carrito();
            c.setUsuario(uBaseDatos);
            entityManager.persist(c);
        }

        boolean productoEnCarrito = false;
        if (c.getProductosCarrito() != null) {
            for (ProductoCarrito pc : c.getProductosCarrito()) {
                if (pc.getZapatilla().getId() == idProducto) {
                    productoEnCarrito = true;
                    pc.setCantidad(pc.getCantidad() + cantidad);
                    entityManager.merge(pc);
                    break;
                }
            }
        }

        if (!productoEnCarrito) {
            ProductoCarrito pc = new ProductoCarrito();
            pc.setCarrito(c);
            pc.setZapatilla(entityManager.find(Zapatilla.class, idProducto));
            pc.setCantidad(cantidad);
            entityManager.persist(pc);
        }
    }

    @Override
    public List<Map<String, Object>> obtenerProductosCarrito(long idUsuario) {
        List<Carrito> carritos = entityManager.createQuery(
                        "SELECT c FROM Carrito c WHERE c.usuario.id = :idUsuario", Carrito.class)
                .setParameter("idUsuario", idUsuario)
                .getResultList();

        Carrito c = carritos.isEmpty() ? null : carritos.get(0);
        List<Map<String, Object>> res = null;

        if (c != null) {
            Query query = entityManager.createNativeQuery(
                    ConstantesSQL.SQL_OBTENER_PRODUCTOS_CARRITO_JSON
            );
            query.setParameter("carrito_id", c.getId());

            NativeQueryImpl nativeQuery = (NativeQueryImpl) query;
            nativeQuery.setTupleTransformer(AliasToEntityMapResultTransformer.INSTANCE);
            res = nativeQuery.getResultList();
        }
        return res;
    }

    @Override
    public void borrarProducto(long idUsuario, long idProducto) {
        List<Carrito> carritos = entityManager.createQuery(
                        "SELECT c FROM Carrito c WHERE c.usuario.id = :idUsuario", Carrito.class)
                .setParameter("idUsuario", idUsuario)
                .getResultList();

        if (!carritos.isEmpty()) {
            Carrito c = carritos.get(0);
            if (c.getProductosCarrito() != null) {
                for (ProductoCarrito pc : c.getProductosCarrito()) {
                    if (pc.getZapatilla().getId() == idProducto) {
                        entityManager.remove(pc);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void actualizarCantidad(long idUsuario, long idProducto, int cantidad) {
        List<Carrito> carritos = entityManager.createQuery(
                        "SELECT c FROM Carrito c WHERE c.usuario.id = :idUsuario", Carrito.class)
                .setParameter("idUsuario", idUsuario)
                .getResultList();

        if (!carritos.isEmpty()) {
            Carrito c = carritos.get(0);
            if (c.getProductosCarrito() != null) {
                for (ProductoCarrito pc : c.getProductosCarrito()) {
                    if (pc.getZapatilla().getId() == idProducto) {
                        if (cantidad <= 0) {
                            entityManager.remove(pc);
                        } else {
                            pc.setCantidad(cantidad);
                            entityManager.merge(pc);
                        }
                        break;
                    }
                }
            }
        }
    }
}
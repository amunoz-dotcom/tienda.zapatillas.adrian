package com.adrian.tienda.zapatillas.serviciosImpl;

import com.adrian.tienda.zapatillas.constantesPedidos.EstadosPedido;
import com.adrian.tienda.zapatillas.constantesSQL.ConstantesSQL;
import com.adrian.tienda.zapatillas.model.*;
import com.adrian.tienda.zapatillas.responseData.ResumenPedido;
import com.adrian.tienda.zapatillas.servicios.ServicioCarrito;
import com.adrian.tienda.zapatillas.servicios.ServicioPedidos;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.YearMonth;
import java.util.List;
import java.util.Arrays;

@Service
@Transactional
public class ServicioPedidosImplJPA implements ServicioPedidos {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ServicioCarrito servicioCarrito;

    @Override
    public List<Pedido> obtenerPedidos() {
        return entityManager.createQuery("SELECT p FROM Pedido p", Pedido.class).getResultList();
    }

    @Override
    public Page<Pedido> obtenerPedidos(int pagina, int tamano) {
        Long total = entityManager.createQuery("select count(p) from Pedido p", Long.class).getSingleResult();
        int tamanoPagina = Math.max(1, Math.min(tamano, 50));
        int ultimaPagina = total == 0 ? 0 : (int) ((total - 1) / tamanoPagina);
        int paginaActual = Math.min(Math.max(0, pagina), ultimaPagina);
        List<Pedido> pedidos = entityManager.createQuery(
                        "select p from Pedido p order by p.id desc", Pedido.class)
                .setFirstResult(paginaActual * tamanoPagina)
                .setMaxResults(tamanoPagina)
                .getResultList();
        return new PageImpl<>(pedidos, PageRequest.of(paginaActual, tamanoPagina), total);
    }

    @Override
    public List<Pedido> obtenerPedidosUsuario(long idUsuario) {
        return entityManager.createQuery(
                        "select p from Pedido p where p.usuario.id = :usuarioId " +
                                "and p.estado <> :estadoEnProceso order by p.id desc",
                        Pedido.class)
                .setParameter("usuarioId", idUsuario)
                .setParameter("estadoEnProceso", EstadosPedido.EnProceso.name())
                .getResultList();
    }

    @Override
    public Pedido obtenerPedidoPorId(long id) {
        return entityManager.find(Pedido.class, id);
    }

    @Override
    public void actualizarEstadoPedido(long id, String estado) {
        if (estado == null || Arrays.stream(com.adrian.tienda.zapatillas.constantesPedidos.EstadosPedido.values())
                .noneMatch(estadoPedido -> estadoPedido.name().equals(estado))) {
            throw new IllegalArgumentException("El estado indicado no es válido.");
        }
        Pedido p = entityManager.find(Pedido.class, id);
        if (p == null) {
            throw new IllegalArgumentException("No se encontró el pedido.");
        }
        p.setEstado(estado);
    }

    // operaciones con la parte publica

    @Override
    public void procesarPaso1(String nombreCompleto, String direccion, String poblacion,
                              String provincia, String codigoPostal, String telefono, long idUsuario) {
        validarTexto(nombreCompleto, "Indica el nombre completo.");
        validarTexto(direccion, "Indica la dirección.");
        validarTexto(poblacion, "Indica la población.");
        validarTexto(provincia, "Indica la provincia.");
        validarTexto(codigoPostal, "Indica el código postal.");
        validarTexto(telefono, "Indica el teléfono.");
        Pedido p = obtenerPedidoActual(idUsuario);
        p.setNombreCompleto(nombreCompleto);
        p.setDireccion(direccion);
        p.setPoblacion(poblacion);
        p.setProvincia(provincia);
        p.setCodigoPostal(codigoPostal);
        p.setTelefono(telefono);
        entityManager.merge(p);
    }

    @Override
    public void procesarPasoPreferencias(String regaloPromocional, String tipoEmbalaje,
                                         String tallaCalcetinesRegalo, long idUsuario) {
        validarTexto(regaloPromocional, "Selecciona un regalo.");
        validarTexto(tipoEmbalaje, "Selecciona un embalaje.");
        validarTexto(tallaCalcetinesRegalo, "Selecciona la talla de los calcetines.");
        Pedido p = obtenerPedidoActual(idUsuario);
        p.setRegaloPromocional(regaloPromocional);
        p.setTipoEmbalaje(tipoEmbalaje);
        p.setTallaCalcetinesRegalo(tallaCalcetinesRegalo);
        entityManager.merge(p);
    }

    @Override
    public void procesarPaso2(String titular, String numero, String tipoTarjeta, String fechaCaducidad, long idUsuario) {
        validarTexto(titular, "Indica el titular de la tarjeta.");
        validarTexto(numero, "Indica el número de la tarjeta.");
        validarTexto(tipoTarjeta, "Selecciona el tipo de tarjeta.");
        validarTexto(fechaCaducidad, "Indica la fecha de caducidad.");
        String numeroNormalizado = numero.replaceAll("[\\s-]", "");
        if (!numeroNormalizado.matches("\\d{12,19}")) {
            throw new IllegalArgumentException("El número de tarjeta debe tener entre 12 y 19 dígitos.");
        }
        if (!numeroTarjetaValido(numeroNormalizado)) {
            throw new IllegalArgumentException("El número de tarjeta no es válido.");
        }
        if (!fechaCaducidad.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            throw new IllegalArgumentException("La fecha de caducidad debe tener el formato MM/AA.");
        }
        String[] partesCaducidad = fechaCaducidad.split("/");
        YearMonth caducidad = YearMonth.of(2000 + Integer.parseInt(partesCaducidad[1]),
                Integer.parseInt(partesCaducidad[0]));
        if (caducidad.isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("La tarjeta está caducada.");
        }
        if (!tipoTarjeta.equals("visa") && !tipoTarjeta.equals("master_card")) {
            throw new IllegalArgumentException("Selecciona un tipo de tarjeta válido.");
        }
        Pedido p = obtenerPedidoActual(idUsuario);
        p.setTitularTarjeta(titular);
        p.setNumeroTarjeta("**** **** **** " + numeroNormalizado.substring(numeroNormalizado.length() - 4));
        p.setTipoTarjeta(tipoTarjeta);
        p.setFechaCaducidad(fechaCaducidad);
        entityManager.merge(p);
    }

    @Override
    public ResumenPedido obtenerResumenPedido(long idUsuario) {
        Pedido p = obtenerPedidoActual(idUsuario);
        validarPedidoCompleto(p);
        List<java.util.Map<String, Object>> productos = servicioCarrito.obtenerProductosCarrito(idUsuario);
        if (productos == null || productos.isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío.");
        }
        ResumenPedido resumen = new ResumenPedido();
        resumen.setNombreCompleto(p.getNombreCompleto());
        resumen.setDireccion(p.getDireccion());
        resumen.setPoblacion(p.getPoblacion());
        resumen.setProvincia(p.getProvincia());
        resumen.setCodigoPostal(p.getCodigoPostal());
        resumen.setTelefono(p.getTelefono());

        resumen.setRegaloPromocional(p.getRegaloPromocional());
        resumen.setTipoEmbalaje(p.getTipoEmbalaje());
        resumen.setTallaCalcetinesRegalo(p.getTallaCalcetinesRegalo());

        resumen.setTipoTarjeta(p.getTipoTarjeta());
        resumen.setTitularTarjeta(p.getTitularTarjeta());
        resumen.setNumeroTarjeta(p.getNumeroTarjeta());
        resumen.setFechaCaducidad(p.getFechaCaducidad());
        resumen.setZapatillas(productos);
        return resumen;
    }

    @Override
    public void confirmarPedido(long idUsuario) {
        Pedido p = obtenerPedidoActual(idUsuario);
        validarPedidoCompleto(p);
        List<Carrito> carritos = entityManager.createQuery(
                        "SELECT c FROM Carrito c WHERE c.usuario.id = :idUsuario", Carrito.class)
                .setParameter("idUsuario", idUsuario)
                .getResultList();
        if (carritos.isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío.");
        }
        Carrito c = carritos.get(0);

        if (c.getProductosCarrito().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío.");
        }
        for (ProductoCarrito pc : c.getProductosCarrito()) {
            ProductoPedido pp = new ProductoPedido();
            pp.setCantidad(pc.getCantidad());
            pp.setZapatilla(pc.getZapatilla());
            pp.setPedido(p);
            p.getProductosPedido().add(pp);
            entityManager.persist(pp);
        }

        entityManager.createNativeQuery(ConstantesSQL.SQL_BORRAR_PRODUCTOS_CARRITO)
                .setParameter("carrito_id", c.getId())
                .executeUpdate();
        c.getProductosCarrito().clear();
        p.setEstado(EstadosPedido.Completado.toString());
        entityManager.merge(p);
    }

    private Pedido obtenerPedidoActual(long idUsuario) {
        Query consulta = entityManager.createQuery(
                        "select p from Pedido p where p.estado = :estado and p.usuario.id = :usuario_id order by p.id desc",
                        Pedido.class)
                .setParameter("estado", EstadosPedido.EnProceso.name())
                .setParameter("usuario_id", idUsuario)
                .setMaxResults(1);
        List<Pedido> resultadoConsulta = consulta.getResultList();
        if (!resultadoConsulta.isEmpty()) {
            return resultadoConsulta.get(0);
        }
        Usuario uBaseDatos = entityManager.find(Usuario.class, idUsuario);
        if (uBaseDatos == null) {
            throw new IllegalArgumentException("No se encontró el usuario.");
        }
        Pedido pedidoEnProceso = new Pedido();
        pedidoEnProceso.setUsuario(uBaseDatos);
        pedidoEnProceso.setEstado(EstadosPedido.EnProceso.name());
        entityManager.persist(pedidoEnProceso);
        return pedidoEnProceso;
    }

    private void validarTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private boolean numeroTarjetaValido(String numero) {
        int suma = 0;
        boolean duplicar = false;
        for (int indice = numero.length() - 1; indice >= 0; indice--) {
            int digito = numero.charAt(indice) - '0';
            if (duplicar) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }
            suma += digito;
            duplicar = !duplicar;
        }
        return suma % 10 == 0;
    }

    private void validarPedidoCompleto(Pedido pedido) {
        if (pedido.getNombreCompleto() == null || pedido.getNombreCompleto().isBlank()
                || pedido.getDireccion() == null || pedido.getDireccion().isBlank()
                || pedido.getPoblacion() == null || pedido.getPoblacion().isBlank()
                || pedido.getProvincia() == null || pedido.getProvincia().isBlank()
                || pedido.getCodigoPostal() == null || pedido.getCodigoPostal().isBlank()
                || pedido.getTelefono() == null || pedido.getTelefono().isBlank()
                || pedido.getTitularTarjeta() == null || pedido.getTitularTarjeta().isBlank()
                || pedido.getNumeroTarjeta() == null || pedido.getNumeroTarjeta().isBlank()
                || pedido.getTipoTarjeta() == null || pedido.getTipoTarjeta().isBlank()
                || pedido.getFechaCaducidad() == null || pedido.getFechaCaducidad().isBlank()
                || pedido.getRegaloPromocional() == null || pedido.getRegaloPromocional().isBlank()
                || pedido.getTipoEmbalaje() == null || pedido.getTipoEmbalaje().isBlank()
                || pedido.getTallaCalcetinesRegalo() == null || pedido.getTallaCalcetinesRegalo().isBlank()) {
            throw new IllegalArgumentException("Completa todos los pasos del pedido antes de confirmarlo.");
        }
    }
}
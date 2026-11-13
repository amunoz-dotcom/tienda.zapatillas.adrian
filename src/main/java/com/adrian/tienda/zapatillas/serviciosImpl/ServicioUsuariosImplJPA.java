package com.adrian.tienda.zapatillas.serviciosImpl;

import com.adrian.tienda.zapatillas.model.Usuario;
import com.adrian.tienda.zapatillas.servicios.ServicioUsuarios;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Service
@Transactional
public class ServicioUsuariosImplJPA implements ServicioUsuarios {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Usuario> obtenerUsuarios() {
        return entityManager.createQuery("select u from Usuario u", Usuario.class).getResultList();
    }

    @Override
    public Page<Usuario> obtenerUsuarios(int pagina, int tamano) {
        Long total = entityManager.createQuery("select count(u) from Usuario u", Long.class).getSingleResult();
        int tamanoPagina = Math.max(1, Math.min(tamano, 50));
        int ultimaPagina = total == 0 ? 0 : (int) ((total - 1) / tamanoPagina);
        int paginaActual = Math.min(Math.max(0, pagina), ultimaPagina);
        List<Usuario> usuarios = entityManager.createQuery("select u from Usuario u order by u.id", Usuario.class)
                .setFirstResult(paginaActual * tamanoPagina)
                .setMaxResults(tamanoPagina)
                .getResultList();
        return new PageImpl<>(usuarios, PageRequest.of(paginaActual, tamanoPagina), total);
    }

    @Override
    public void registrarUsuario(Usuario usuario) {
        if (usuario.getId() == 0) {
            entityManager.persist(usuario); // Crear
        } else {
            entityManager.merge(usuario);   // Actualizar
        }
    }

    @Override
    public void borrarUsuario(long id) {
        Usuario u = obtenerUsuarioPorId(id);
        if (u != null) {
            entityManager.remove(u);
        }
    }

    @Override
    public Usuario obtenerUsuarioPorId(long id) {
        return entityManager.find(Usuario.class, id);
    }

    @Override
    public Usuario obtenerUsuarioPorEmailYpass(String email, String pass) {
        Query query =
                entityManager.createQuery(
                        "select u from Usuario u where u.email = :email and u.pass = :pass");
        //la anterior consulta de jpa seria como la sql:
        // select * from usuario where email = ? and pass = ?
        query.setParameter("email", email);//asi asigno el :email
        query.setParameter("pass", pass); // asi asigno el :pass
        List<Usuario> res = query.getResultList();

        if( res.size() == 0 ){
            return null;
        }
        return res.get(0);
    }

    @Override
    public void actualizarDatosPersonales(long id, String nombre, String email, String direccion,
                                          String nuevaPass, byte[] avatar) {
        if (nombre == null || nombre.isBlank() || email == null || email.isBlank()
                || direccion == null || direccion.isBlank()) {
            throw new IllegalArgumentException("Completa el nombre, el email y la dirección.");
        }
        String emailNormalizado = email.trim();
        if (!emailNormalizado.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("El formato del email no es válido.");
        }
        if (nuevaPass != null && !nuevaPass.isBlank()
                && (nuevaPass.length() < 4 || nuevaPass.length() > 100)) {
            throw new IllegalArgumentException("La contraseña debe tener entre 4 y 100 caracteres.");
        }

        Usuario usuario = entityManager.find(Usuario.class, id);
        if (usuario == null) {
            throw new IllegalArgumentException("No se encontró el usuario.");
        }
        Long emailEnUso = entityManager.createQuery(
                        "select count(u) from Usuario u where lower(u.email) = lower(:email) and u.id <> :id",
                        Long.class)
                .setParameter("email", emailNormalizado)
                .setParameter("id", id)
                .getSingleResult();
        if (emailEnUso > 0) {
            throw new IllegalArgumentException("Ese email ya está asociado a otra cuenta.");
        }

        usuario.setNombre(nombre.trim());
        usuario.setEmail(emailNormalizado);
        usuario.setDireccion(direccion.trim());
        if (nuevaPass != null && !nuevaPass.isBlank()) {
            usuario.setPass(nuevaPass);
        }
        if (avatar != null) {
            usuario.setAvatar(avatar);
        }
    }
}
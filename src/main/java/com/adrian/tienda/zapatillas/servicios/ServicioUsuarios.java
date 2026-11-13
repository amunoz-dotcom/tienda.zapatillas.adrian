package com.adrian.tienda.zapatillas.servicios;

import com.adrian.tienda.zapatillas.model.Usuario;
import org.springframework.data.domain.Page;
import java.util.List;

public interface ServicioUsuarios {

    List<Usuario> obtenerUsuarios();

    Page<Usuario> obtenerUsuarios(int pagina, int tamano);

    void registrarUsuario(Usuario usuario);

    void borrarUsuario(long id);

    Usuario obtenerUsuarioPorId(long id);

    Usuario obtenerUsuarioPorEmailYpass(String email, String pass);

    void actualizarDatosPersonales(long id, String nombre, String email, String direccion,
                                   String nuevaPass, byte[] avatar);

}
package com.adrian.tienda.zapatillas.controllers;

import com.adrian.tienda.zapatillas.model.Usuario;
import com.adrian.tienda.zapatillas.servicios.ServicioUsuarios;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

@Controller
@RequestMapping("/admin")
public class ControladorUsuarios {

    @Autowired
    private ServicioUsuarios servicioUsuarios;

    // Listar
    @GetMapping("/gestionarUsuarios")
    public String gestionarUsuarios(@RequestParam(defaultValue = "0") int pagina, Model model) {
        Page<Usuario> usuarios = servicioUsuarios.obtenerUsuarios(pagina, 10);
        model.addAttribute("usuarios", usuarios.getContent());
        model.addAttribute("pagina", usuarios);
        return "admin/gestionar_usuarios";
    }

    // Formulario de Registro (Nuevo)
    @GetMapping("/registrarUsuario")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "admin/registro_usuario";
    }

    // Formulario para Editar
    @GetMapping("/editarUsuario")
    public String editarUsuario(@RequestParam("id") long id, Model model) {
        Usuario usuario = servicioUsuarios.obtenerUsuarioPorId(id);
        model.addAttribute("usuario", usuario);
        return "admin/registro_usuario";
    }

    // Guardar o Actualizar (con validación de errores)
    @PostMapping("/guardarUsuario")
    public String guardarUsuario(@Valid @ModelAttribute("usuario") Usuario usuario,
                                 BindingResult result,
                                 Model model) {
        if (result.hasErrors()) {
            return "admin/registro_usuario";
        }
        servicioUsuarios.registrarUsuario(usuario);
        return "redirect:/admin/gestionarUsuarios";
    }

    // Borrar
    @GetMapping("/borrarUsuario")
    public String borrarUsuario(@RequestParam("id") long id) {
        servicioUsuarios.borrarUsuario(id);
        return "redirect:/admin/gestionarUsuarios";
    }
}
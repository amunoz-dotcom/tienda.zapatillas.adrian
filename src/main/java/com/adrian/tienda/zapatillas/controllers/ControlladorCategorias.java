package com.adrian.tienda.zapatillas.controllers;

import com.adrian.tienda.zapatillas.model.Categoria;
import com.adrian.tienda.zapatillas.servicios.ServicioCategorias;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

@Controller
@RequestMapping("/admin")
public class ControlladorCategorias {

    @Autowired
    private ServicioCategorias servicioCategorias;

    // Listar todas las categorías
    @GetMapping("/gestionarCategorias")
    public String gestionarCategorias(@RequestParam(defaultValue = "0") int pagina, Model model) {
        Page<Categoria> categorias = servicioCategorias.obtenerCategorias(pagina, 10);
        model.addAttribute("categorias", categorias.getContent());
        model.addAttribute("pagina", categorias);
        return "admin/gestionar_categorias";
    }

    // 1. Mostrar el formulario para crear una categoría
    @GetMapping("/registrarCategoria")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "admin/registro_categoria"; // Nombre del archivo HTML en templates/admin/
    }

    // 2. Recibir los datos del formulario y guardar la categoría
    @PostMapping("/guardarCategoria")
    public String guardarCategoria(@ModelAttribute("categoria") Categoria categoria) {
        servicioCategorias.registrarCategoria(categoria);
        return "redirect:/admin/gestionarCategorias";
    }

    @GetMapping("/editarCategoria")
    public String editarCategoria(@RequestParam("id") int id, Model model) {
        // 1. Buscamos la categoría existente en la base de datos por su ID
        Categoria c = servicioCategorias.obtenerCategoriaPorId(id);

        // 2. Pasamos la categoría cargada al modelo
        model.addAttribute("categoria", c);

        // 3. Reutilizamos el mismo formulario HTML de registro
        return "admin/registro_categoria";
    }

    // 3. Borrar una categoría por ID
    @GetMapping("/borrarCategoria")
    public String borrarCategoria(@RequestParam("id") int id) {
        servicioCategorias.borrarCategoria(id); // Ajusta según el método de tu servicio
        return "redirect:/admin/gestionarCategorias";
    }
}
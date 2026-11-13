package com.adrian.tienda.zapatillas.controllers;


import com.adrian.tienda.zapatillas.model.Zapatilla;
import com.adrian.tienda.zapatillas.servicios.ServicioCategorias;
import com.adrian.tienda.zapatillas.servicios.ServicioZapatillas;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("admin/")
public class ControlladorZapatillas {


    @Autowired
    private ServicioZapatillas servicioZapatillas;

    @Autowired
    private ServicioCategorias servicioCategorias;

    @Value("${app.product-images.min-width:300}")
    private int anchoMinimoImagen;

    @Value("${app.product-images.min-height:300}")
    private int altoMinimoImagen;

    @Value("${app.product-images.max-width:4000}")
    private int anchoMaximoImagen;

    @Value("${app.product-images.max-height:4000}")
    private int altoMaximoImagen;

    @Value("${app.product-images.max-file-size:10485760}")
    private long pesoMaximoImagen;


    @RequestMapping("gestionarZapatillas")
    public String gestionarZapatillas(
            @RequestParam(defaultValue = "") String busqueda,
            @RequestParam(defaultValue = "0") int pagina,
            Model model){
        Page<Zapatilla> zapatillas = servicioZapatillas.buscarZapatillas(busqueda, pagina, 10);
        model.addAttribute("zapatillas", zapatillas.getContent());
        model.addAttribute("pagina", zapatillas);
        model.addAttribute("busqueda", busqueda.trim());
        return "admin/gestionar_productos";
    }

    @RequestMapping("registrarZapatilla")
    public String registrarZapatilla(Model model){
        Zapatilla nuevo = new Zapatilla();
        nuevo.setPrecio(1);
        model.addAttribute("nuevo", nuevo);
        model.addAttribute("categorias", servicioCategorias.obtenerCategorias());
        prepararLimitesImagen(model);

        return "admin/registro_producto";
    }


    @PostMapping("guardarZapatilla")
    public String guardarZapatilla(
            @ModelAttribute("nuevo")
            @Valid
            Zapatilla nuevaZapatilla,
            BindingResult resultadoValidacion,
            Model model,
            @RequestParam(name = "imagenes", required = false) MultipartFile[] imagenes,
            @RequestParam(name = "imagenPrincipal", defaultValue = "new:0") String imagenPrincipal) {
        System.out.println("recibido para registrar: " + nuevaZapatilla);
        if( resultadoValidacion.hasErrors()){
            model.addAttribute("categorias", servicioCategorias.obtenerCategorias());
            prepararLimitesImagen(model);
            return "admin/registro_producto";
        }
        try {
            servicioZapatillas.registrarZapatilla(nuevaZapatilla, imagenes, imagenPrincipal);
            return "admin/registro_producto_ok";
        } catch (IllegalArgumentException | IllegalStateException error) {
            model.addAttribute("categorias", servicioCategorias.obtenerCategorias());
            prepararLimitesImagen(model);
            model.addAttribute("errorImagen", error.getMessage());
            return "admin/registro_producto";
        }
    }

    @GetMapping("borrarZapatilla")
    public String borrarZapatilla(@RequestParam("id") Long id, Model model){
        //repaso: model es una variable donde meto lo que quiera que le llegue
        //a la vista indicada en el return
        System.out.println("id recibida para borrar registro: " + id);
        servicioZapatillas.borrarZapatilla(id);
        return "redirect:/admin/gestionarZapatillas";
    }


    @GetMapping("editarZapatilla")
    public String editarZapatilla(@RequestParam("id") Long id, Model model){

        Zapatilla vEditar = servicioZapatillas.obtenerZapatillaPorid(id);
        if (vEditar == null) {
            return "redirect:/admin/gestionarZapatillas";
        }
        vEditar.setIdCategoria(vEditar.getCategoria().getId());

        model.addAttribute("zapatillaEditar",
                vEditar);
        model.addAttribute("categorias", servicioCategorias.obtenerCategorias());
        prepararGaleriaEdicion(model, vEditar);
        prepararLimitesImagen(model);

        return "admin/editar_producto";
    }

    //el metodo va a atender la ruta guardarCambiosVideojuego
    // a esa ruta se llega al hacer click en el GUARDAR CAMBIOS del form de edicion
    @PostMapping("guardarCambiosZapatilla")
    public String guardarCambiosZapatilla(
            @ModelAttribute("zapatillaEditar") //th:object del form
            @Valid //para validar con las reglas puestas con @ en Videjuego.java
            Zapatilla zapatillaEditar, //objeto que contiene lo introducido en el form
            BindingResult resultadoValidacion,
            Model model,
            @RequestParam(name = "imagenes", required = false) MultipartFile[] imagenes,
            @RequestParam(name = "imagenPrincipal", required = false) String imagenPrincipal) throws Exception {
        if( resultadoValidacion.hasErrors()){
            model.addAttribute("categorias", servicioCategorias.obtenerCategorias());
            Zapatilla original = servicioZapatillas.obtenerZapatillaPorid(zapatillaEditar.getId());
            model.addAttribute("zapatillaEditar", zapatillaEditar);
            prepararGaleriaEdicion(model, original);
            prepararLimitesImagen(model);
            return "admin/editar_producto";
        }
        try {
            servicioZapatillas.guardarCambiosZapatilla(zapatillaEditar, imagenes, imagenPrincipal);
            return "redirect:/admin/gestionarZapatillas";
        } catch (IllegalArgumentException | IllegalStateException error) {
            Zapatilla original = servicioZapatillas.obtenerZapatillaPorid(zapatillaEditar.getId());
            model.addAttribute("categorias", servicioCategorias.obtenerCategorias());
            zapatillaEditar.setIdCategoria(original.getCategoria().getId());
            model.addAttribute("zapatillaEditar", zapatillaEditar);
            prepararGaleriaEdicion(model, original);
            model.addAttribute("errorImagen", error.getMessage());
            prepararLimitesImagen(model);
            return "admin/editar_producto";
        }
    }

    private void prepararGaleriaEdicion(Model model, Zapatilla producto) {
        model.addAttribute("imagenesActuales", producto.getImagenes());
        model.addAttribute("imagenLegada", producto.getImagenPortada() != null);
        Long principal = producto.getImagenes().stream()
                .filter(com.adrian.tienda.zapatillas.model.ImagenProducto::isPrincipal)
                .map(com.adrian.tienda.zapatillas.model.ImagenProducto::getId)
                .findFirst().orElse(null);
        boolean legadoPrincipal = principal == null && producto.getImagenPortada() != null;
        model.addAttribute("imagenLegadaPrincipal", legadoPrincipal);
        model.addAttribute("imagenPrincipalActual", legadoPrincipal ? "legacy"
                : principal == null ? "" : "existing:" + principal);
    }

    private void prepararLimitesImagen(Model model) {
        model.addAttribute("imagenMinAncho", anchoMinimoImagen);
        model.addAttribute("imagenMinAlto", altoMinimoImagen);
        model.addAttribute("imagenMaxAncho", anchoMaximoImagen);
        model.addAttribute("imagenMaxAlto", altoMaximoImagen);
        model.addAttribute("imagenMaxPesoMb", pesoMaximoImagen / 1024 / 1024);
    }

    @PostMapping("eliminarImagenZapatilla")
    public String eliminarImagenZapatilla(@RequestParam("idZapatilla") long idZapatilla,
                                          @RequestParam("imagen") String imagen,
                                          RedirectAttributes redirectAttributes) {
        try {
            servicioZapatillas.eliminarImagen(idZapatilla, imagen);
        } catch (IllegalArgumentException error) {
            redirectAttributes.addFlashAttribute("errorImagen", error.getMessage());
        }
        return "redirect:/admin/editarZapatilla?id=" + idZapatilla;
    }



}

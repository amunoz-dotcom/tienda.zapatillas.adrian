package com.adrian.tienda.zapatillas.controlladorREST;

import com.adrian.tienda.zapatillas.model.Usuario;
import com.adrian.tienda.zapatillas.servicios.ServicioUsuarios;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/restUsuarios")
public class UsuariosREST {


    @Autowired
    private ServicioUsuarios servicioUsuarios;

    @PostMapping("registrarUsuario")
    public String registrarUsuario(@Valid Usuario u, BindingResult result) {
        // Captura los errores de validación de las anotaciones (@NotEmpty, @NotNull, etc.)
        if (result.hasErrors()) {
            return result.getAllErrors().get(0).getDefaultMessage();
        }

        servicioUsuarios.registrarUsuario(u);
        return "Te has registrado correctamente, ya puedes identificarte";
    }

    //recordatorio: todos estos metodos se llaman desde el js de index.html
    @RequestMapping("identificarUsuario")
    public String identificarUsuario(
            @RequestParam("email") String email,
            @RequestParam("pass") String pass,
            HttpServletRequest request){
        //comprobar si hay un usuario con este email y pass en bd
        //si es asi, los datos son correctos
        //marcamos al usuario en cuestion que está identificado
        //usando una variable de sesion como hicimos con el interceptor
        //de administracion
        Usuario u = servicioUsuarios.obtenerUsuarioPorEmailYpass(email,pass);
        String respuesta = "";
        if( u != null ){
            u.setAvatar(null);
            request.getSession().setAttribute("usuario_identificado", u );
            respuesta = "ok," + u.getNombre() ;
        }else{
            respuesta = "email o pass incorrectos";
        }
        return respuesta;
    }

    @GetMapping("usuarioActual")
    public ResponseEntity<?> obtenerUsuarioActual(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Usuario usuario = session == null ? null : (Usuario) session.getAttribute("usuario_identificado");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        usuario = servicioUsuarios.obtenerUsuarioPorId(usuario.getId());
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("nombre", usuario.getNombre());
        datos.put("email", usuario.getEmail());
        datos.put("direccion", usuario.getDireccion());
        datos.put("tieneAvatar", usuario.getAvatar() != null && usuario.getAvatar().length > 0);
        return ResponseEntity.ok(datos);
    }

    @GetMapping("avatar")
    public ResponseEntity<byte[]> obtenerAvatar(HttpServletRequest request) {
        Usuario usuario = usuarioIdentificado(request);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        usuario = servicioUsuarios.obtenerUsuarioPorId(usuario.getId());
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        byte[] avatar = usuario.getAvatar();
        if (avatar == null || avatar.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.noStore())
                .body(avatar);
    }

    @PostMapping(value = "mis-datos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> actualizarMisDatos(
            @RequestParam String nombre,
            @RequestParam String email,
            @RequestParam String direccion,
            @RequestParam(required = false, defaultValue = "") String nuevaPass,
            @RequestParam(required = false) MultipartFile avatar,
            HttpServletRequest request) {
        Usuario usuario = usuarioIdentificado(request);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Debes identificarte para modificar tus datos.");
        }
        try {
            byte[] avatarValidado = avatar == null || avatar.isEmpty() ? null : convertirAvatar(avatar);
            servicioUsuarios.actualizarDatosPersonales(usuario.getId(), nombre, email, direccion,
                    nuevaPass, avatarValidado);
            Usuario usuarioActualizado = servicioUsuarios.obtenerUsuarioPorId(usuario.getId());
            usuarioActualizado.setAvatar(null);
            request.getSession(false).setAttribute("usuario_identificado", usuarioActualizado);
            return ResponseEntity.ok(Map.of("nombre", usuarioActualizado.getNombre()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (IOException ex) {
            return ResponseEntity.badRequest().body("No se pudo procesar la imagen del avatar.");
        }
    }

    private Usuario usuarioIdentificado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (Usuario) session.getAttribute("usuario_identificado");
    }

    private byte[] convertirAvatar(MultipartFile archivo) throws IOException {
        final long maximoBytes = 2 * 1024 * 1024;
        if (archivo.getSize() > maximoBytes) {
            throw new IllegalArgumentException("El avatar no puede superar los 2 MB.");
        }

        try (ImageInputStream entrada = ImageIO.createImageInputStream(
                new ByteArrayInputStream(archivo.getBytes()))) {
            if (entrada == null) {
                throw new IllegalArgumentException("Selecciona una imagen PNG o JPEG válida.");
            }
            Iterator<ImageReader> lectores = ImageIO.getImageReaders(entrada);
            if (!lectores.hasNext()) {
                throw new IllegalArgumentException("Selecciona una imagen PNG o JPEG válida.");
            }
            ImageReader lector = lectores.next();
            try {
                lector.setInput(entrada, true, true);
                String formato = lector.getFormatName();
                if (!formato.equalsIgnoreCase("png") && !formato.equalsIgnoreCase("jpeg")) {
                    throw new IllegalArgumentException("El avatar debe ser una imagen PNG o JPEG.");
                }
                int ancho = lector.getWidth(0);
                int alto = lector.getHeight(0);
                if (ancho < 1 || alto < 1 || ancho > 2048 || alto > 2048) {
                    throw new IllegalArgumentException("El avatar no puede superar 2048 × 2048 píxeles.");
                }
                BufferedImage imagen = lector.read(0);
                try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
                    if (!ImageIO.write(imagen, "png", salida)) {
                        throw new IOException("No se pudo convertir el avatar a PNG.");
                    }
                    byte[] resultado = salida.toByteArray();
                    if (resultado.length > maximoBytes) {
                        throw new IllegalArgumentException("El avatar convertido no puede superar los 2 MB.");
                    }
                    return resultado;
                }
            } finally {
                lector.dispose();
            }
        }
    }

    @PostMapping("cerrarSesion")
    public String cerrarSesion(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "ok";
    }
}
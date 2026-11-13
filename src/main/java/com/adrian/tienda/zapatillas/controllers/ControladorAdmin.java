package com.adrian.tienda.zapatillas.controllers;


import org.springframework.stereotype.Controller;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControladorAdmin {
    // la redireccion que hace el interceptor
    // sera atendida aqui, que es el unico sitio
    // donde se puede devolver una vista
    @RequestMapping("loginAdmin")
    public String loginAdmin(){
        return "admin/login_admin";
    }

    @RequestMapping("admin/inicioAdmin")
    public String inicioAdmin(){
        return "admin/inicio_admin";
    }

    @GetMapping("admin/cerrarSesion")
    public String cerrarSesion(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

}

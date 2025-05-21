package br.com.calmaja.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TesteController {

    @GetMapping("/hello")
    public String hello(){
        return "Hello Calma Já";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public String user(){
        return "Usuário Normal Acessou o Sistema ! ";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String admin(){
        return "Administrador Acessou o Sistema ! ";
    }
}

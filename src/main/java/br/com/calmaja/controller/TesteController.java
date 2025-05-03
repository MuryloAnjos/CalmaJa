package br.com.calmaja.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TesteController {

    @GetMapping("/hello")
    public String hello(){
        return "Hello Calma Já";
    }
}

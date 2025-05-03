package br.com.calmaja.dto;

public record RegisterRequest(String username, String email, String telephone, String password, String role) {
}

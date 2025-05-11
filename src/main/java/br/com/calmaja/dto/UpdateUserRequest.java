package br.com.calmaja.dto;

public record UpdateUserRequest(String username, String email, String oldPassword, String newPassword) {
}

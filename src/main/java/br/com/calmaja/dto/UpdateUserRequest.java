package br.com.calmaja.dto;

public record UpdateUserRequest(String username, String email, String telephone, String bio, String oldPassword, String newPassword) {
}

package br.com.calmaja.dto;

import java.util.UUID;

public record UserResponse(UUID id, String username, String email) {
}

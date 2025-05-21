package br.com.calmaja.dto;

import org.springframework.web.multipart.MultipartFile;

public record UpdateUserRequest(String username, String email, String telephone, String bio, String oldPassword, String newPassword, MultipartFile profileImage) {
}

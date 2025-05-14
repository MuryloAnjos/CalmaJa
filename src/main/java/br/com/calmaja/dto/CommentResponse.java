package br.com.calmaja.dto;

import java.time.LocalDateTime;

public record CommentResponse(Long id, String content, LocalDateTime createdAt, UserResponse userResponse) {
}

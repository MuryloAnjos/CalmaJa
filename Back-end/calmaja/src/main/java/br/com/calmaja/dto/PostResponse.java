package br.com.calmaja.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(Long id, String title, String content, int upvotes, int complaints, LocalDateTime createdAt, UserResponse user, List<CommentResponse> comments, boolean isVerified){
}

package br.com.calmaja.dto;

import br.com.calmaja.model.User;

public record CreatePostRequest(String title, String content) {
}

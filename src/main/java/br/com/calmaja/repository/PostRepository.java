package br.com.calmaja.repository;

import br.com.calmaja.model.Post;
import br.com.calmaja.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByContent(String content);
    List<Post> findByCreatedBy(User user);
    List<Post> findByCreatedByIn(List<User> users);
    List<Post> findByIsVerified(Boolean isVerified);
}

package br.com.calmaja.repository;

import br.com.calmaja.model.Post;
import br.com.calmaja.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.createdBy LEFT JOIN FETCH p.comments c LEFT JOIN FETCH c.user WHERE p.content LIKE %:content%")
    List<Post> findByContent(String content);

    @Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.createdBy LEFT JOIN FETCH p.comments c LEFT JOIN FETCH c.user WHERE p.isVerified = :isVerified")
    List<Post> findByIsVerified(Boolean isVerified);

    @Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.createdBy LEFT JOIN FETCH p.comments c LEFT JOIN FETCH c.user WHERE p.createdBy IN :users")
    List<Post> findByCreatedByInWithFetch (List<User> users);

    @Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.createdBy LEFT JOIN FETCH p.comments c LEFT JOIN FETCH c.user")
    List<Post> findAllWithFetch();

    @Query("SELECT DISTINCT p FROM Post p JOIN FETCH p.createdBy LEFT JOIN FETCH p.comments c LEFT JOIN FETCH c.user WHERE p.id = :id")
    Optional<Post> findByIdWithFetch(Long id);


}

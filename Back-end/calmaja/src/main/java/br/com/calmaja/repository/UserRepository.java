package br.com.calmaja.repository;

import br.com.calmaja.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.following LEFT JOIN FETCH u.followers WHERE u.id = :id")
    Optional<User> findByIdWithFetch(UUID id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.following LEFT JOIN FETCH u.followers WHERE u.refreshToken = :refreshToken")
    Optional<User> findByRefreshToken(String refreshToken);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.following LEFT JOIN FETCH u.followers WHERE u.username = :username")
    Optional<User> findByUsernameWithFollowing(String username);
}

package br.com.calmaja.service;

import br.com.calmaja.dto.UpdateUserRequest;
import br.com.calmaja.model.User;
import br.com.calmaja.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserById(UUID id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found !"));
        return user;
    }

    public User getUserByUsername(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found !"));
        return user;
    }

    public User updateUser(String username, UpdateUserRequest request){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found !"));

        if(request.username() != null || !request.username().isEmpty()){
            user.setUsername(request.username());
        }

        if(request.email() != null || !request.email().isEmpty()){
            user.setEmail(request.email());
        }

        if(request.newPassword() != null || !request.newPassword().isEmpty()){
            if(request.oldPassword() == null || request.oldPassword().isEmpty()){
                throw new IllegalArgumentException("Old Password Required");
            }
            if(!passwordEncoder.matches(request.oldPassword(), user.getPassword())){
                throw new IllegalArgumentException("Senha Antiga Incorreta");
            }
            user.setPassword(passwordEncoder.encode(request.newPassword()));
        }
        return userRepository.save(user);
    }

    public void followUser(UUID followedId, UUID followerId){
        User followed = userRepository.findByIdWithFetch(followedId)
                .orElseThrow(() -> new RuntimeException("Followed not found ! "));

        User follower = userRepository.findByIdWithFetch(followerId)
                .orElseThrow(() -> new RuntimeException("Follower not found ! "));

        if(follower.getFollowing().contains(followed)){
            throw new RuntimeException("Already following this user!");
        }

        follower.getFollowing().add(followed);
        userRepository.save(follower);

    }

    public void unfollowUser(UUID followedId, UUID followerId){
        User followed = userRepository.findByIdWithFetch(followedId)
                .orElseThrow(() -> new RuntimeException("Followed not found ! "));

        User follower = userRepository.findByIdWithFetch(followerId)
                .orElseThrow(() -> new RuntimeException("Follower not found ! "));

        if(!follower.getFollowing().remove(followed)){
            throw new RuntimeException("Not Following this user!");
        }

        userRepository.save(follower);
    }

}

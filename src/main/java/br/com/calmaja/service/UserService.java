package br.com.calmaja.service;

import br.com.calmaja.dto.TokenResponse;
import br.com.calmaja.dto.UpdateUserRequest;
import br.com.calmaja.model.User;
import br.com.calmaja.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final FileStorageService fileStorageService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.fileStorageService = fileStorageService;
    }

    public User getUserById(UUID id){
        User user = userRepository.findByIdWithFetch(id)
                .orElseThrow(() -> new RuntimeException("User not found !"));
        return user;
    }

    public User getUserByUsername(String username){
        User user = userRepository.findByUsernameWithFollowing(username)
                .orElseThrow(() -> new RuntimeException("User not found !"));
        return user;
    }

    public TokenResponse updateUser(String username, UpdateUserRequest request){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not Found !"));

        boolean usernameChanged = false;
        boolean passwordChanged = false;

        if(request.username() != null || !request.username().isEmpty()){
            user.setUsername(request.username());
        }

        if(request.telephone() != null || !request.telephone().isEmpty()){
            user.setTelephone(request.telephone());
        }

        if(request.email() != null || !request.email().isEmpty()){
            user.setEmail(request.email());
        }

        if(request.bio() != null || !request.bio().isEmpty()){
            user.setBioUser(request.bio());
        }

        if(request.newPassword() != null || !request.newPassword().isEmpty()){
            if(request.oldPassword() == null || request.oldPassword().isEmpty()){
                throw new IllegalArgumentException("Old Password Required");
            }
            if(!passwordEncoder.matches(request.oldPassword(), user.getPassword())){
                throw new IllegalArgumentException("Senha Antiga Incorreta");
            }
            user.setPassword(passwordEncoder.encode(request.newPassword()));
            passwordChanged = true;
            user.setRefreshToken(null);
            user.setRefreshTokenExpiryDate(null); // invalida os refresh tokens antiga
        }

        if(request.profileImage() != null || !request.profileImage().isEmpty()){
            String fileName = fileStorageService.storeFile(request.profileImage(), user.getId());
            user.setProfileImagePath(fileName);
        }

        User updatedUser = userRepository.save(user);

        String newAccessToken = null;
        String newRefreshToken = null;

        if(usernameChanged || passwordChanged){
            Authentication authentication = new UsernamePasswordAuthenticationToken(updatedUser.getUsername(), null, updatedUser.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName())).collect(Collectors.toList()));
            newAccessToken = jwtService.generateAccessToken(authentication);
            newRefreshToken = jwtService.generateRefreshToken(authentication);
            updatedUser.setRefreshToken(newRefreshToken);
            updatedUser.setRefreshTokenExpiryDate(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000));
            updatedUser = userRepository.save(updatedUser);
        }

        return new TokenResponse(newAccessToken, newRefreshToken);

    }

    public TokenResponse refreshToken(User user){
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName())).collect(Collectors.toList()));

        String newAccessToken = jwtService.generateAccessToken(authentication);
        String newRefreshToken = jwtService.generateRefreshToken(authentication);
        user.setRefreshToken(newRefreshToken);
        user.setRefreshTokenExpiryDate(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000));
        userRepository.save(user);
        return new TokenResponse(newAccessToken, newRefreshToken);
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

package br.com.calmaja.controller;

import br.com.calmaja.dto.TokenResponse;
import br.com.calmaja.dto.UpdateUserRequest;
import br.com.calmaja.model.User;
import br.com.calmaja.model.UserAuthenticated;
import br.com.calmaja.repository.UserRepository;
import br.com.calmaja.service.FileStorageService;
import br.com.calmaja.service.JwtService;
import br.com.calmaja.service.UserService;
import org.apache.coyote.Response;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public UserController(UserService userService, UserRepository userRepository, FileStorageService fileStorageService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/current")
    @PreAuthorize("hasRole('USER') or hasRole('SPECIALIST')")
    public ResponseEntity<User> getCurrentUser(Authentication authentication){
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("sub");
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('SPECIALIST')")
    public ResponseEntity<User> getUserById(@PathVariable UUID id, Authentication authentication){
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) //vai permitir update de imagem ao atualizar user
    @PreAuthorize("hasRole('USER') or hasRole('SPECIALIST')")
    public ResponseEntity<TokenResponse> updateUser(@RequestPart("request") UpdateUserRequest request, Authentication authentication){
        if(authentication == null || authentication.getPrincipal() == null){
            return ResponseEntity.status(400).build();
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("sub");

        TokenResponse updatedUser = userService.updateUser(username, request);
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.badRequest().build();
    }

    @GetMapping("profile-image/{fileName:.+}")
    public ResponseEntity<Resource> getProfileUser(@PathVariable String fileName){
        Resource resource = fileStorageService.loadAsResource(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    @PostMapping("/refresh")
    @PreAuthorize("hasRole('USER') or hasRole('SPECIALIST')")
    public ResponseEntity<TokenResponse> refreshToken(@RequestParam String refreshToken){
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh Token invalid!"));

        if(user.getRefreshTokenExpiryDate().before(new Date())){
            throw new RuntimeException("Refresh Token expired!");
        }

        TokenResponse token = userService.refreshToken(user);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/{followedId}/follow")
    @PreAuthorize("hasRole('USER') or hasRole('SPECIALIST')")
    public ResponseEntity<String> followUser(@PathVariable UUID followedId, Authentication authentication){
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("sub");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found !"));

        userService.followUser(followedId, user.getId());
        return ResponseEntity.ok("User followed successfully");
    }

    @DeleteMapping("/{followedId}/unfollow")
    @PreAuthorize("hasRole('USER') or hasRole('SPECIALIST')")
    public ResponseEntity<String> unfollowUser(@PathVariable UUID followedId, Authentication authentication){
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("sub");
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found !"));

        userService.unfollowUser(followedId, user.getId());
        return ResponseEntity.ok("User unfollowed successfully");
    }



}

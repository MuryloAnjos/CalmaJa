package br.com.calmaja.controller;

import br.com.calmaja.dto.UpdateUserRequest;
import br.com.calmaja.model.User;
import br.com.calmaja.model.UserAuthenticated;
import br.com.calmaja.service.UserService;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("calmaja/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUser(Authentication authentication){
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("sub");
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id, Authentication authentication){
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/update")
    public ResponseEntity<User> updateUser(@RequestBody UpdateUserRequest request, Authentication authentication){
        if(authentication == null || authentication.getPrincipal() == null){
            return ResponseEntity.status(400).build();
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaimAsString("sub");

        User updatedUser = userService.updateUser(username, request);
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.badRequest().build();
    }

 }

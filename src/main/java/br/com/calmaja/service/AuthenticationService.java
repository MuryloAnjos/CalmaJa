package br.com.calmaja.service;

import br.com.calmaja.dto.LoginRequest;
import br.com.calmaja.dto.RegisterRequest;
import br.com.calmaja.dto.TokenResponse;
import br.com.calmaja.model.Role;
import br.com.calmaja.model.User;
import br.com.calmaja.repository.RoleRepository;
import br.com.calmaja.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;

@Service
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;

    public AuthenticationService(PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, JwtService jwtService) {
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
    }


    public void register(RegisterRequest request){

        if(userRepository.findByUsername(request.username()).isPresent()){
            throw new RuntimeException("User already exists");
        }

        String roleName = request.role().toUpperCase();

        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(roleName);
                    return roleRepository.save(newRole);
                });

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setTelephone(request.telephone());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(new HashSet<>());
        user.getRoles().add(role);
        userRepository.save(user);
    }

    public TokenResponse authenticate(LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        String accessToken = jwtService.generateAccessToken(authentication);
        String refreshToken = jwtService.generateRefreshToken(authentication);

        User user = userRepository.findByUsernameWithFollowing(request.username())
                .orElseThrow(() -> new RuntimeException("User not Found !"));
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiryDate(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000));
        userRepository.save(user);
        return new TokenResponse(accessToken, refreshToken);
    }

}

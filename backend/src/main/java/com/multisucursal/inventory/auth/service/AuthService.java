package com.multisucursal.inventory.auth.service;

import com.multisucursal.inventory.auth.dto.AuthResponse;
import com.multisucursal.inventory.auth.dto.LoginRequest;
import com.multisucursal.inventory.auth.dto.RegisterRequest;
import com.multisucursal.inventory.security.jwt.JwtService;
import com.multisucursal.inventory.user.entity.AppUser;
import com.multisucursal.inventory.user.entity.Role;
import com.multisucursal.inventory.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        AppUser user = new AppUser();
        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.EMPLEADO);
        user.setEnabled(true);

        AppUser savedUser = userRepository.save(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        return buildResponse(savedUser, jwtService.generateToken(userDetails));
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
        );

        AppUser user = userRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        return buildResponse(user, jwtService.generateToken(userDetails));
    }

    private AuthResponse buildResponse(AppUser user, String token) {
        return new AuthResponse(
            token,
            "Bearer",
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole()
        );
    }
}


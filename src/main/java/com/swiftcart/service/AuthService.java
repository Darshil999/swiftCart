package com.swiftcart.service;

import com.swiftcart.dto.request.LoginRequest;
import com.swiftcart.dto.request.RegisterRequest;
import com.swiftcart.dto.response.AuthResponse;
import com.swiftcart.entity.Role;
import com.swiftcart.entity.User;
import com.swiftcart.exception.EmailAlreadyExistsException;
import com.swiftcart.repository.UserRepository;
import com.swiftcart.security.JwtUtil;
import com.swiftcart.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user.
     * - Checks for duplicate email before persisting.
     * - Defaults role to BUYER if not provided.
     * - Password is BCrypt-hashed before storage.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        Role role = (request.getRole() != null) ? request.getRole() : Role.BUYER;

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);

        UserDetailsImpl userDetails = UserDetailsImpl.from(user);
        String token = jwtUtil.generateToken(userDetails);

        return buildAuthResponse(token, user);
    }

    /**
     * Authenticates user credentials via Spring Security's AuthenticationManager.
     * If invalid, AuthenticationManager throws BadCredentialsException (handled globally).
     */
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(); // Safe here — authentication already succeeded

        return buildAuthResponse(token, user);
    }

    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}

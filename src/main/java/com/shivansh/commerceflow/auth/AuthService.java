package com.shivansh.commerceflow.auth;

import com.shivansh.commerceflow.auth.dto.AuthResponse;
import com.shivansh.commerceflow.auth.dto.LoginRequest;
import com.shivansh.commerceflow.auth.dto.RegisterRequest;
import com.shivansh.commerceflow.auth.dto.UserResponse;
import com.shivansh.commerceflow.user.AppUser;
import com.shivansh.commerceflow.user.Role;
import com.shivansh.commerceflow.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {

        String normalizedEmail = request.email()
                .trim()
                .toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User already exists with email: " + normalizedEmail
            );
        }

        AppUser user = new AppUser();
        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(
                passwordEncoder.encode(request.password())
        );
        user.setRole(Role.CUSTOMER);
        user.setActive(true);

        AppUser savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        String normalizedEmail = request.email()
                .trim()
                .toLowerCase();

        AppUser user = userRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        if (!user.isActive()
                || !passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                mapToResponse(user)
        );
    }

    private UserResponse mapToResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
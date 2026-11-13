package za.co.sheeptracker.service;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.sheeptracker.dto.auth.AuthResponse;
import za.co.sheeptracker.dto.auth.LoginRequest;
import za.co.sheeptracker.dto.auth.RefreshRequest;
import za.co.sheeptracker.dto.auth.RegisterRequest;
import za.co.sheeptracker.entity.User;
import za.co.sheeptracker.exception.AuthenticationFailedException;
import za.co.sheeptracker.exception.DuplicateResourceException;
import za.co.sheeptracker.repository.UserRepository;
import za.co.sheeptracker.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email().toLowerCase())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .name(request.name().trim())
                .email(request.email().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.password()))
                .onboarded(false)
                .build();

        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(() -> new AuthenticationFailedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid credentials");
        }

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshRequest request) {
        String token = request.refreshToken();
        if (!jwtService.isTokenValid(token) || !"refresh".equals(jwtService.extractTokenType(token))) {
            throw new AuthenticationFailedException("Invalid or expired refresh token");
        }

        UUID userId = jwtService.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationFailedException("Account no longer exists"));

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String access = jwtService.generateAccessToken(user.getId(), user.getEmail());
        String refresh = jwtService.generateRefreshToken(user.getId(), user.getEmail());
        return new AuthResponse(access, refresh, user.getId(), user.getName(), user.getEmail(), user.isOnboarded());
    }
}

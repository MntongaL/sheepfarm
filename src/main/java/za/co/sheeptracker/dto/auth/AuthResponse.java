package za.co.sheeptracker.dto.auth;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UUID userId,
        String name,
        String email,
        boolean onboarded
) {
}

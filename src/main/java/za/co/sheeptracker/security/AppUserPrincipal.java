package za.co.sheeptracker.security;

import java.util.UUID;

/**
 * Lightweight authenticated-principal wrapper, avoids exposing the JPA User entity via SecurityContext.
 */
public record AppUserPrincipal(UUID userId, String email) {
}

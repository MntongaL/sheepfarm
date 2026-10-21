package za.co.sheeptracker.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/** Id of the authenticated user (set by JwtAuthFilter), or null when there is none. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static UUID idOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUserPrincipal principal) {
            return principal.userId();
        }
        return null;
    }
}

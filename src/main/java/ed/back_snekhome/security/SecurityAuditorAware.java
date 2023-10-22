package ed.back_snekhome.security;

import ed.back_snekhome.entities.user.UserEntity;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;


public class SecurityAuditorAware implements AuditorAware<UserEntity> {

    @Override
    public Optional<UserEntity> getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated())
            return Optional.empty();

        return Optional.of((UserEntity) authentication.getPrincipal());
    }
}

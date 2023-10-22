package ed.back_snekhome.security;


import ed.back_snekhome.entities.user.UserEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
class JpaConfig {

    @Bean
    public AuditorAware<UserEntity> auditorProvider() {
        return new SecurityAuditorAware();
    }

}

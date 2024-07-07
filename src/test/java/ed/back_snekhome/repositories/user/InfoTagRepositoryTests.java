package ed.back_snekhome.repositories.user;

import ed.back_snekhome.entities.user.InfoTag;
import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.helperComponents.UserHelper;
import ed.back_snekhome.repositories.user.InfoTagRepository;
import ed.back_snekhome.security.Role;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Before;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.core.userdetails.User;
import java.time.LocalDate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class InfoTagRepositoryTests {

    private InfoTagRepository infoTagRepository;
    private UserRepository userRepository;


    private UserEntity user;

    @Autowired
    public InfoTagRepositoryTests(InfoTagRepository infoTagRepository, UserRepository userRepository) {
        this.infoTagRepository = infoTagRepository;
        this.userRepository = userRepository;
    }


    @BeforeEach
    public void setUp() {
        user = UserEntity.builder()
                .email("tested_user@test.com")
                .enabled(true)
                .nickname("testovic200")
                .name("Test")
                .surname("Testovic")
                .password("$2a$10$1hLOIX6WJeF4cEWSlup2XulIm8jYiXWROFXFdT608JjwDclD7OuHO")
                .registration(LocalDate.now())
                .role(Role.USER)
                .nicknameColor("blue")
                .build();
        userRepository.save(user);
    }


    @Test
    public void infoTagRepository_Save_ReturnsSavedInfoTage() {

        var tag = InfoTag.builder()
                .user(user)
                .title("Salads I like")
                .text("Cezar, greek, salad with tuna")
                .build();
        var savedTag = infoTagRepository.save(tag);

        Assertions.assertThat(savedTag).isNotNull();
        Assertions.assertThat(savedTag.getIdTag()).isGreaterThan(0);
    }

}

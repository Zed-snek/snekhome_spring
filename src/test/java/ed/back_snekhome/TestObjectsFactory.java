package ed.back_snekhome;


import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.security.Role;

import java.time.LocalDate;

public class TestObjectsFactory {

    public static UserEntity createUserEntity() {
        return UserEntity.builder()
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
    }

}

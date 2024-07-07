package ed.back_snekhome.repositories.user;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static ed.back_snekhome.TestObjectsFactory.createUserEntity;



@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void userRepository_Save_ReturnsSavedUser() {

        var savedUser = userRepository.save(createUserEntity());

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getIdAccount()).isGreaterThan(0);
    }


    @Test
    public void userRepository_ExistsByNicknameIgnoreCase_ReturnsTrue() {
        var user = createUserEntity();
        userRepository.save(user);

        boolean isExist = userRepository.existsByNicknameIgnoreCase(user.getNickname());

        boolean isExistUpperCase = userRepository.existsByNicknameIgnoreCase(user.getNickname().toUpperCase());
        boolean isExistLowerCase = userRepository.existsByNicknameIgnoreCase(user.getNickname().toLowerCase());

        assertThat(isExist).isTrue(); //exists
        assertThat(isExistUpperCase && isExistLowerCase).isTrue(); //ignored case exists
    }

}

package ed.back_snekhome.helperComponents;


import ed.back_snekhome.entities.user.UserEntity;
import ed.back_snekhome.exceptionHandler.exceptions.EntityNotFoundException;
import ed.back_snekhome.exceptionHandler.exceptions.LoginNotFoundException;
import ed.back_snekhome.repositories.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static ed.back_snekhome.TestObjectsFactory.createUserEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class UserHelperTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserHelper userHelper;


    @Test
    public void UserHelper_FindByIdOrThrowErr_ReturnedUser() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.ofNullable(createUserEntity()));

        var user = userHelper.getUserByIdOrThrowErr(5L);
        assertThat(user).isNotNull();
    }

    @Test
    public void UserHelper_FindByIdOrThrowErr_ThrowsError() {
        when(userRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userHelper.getUserByIdOrThrowErr(5L));
    }

}

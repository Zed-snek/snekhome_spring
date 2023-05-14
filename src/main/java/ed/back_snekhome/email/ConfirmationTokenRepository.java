package ed.back_snekhome.email;

import org.springframework.data.repository.CrudRepository;

public interface ConfirmationTokenRepository extends CrudRepository<ConfirmationToken, Long> {

    ConfirmationToken findConfirmationTokenByToken(String token);

}

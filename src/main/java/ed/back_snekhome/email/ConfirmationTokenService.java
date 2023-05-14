package ed.back_snekhome.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfirmationTokenService { //For now, is used only to make confirmation code for verification by email

    private final ConfirmationTokenRepository confirmationTokenRepository;

    public ConfirmationToken save(ConfirmationToken confirmationToken){
        return confirmationTokenRepository.save(confirmationToken);
    }

    public ConfirmationToken findByToken(String token){
        return confirmationTokenRepository.findConfirmationTokenByToken(token);
    }

}
